# US-LIGA Fase 2 — Despliegue (Cloud Functions)

> La Fase 2 convierte la liga en el "Clash real": **grupos de ~30**, **ascenso/descenso** y
> **cierre mensual** con reset + **medallas de temporada**. Todo el código está en el repo; **tú lo
> despliegas** (Claude no despliega). Requiere el plan **Blaze** de Firebase (pago por uso; la capa
> gratuita cubre de sobra unos pocos usuarios).

El cliente Android ya está preparado: si las Functions no están desplegadas, la pestaña LIGA sigue
funcionando en modo **tabla mundial** (Fase 1). En cuanto despliegues y el servidor te asigne grupo,
la pestaña pasa **sola** a modo **grupo** (con zonas de ascenso/descenso y medallas).

## Qué se despliega

- `functions/index.js` — 3 piezas:
  - `onLeagueMemberWritten` (trigger Firestore): asigna liga **BRONCE** + grupo a quien entra sin
    grupo, y mantiene al día el espejo del grupo (`leagueGroups/.../members`).
  - `monthlyLeagueRollover` (programada, **día 1 a las 00:05 UTC**): cierra la temporada, calcula
    ascensos (7 primeros) / descensos (5 últimos) por grupo, archiva medallas, re-agrupa de 30 en 30
    y **resetea puntos a 0**.
  - `adminRunRollover` (HTTP con secreto): dispara el cierre **a mano** para probar sin esperar al mes.
- `firestore.rules` — reglas nuevas de `leagueMembers` (campos `league`/`groupId` protegidos =
  server-only), `leagueSeasons`, `leagueGroups`, `leagueMedals`.

## Requisitos previos

```bash
# 1) Node 20 y Firebase CLI
node --version           # debe ser 20.x
npm install -g firebase-tools
firebase login

# 2) Plan Blaze (necesario para Functions programadas / trigger)
#    Firebase Console → ⚙ Configuración → Uso y facturación → cambiar a Blaze.
```

## Pasos

```bash
# En la raíz del repo (donde están firebase.json y la carpeta functions/)

# 1) Instala dependencias de las Functions
cd functions
npm install
cd ..

# 2) Define el secreto para el cierre manual de pruebas (elige una cadena larga tú)
#    Se guarda como parámetro/secreto del proyecto; lo pedirá al desplegar si no existe.
firebase functions:secrets:set LEAGUE_ROLLOVER_SECRET
#   (pega tu secreto cuando lo pida)

# 3) Despliega Functions + reglas
firebase deploy --only functions,firestore:rules
```

> Si `functions:secrets:set` te da problemas, alternativa con parámetro `.env`:
> crea `functions/.env` con `LEAGUE_ROLLOVER_SECRET=tu-secreto` y vuelve a desplegar.
> (No lo subas al repo — `functions/.gitignore` ya ignora `.env` si lo añades.)

## Probar el cierre mensual sin esperar al mes

Tras desplegar, entra en la app en 2+ cuentas con @usuario y abre la pestaña LIGA (así se crean sus
`leagueMembers` y el trigger las mete en un grupo de BRONCE). Luego fuerza un cierre por HTTP:

```bash
# Sustituye REGION (p.ej. us-central1) y el secreto. closedSeason = temporada actual, newSeason = la siguiente.
curl "https://us-central1-aegis-c1471.cloudfunctions.net/adminRunRollover?secret=TU_SECRETO&closedSeason=2026-09&newSeason=2026-10"
```

Respuesta esperada: `{"ok":true,...}`. Al reabrir la app, las cuentas del top 7 habrán **subido** de
liga, las del fondo 5 habrán **bajado**, los puntos estarán a **0** y aparecerá una **medalla** de la
temporada cerrada. La URL exacta de la función sale también al final de `firebase deploy`.

## Modelo de datos (referencia)

```
leagueMembers/{uid}                                   { seasonId, username, avatar, points, sessions,
                                                        relativeWork, tier,  league*, groupId* }   (* = server-only)
leagueGroups/{seasonId}/groups/{groupId}              { league, count }
leagueGroups/{seasonId}/groups/{groupId}/members/{uid} { uid, username, avatar, points, league }   (espejo)
leagueGroups/{seasonId}/meta/{league}                 { groupSeq, openGroupId, openCount }          (interno)
leagueSeasons/{seasonId}                              { status: open|closed }
leagueMedals/{uid}/seasons/{seasonId}                 { league, position, groupSize, movement }
```

## Notas / calibración

- **Constantes** (en `functions/index.js` y espejadas en `LeagueSystem.kt`): `GROUP_SIZE=30`,
  `PROMOTE_TOP=7`, `RELEGATE_BOTTOM=5`. Si las cambias, cámbialas **en los dos sitios** (el cliente
  pinta las zonas verde/rojo con esos números).
- **Cold-start** (pocos usuarios): los grupos no se llenan a 30; el cierre re-agrupa lo que haya y las
  cotas se escalan solas para grupos pequeños (nadie sube y baja a la vez).
- **Puntos**: los sigue calculando el cliente (fórmula de esfuerzo relativo al peso). El **anti-trampa
  server** (recomputar el score desde el historial real) es **Fase 3**.
- **Coste**: con pocos usuarios, dentro de la capa gratuita de Blaze. El trigger se ejecuta una vez por
  escritura de puntos (una por apertura de la pestaña / fin de entreno) — insignificante.
- **Zona horaria del cierre**: 00:05 UTC del día 1. Si prefieres otra, edita el `schedule`/`timeZone`
  de `monthlyLeagueRollover`.
