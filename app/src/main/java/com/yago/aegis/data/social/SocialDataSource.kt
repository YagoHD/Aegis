package com.yago.aegis.data.social

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * Capa de datos social sobre Firestore (fase 2). Reglas: ver firestore.rules.
 * - @usuarios en `usernames/{u}` (claim-once, resolución exacta).
 * - Amistades en `friendships/{par}` (un doc por pareja; id = pairId ordenado, igual que la regla).
 * - Perfil público en `publicProfiles/{uid}` (solo rango + nombre/nivel).
 * Los getters PROPAGAN errores (fallo de red → el llamador decide); los mutadores devuelven Result.
 */
class SocialDataSource {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val myUid: String? get() = auth.currentUser?.uid

    /** uid del usuario actual (para que el ViewModel separe amistades desde mi punto de vista). */
    fun currentUid(): String? = myUid

    // MISMO criterio que la función pairId() de las reglas (orden alfabético).
    private fun pairId(a: String, b: String): String = if (a < b) "${a}_${b}" else "${b}_${a}"

    // ---- @usuarios ----

    /** true si el @usuario está libre. (Coincidencia exacta; no lista a nadie.) */
    suspend fun isUsernameAvailable(username: String): Boolean =
        !db.collection("usernames").document(username).get().await().exists()

    /**
     * Reclama el @usuario. `set()` sobre un doc inexistente = create (permitido); sobre uno
     * existente = update, que las reglas DENIEGAN → si ya está cogido, lanza y devuelve failure.
     */
    suspend fun claimUsername(username: String): Result<Unit> {
        val me = myUid ?: return Result.failure(IllegalStateException("no-session"))
        return runCatching {
            db.collection("usernames").document(username).set(mapOf("uid" to me)).await()
        }
    }

    /** Resuelve un @usuario a su uid (exacto). null si no existe. */
    suspend fun findUidByUsername(username: String): String? =
        db.collection("usernames").document(username).get().await().getString("uid")

    // ---- Amistades ----

    /** Envía solicitud a [toUid] (@[toUsername]) firmando también mi @[myUsername] para mostrar. */
    suspend fun sendFriendRequest(toUid: String, toUsername: String, myUsername: String): Result<Unit> {
        val me = myUid ?: return Result.failure(IllegalStateException("no-session"))
        if (me == toUid) return Result.failure(IllegalArgumentException("self"))
        return runCatching {
            db.collection("friendships").document(pairId(me, toUid)).set(
                mapOf(
                    "users" to listOf(me, toUid),
                    "requestedBy" to me,
                    "status" to "pending",
                    "usernames" to mapOf(me to myUsername, toUid to toUsername),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }

    /** Acepta la solicitud del amigo [otherUid] (pending → accepted; solo la otra parte). */
    suspend fun acceptFriendship(otherUid: String): Result<Unit> {
        val me = myUid ?: return Result.failure(IllegalStateException("no-session"))
        return runCatching {
            db.collection("friendships").document(pairId(me, otherUid)).update(
                mapOf("status" to "accepted", "updatedAt" to System.currentTimeMillis())
            ).await()
        }
    }

    /** Rechaza / cancela / elimina la amistad con [otherUid] (borra el doc de la pareja). */
    suspend fun removeFriendship(otherUid: String): Result<Unit> {
        val me = myUid ?: return Result.failure(IllegalStateException("no-session"))
        return runCatching {
            db.collection("friendships").document(pairId(me, otherUid)).delete().await()
        }
    }

    /** Observa todas mis amistades (aceptadas + pendientes). La UI separa por estado/solicitante. */
    fun observeFriendships(): Flow<List<Friendship>> {
        val me = myUid ?: return flowOf(emptyList())
        return callbackFlow {
            val reg = db.collection("friendships")
                .whereArrayContains("users", me)
                .addSnapshotListener { snap, err ->
                    if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                    trySend(snap?.documents?.mapNotNull { it.toFriendship() } ?: emptyList())
                }
            awaitClose { reg.remove() }
        }
    }

    // ---- Perfil público ----

    /** Sube MI perfil público (solo rango + nombre/nivel). */
    suspend fun uploadPublicProfile(profile: PublicProfile): Result<Unit> {
        val me = myUid ?: return Result.failure(IllegalStateException("no-session"))
        return runCatching {
            db.collection("publicProfiles").document(me).set(
                mapOf(
                    "uid" to me,
                    "username" to profile.username,
                    "displayName" to profile.displayName,
                    "level" to profile.level,
                    "overallTier" to profile.overallTier,
                    "groupTiers" to profile.groupTiers,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }

    /** Lee el perfil público de un amigo (las reglas exigen amistad aceptada). null si no hay. */
    suspend fun getPublicProfile(uid: String): PublicProfile? {
        val doc = db.collection("publicProfiles").document(uid).get().await()
        if (!doc.exists()) return null
        @Suppress("UNCHECKED_CAST")
        return PublicProfile(
            uid = doc.getString("uid") ?: uid,
            username = doc.getString("username") ?: "",
            displayName = doc.getString("displayName") ?: "",
            level = (doc.getLong("level") ?: 0L).toInt(),
            overallTier = doc.getString("overallTier") ?: "SIN_RANGO",
            groupTiers = (doc.get("groupTiers") as? Map<String, String>) ?: emptyMap(),
            updatedAt = doc.getLong("updatedAt") ?: 0L
        )
    }

    private fun DocumentSnapshot.toFriendship(): Friendship? {
        val users = (get("users") as? List<*>)?.filterIsInstance<String>() ?: return null
        if (users.size != 2) return null
        @Suppress("UNCHECKED_CAST")
        val usernames = (get("usernames") as? Map<String, String>) ?: emptyMap()
        return Friendship(
            users = users,
            requestedBy = getString("requestedBy") ?: "",
            status = getString("status") ?: "pending",
            usernames = usernames,
            updatedAt = getLong("updatedAt") ?: 0L
        )
    }
}
