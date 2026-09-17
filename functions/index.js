/**
 * Aegis — Cloud Functions de la LIGA mensual (US-LIGA, Fase 2).
 *
 * Piezas:
 *  1) onLeagueMemberWritten (trigger Firestore): asigna liga+grupo a quien entra en la temporada
 *     actual sin grupo (liga BRONCE para los nuevos) y mantiene el espejo del grupo al día.
 *  2) monthlyLeagueRollover (programada, día 1 00:05 UTC): cierra la temporada, calcula
 *     ascensos/descensos por grupo, archiva medallas, re-agrupa de 30 en 30 y resetea puntos.
 *  3) adminRunRollover (HTTP con secreto): dispara el cierre a mano para PROBAR sin esperar al mes.
 *
 * Modelo de datos (ver docs/us-liga-fase2-deploy.md):
 *   leagueMembers/{uid}                                  el cliente escribe puntos; el server pone league+groupId
 *   leagueGroups/{seasonId}/groups/{groupId}             { league, count }
 *   leagueGroups/{seasonId}/groups/{groupId}/members/{uid}  espejo { uid, username, avatar, points, league }
 *   leagueGroups/{seasonId}/meta/{league}                { groupSeq, openGroupId, openCount }
 *   leagueSeasons/{seasonId}                             { status: open|closed }
 *   leagueMedals/{uid}/seasons/{seasonId}                { league, position, groupSize, movement }
 */

const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onRequest } = require("firebase-functions/v2/https");
const { defineString } = require("firebase-functions/params");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");

initializeApp();

// Secreto para disparar el cierre a mano (se define al desplegar). Ver el doc de despliegue.
const ROLLOVER_SECRET = defineString("LEAGUE_ROLLOVER_SECRET");

// ─── Constantes de liga (calibrables) ───────────────────────────────────────
const GROUP_SIZE = 30;      // tamaño objetivo de grupo
const PROMOTE_TOP = 7;      // suben los N primeros
const RELEGATE_BOTTOM = 5;  // bajan los N últimos
const LADDER = ["BRONCE", "PLATA", "ORO", "PLATINO", "DIAMANTE", "TITAN"];

// ─── Utilidades ──────────────────────────────────────────────────────────────

/** "YYYY-MM" en UTC de una fecha. */
function seasonId(date) {
  return `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, "0")}`;
}

function up(league) {
  const i = LADDER.indexOf(league);
  return i < 0 ? "BRONCE" : LADDER[Math.min(i + 1, LADDER.length - 1)];
}
function down(league) {
  const i = LADDER.indexOf(league);
  return i <= 0 ? "BRONCE" : LADDER[i - 1];
}

/** Cuántos suben/bajan en un grupo de [size] de la liga [league], evitando solapes en grupos pequeños. */
function cutoffs(size, league) {
  if (size <= 1) return { promote: 0, relegate: 0 };
  let promote = league === "TITAN" ? 0 : Math.min(PROMOTE_TOP, Math.floor(size / 3));
  let relegate = league === "BRONCE" ? 0 : Math.min(RELEGATE_BOTTOM, Math.floor(size / 4));
  // Nadie puede ascender y descender a la vez: recorta si se solapan.
  while (promote + relegate > size - 1 && (promote > 0 || relegate > 0)) {
    if (relegate >= promote && relegate > 0) relegate--;
    else if (promote > 0) promote--;
    else break;
  }
  return { promote, relegate };
}

function groupBy(arr, keyFn) {
  const m = {};
  for (const x of arr) {
    const k = keyFn(x);
    (m[k] = m[k] || []).push(x);
  }
  return m;
}

function shuffle(arr) {
  const r = [...arr];
  for (let i = r.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [r[i], r[j]] = [r[j], r[i]];
  }
  return r;
}

/** Escribe una lista de {ref, data, merge?} en lotes de 400 (límite Firestore = 500). */
async function commitInChunks(db, writes) {
  for (let i = 0; i < writes.length; i += 400) {
    const batch = db.batch();
    for (const w of writes.slice(i, i + 400)) {
      batch.set(w.ref, w.data, { merge: w.merge !== false });
    }
    await batch.commit();
  }
}

/**
 * Asigna un grupo con hueco (o crea uno nuevo) para [league] en [season], de forma transaccional.
 * Usa un doc-índice `meta/{league}` para no necesitar índices compuestos.
 */
async function assignGroup(db, season, league) {
  const metaRef = db.doc(`leagueGroups/${season}/meta/${league}`);
  return db.runTransaction(async (tx) => {
    const snap = await tx.get(metaRef);
    let seq = 0;
    let openGroup = null;
    let openCount = 0;
    if (snap.exists) {
      const m = snap.data();
      seq = m.groupSeq || 0;
      openGroup = m.openGroupId || null;
      openCount = m.openCount || 0;
    }
    if (!openGroup || openCount >= GROUP_SIZE) {
      seq += 1;
      openGroup = `${league}-${seq}`;
      openCount = 0;
    }
    openCount += 1;
    tx.set(metaRef, { groupSeq: seq, openGroupId: openGroup, openCount }, { merge: true });
    tx.set(
      db.doc(`leagueGroups/${season}/groups/${openGroup}`),
      { league, count: openCount, updatedAt: Date.now() },
      { merge: true }
    );
    return openGroup;
  });
}

// ─── 1) Trigger: asignación de grupo + espejo ────────────────────────────────

exports.onLeagueMemberWritten = onDocumentWritten("leagueMembers/{uid}", async (event) => {
  const after = event.data && event.data.after && event.data.after.exists ? event.data.after.data() : null;
  if (!after) return; // borrado

  const uid = event.params.uid;
  const db = getFirestore();
  const season = seasonId(new Date());

  // Solo gestionamos la temporada actual (los docs de rollover ya vienen con la nueva temporada).
  if (after.seasonId !== season) return;

  // Ya tiene liga y grupo → solo refresca el espejo del grupo (puntos/nombre/avatar).
  if (after.league && after.groupId) {
    await db
      .doc(`leagueGroups/${season}/groups/${after.groupId}/members/${uid}`)
      .set(
        {
          uid,
          username: after.username || "",
          avatar: after.avatar || "",
          points: after.points || 0,
          league: after.league,
          updatedAt: Date.now(),
        },
        { merge: true }
      );
    return;
  }

  // Nuevo participante de la temporada (sin grupo): entra en BRONCE (la liga se gana ascendiendo).
  const league = "BRONCE";
  const groupId = await assignGroup(db, season, league);

  // Escribe liga+grupo en su doc (merge preserva sus puntos). Esto re-dispara el trigger, que
  // en la 2ª pasada verá league+groupId y solo actualizará el espejo (sin bucle).
  await db.doc(`leagueMembers/${uid}`).set({ league, groupId }, { merge: true });
  await db
    .doc(`leagueGroups/${season}/groups/${groupId}/members/${uid}`)
    .set(
      {
        uid,
        username: after.username || "",
        avatar: after.avatar || "",
        points: after.points || 0,
        league,
        updatedAt: Date.now(),
      },
      { merge: true }
    );
});

// ─── 2) Cierre mensual (ascenso/descenso + reset + medallas) ─────────────────

/** Cierra [closedSeason] y siembra [newSeason]. Idempotente (si ya está cerrada, no hace nada). */
async function doRollover(closedSeason, newSeason) {
  const db = getFirestore();

  const seasonRef = db.doc(`leagueSeasons/${closedSeason}`);
  const seasonSnap = await seasonRef.get();
  if (seasonSnap.exists && seasonSnap.data().status === "closed") {
    return { skipped: "already-closed" };
  }

  // 1) Todos los miembros de la temporada que cierra (un doc por usuario).
  const membersSnap = await db.collection("leagueMembers").where("seasonId", "==", closedSeason).get();
  const members = membersSnap.docs.map((d) => ({ uid: d.id, ...d.data() }));

  const writes = [];
  const promoted = []; // { uid, username, avatar, newLeague }

  // 2) Por grupo: ordena, decide ascenso/descenso, archiva medallas.
  const byGroup = groupBy(members, (m) => m.groupId || "UNASSIGNED");
  for (const list of Object.values(byGroup)) {
    const sorted = [...list].sort((a, b) => (b.points || 0) - (a.points || 0));
    const size = sorted.length;
    const league = sorted[0] && sorted[0].league ? sorted[0].league : "BRONCE";
    const { promote, relegate } = cutoffs(size, league);

    sorted.forEach((m, idx) => {
      const curLeague = m.league || "BRONCE";
      let movement = "stay";
      let newLeague = curLeague;
      if (idx < promote) {
        newLeague = up(curLeague);
        movement = "up";
      } else if (idx >= size - relegate) {
        newLeague = down(curLeague);
        movement = "down";
      }
      promoted.push({ uid: m.uid, username: m.username || "", avatar: m.avatar || "", newLeague });
      writes.push({
        ref: db.doc(`leagueMedals/${m.uid}/seasons/${closedSeason}`),
        data: {
          seasonId: closedSeason,
          league: curLeague,
          position: idx + 1,
          groupSize: size,
          movement,
          createdAt: Date.now(),
        },
      });
    });
  }

  // 3) Re-agrupa por nueva liga en grupos de 30, resetea puntos y siembra la nueva temporada.
  const byLeague = groupBy(promoted, (p) => p.newLeague);
  for (const [league, list] of Object.entries(byLeague)) {
    const shuffled = shuffle(list);
    let seq = 0;
    for (let i = 0; i < shuffled.length; i += GROUP_SIZE) {
      seq += 1;
      const groupId = `${league}-${seq}`;
      const chunk = shuffled.slice(i, i + GROUP_SIZE);
      chunk.forEach((p) => {
        writes.push({
          ref: db.doc(`leagueMembers/${p.uid}`),
          data: {
            uid: p.uid,
            seasonId: newSeason,
            league,
            groupId,
            points: 0,
            sessions: 0,
            relativeWork: 0,
            username: p.username,
            avatar: p.avatar,
            updatedAt: Date.now(),
          },
        });
        writes.push({
          ref: db.doc(`leagueGroups/${newSeason}/groups/${groupId}/members/${p.uid}`),
          data: { uid: p.uid, username: p.username, avatar: p.avatar, points: 0, league, updatedAt: Date.now() },
        });
      });
      writes.push({
        ref: db.doc(`leagueGroups/${newSeason}/groups/${groupId}`),
        data: { league, count: chunk.length, updatedAt: Date.now() },
      });
      // El último grupo de cada liga queda "abierto" para quien entre a mitad de mes.
      writes.push({
        ref: db.doc(`leagueGroups/${newSeason}/meta/${league}`),
        data: { groupSeq: seq, openGroupId: groupId, openCount: chunk.length },
      });
    }
  }

  // 4) Marca temporadas.
  writes.push({ ref: db.doc(`leagueSeasons/${closedSeason}`), data: { seasonId: closedSeason, status: "closed", closedAt: Date.now() } });
  writes.push({ ref: db.doc(`leagueSeasons/${newSeason}`), data: { seasonId: newSeason, status: "open", startedAt: Date.now() } });

  await commitInChunks(db, writes);

  // 5) Limpieza best-effort de los grupos de la temporada cerrada (medallas y nueva temporada se conservan).
  await db.recursiveDelete(db.collection(`leagueGroups/${closedSeason}/groups`)).catch(() => {});
  await db.recursiveDelete(db.collection(`leagueGroups/${closedSeason}/meta`)).catch(() => {});

  return { members: members.length, groups: Object.keys(byGroup).length };
}

exports.monthlyLeagueRollover = onSchedule({ schedule: "5 0 1 * *", timeZone: "UTC" }, async () => {
  const now = new Date();
  const newSeason = seasonId(now);
  // Temporada cerrada = mes anterior (último día del mes previo).
  const prev = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), 1));
  prev.setUTCDate(0);
  const closedSeason = seasonId(prev);
  const res = await doRollover(closedSeason, newSeason);
  console.log("monthlyLeagueRollover", { closedSeason, newSeason, ...res });
});

// ─── 3) Cierre a mano para PROBAR (HTTP con secreto) ─────────────────────────

exports.adminRunRollover = onRequest(async (req, res) => {
  if ((req.query.secret || "") !== ROLLOVER_SECRET.value()) {
    res.status(403).send("forbidden");
    return;
  }
  const closedSeason = String(req.query.closedSeason || "");
  const newSeason = String(req.query.newSeason || "");
  if (!/^\d{4}-\d{2}$/.test(closedSeason) || !/^\d{4}-\d{2}$/.test(newSeason)) {
    res.status(400).send("need closedSeason & newSeason as YYYY-MM");
    return;
  }
  try {
    const result = await doRollover(closedSeason, newSeason);
    res.json({ ok: true, closedSeason, newSeason, result });
  } catch (e) {
    console.error("adminRunRollover failed", e);
    res.status(500).json({ ok: false, error: String(e) });
  }
});
