package com.yago.aegis.data.league

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Liga mensual global sobre Firestore (Fase 1, solo cliente).
 *
 * Colección global `leagueMembers/{uid}`: MI entrada de la temporada actual. Cualquier usuario
 * autenticado puede LEER (es una tabla mundial) pero solo escribir la SUYA (ver firestore.rules).
 * Los grupos de 30, el ascenso/descenso y el cierre mensual llegan en Fase 2 (Cloud Functions).
 *
 * Los getters propagan errores (fallo de red → el llamador decide); los mutadores devuelven Result.
 */
class LeagueDataSource {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val myUid: String? get() = auth.currentUser?.uid

    fun currentUid(): String? = myUid

    /**
     * Sube/actualiza MI entrada de la temporada (idempotente: un doc por usuario).
     * Usa **merge** para NO pisar `league`/`groupId` que el servidor (Cloud Functions, Fase 2) haya
     * fijado. En Fase 1 (sin Functions) esos campos no existen y el merge se comporta como un set.
     */
    suspend fun uploadEntry(entry: LeagueEntry): Result<Unit> {
        val me = myUid ?: return Result.failure(IllegalStateException("no-session"))
        return runCatching {
            db.collection("leagueMembers").document(me).set(
                mapOf(
                    "uid" to me,
                    "seasonId" to entry.seasonId,
                    "username" to entry.username,
                    "avatar" to entry.avatar,
                    "points" to entry.points,
                    "sessions" to entry.sessions,
                    "relativeWork" to entry.relativeWork,
                    "tier" to entry.tier,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
        }
    }

    /** Miembros del grupo (~30) de la temporada, para la tabla del grupo (Fase 2). */
    suspend fun getGroupMembers(seasonId: String, groupId: String): List<LeagueEntry> {
        val snap = db.collection("leagueGroups").document(seasonId)
            .collection("groups").document(groupId)
            .collection("members")
            .get().await()
        return snap.documents.map { d ->
            LeagueEntry(
                uid = d.getString("uid") ?: d.id,
                seasonId = seasonId,
                username = d.getString("username") ?: "",
                avatar = d.getString("avatar") ?: "",
                points = d.getLong("points") ?: 0L,
                tier = d.getString("league") ?: "BRONCE",
                league = d.getString("league") ?: "",
                groupId = groupId,
                updatedAt = d.getLong("updatedAt") ?: 0L
            )
        }
    }

    /** Mis medallas de temporadas cerradas (más recientes primero). */
    suspend fun getMedals(uid: String, limit: Long = 6): List<LeagueMedal> {
        val snap = db.collection("leagueMedals").document(uid)
            .collection("seasons")
            .orderBy("seasonId", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        return snap.documents.map { d ->
            LeagueMedal(
                seasonId = d.getString("seasonId") ?: d.id,
                league = d.getString("league") ?: "BRONCE",
                position = (d.getLong("position") ?: 0L).toInt(),
                groupSize = (d.getLong("groupSize") ?: 0L).toInt(),
                movement = d.getString("movement") ?: "stay"
            )
        }
    }

    /**
     * Top mundial por puntos (desc). Ordena solo por `points` (índice de un campo, automático:
     * sin índice compuesto). El filtro por temporada se hace en el cliente para no necesitar un
     * índice compuesto en Fase 1; por eso se piden más de los que se muestran.
     */
    suspend fun topEntries(limit: Long = 150): List<LeagueEntry> {
        val snap = db.collection("leagueMembers")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        return snap.documents.mapNotNull { it.toEntry() }
    }

    /** Mi entrada (para asegurarla en la tabla aunque quede fuera del top). null si no hay. */
    suspend fun getEntry(uid: String): LeagueEntry? {
        val doc = db.collection("leagueMembers").document(uid).get().await()
        return if (doc.exists()) doc.toEntry() else null
    }

    private fun DocumentSnapshot.toEntry(): LeagueEntry? {
        val uid = getString("uid") ?: id
        return LeagueEntry(
            uid = uid,
            seasonId = getString("seasonId") ?: "",
            username = getString("username") ?: "",
            avatar = getString("avatar") ?: "",
            points = getLong("points") ?: 0L,
            sessions = (getLong("sessions") ?: 0L).toInt(),
            relativeWork = getDouble("relativeWork") ?: 0.0,
            tier = getString("tier") ?: "BRONCE",
            league = getString("league") ?: "",
            groupId = getString("groupId") ?: "",
            updatedAt = getLong("updatedAt") ?: 0L
        )
    }
}
