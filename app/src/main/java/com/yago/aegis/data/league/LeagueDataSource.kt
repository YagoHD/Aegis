package com.yago.aegis.data.league

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    /** Sube/actualiza MI entrada de la temporada (idempotente: un doc por usuario). */
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
                )
            ).await()
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
            updatedAt = getLong("updatedAt") ?: 0L
        )
    }
}
