package com.yago.aegis.data.social

import com.yago.aegis.data.PanteonResult
import com.yago.aegis.data.RankTier
import com.yago.aegis.data.divisionFromProgress

/**
 * Capa social (amigos + ranking competitivo).
 *
 * PRIVACIDAD: el perfil público solo lleva el RANGO YA CALCULADO (tiers) + nombre/nivel.
 * Nunca se suben los pesos ni el historial. Y como el rango ya está normalizado por sexo,
 * comparar tiers entre amigos es justo.
 */

/** Trozo compartible: rango por grupo + tier global (nombre del enum RankTier, p.ej. "ORO"). */
data class RankSummary(
    val overall: String = RankTier.SIN_RANGO.name,
    val byGroup: Map<String, String> = emptyMap(),       // "PECHO" -> "PLATINO", ...
    val byGroupDivision: Map<String, Int> = emptyMap()   // "PECHO" -> 1 (I) .. 3 (III)
)

/** Perfil que ven los amigos. Defaults para deserialización de Firestore. */
data class PublicProfile(
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val level: Int = 0,
    val overallTier: String = RankTier.SIN_RANGO.name,
    val groupTiers: Map<String, String> = emptyMap(),
    val groupDivisions: Map<String, Int> = emptyMap(),
    val updatedAt: Long = 0L
)

/** Amistad: un doc por pareja. status = "pending" | "accepted". */
data class Friendship(
    val users: List<String> = emptyList(),
    val requestedBy: String = "",
    val status: String = "pending",
    // uid -> @usuario, para mostrar amigos/solicitudes SIN leer el perfil ajeno (las reglas lo
    // bloquean hasta ser amigos). Se rellena al enviar la solicitud.
    val usernames: Map<String, String> = emptyMap(),
    val updatedAt: Long = 0L
) {
    fun otherThan(uid: String): String? = users.firstOrNull { it != uid }
    val isPending: Boolean get() = status == "pending"
    val isAccepted: Boolean get() = status == "accepted"
}

/** Extrae el resumen compartible del resultado del Panteón (lógica pura, testeable). */
fun PanteonResult.toRankSummary(): RankSummary = RankSummary(
    overall = strongest?.tier?.name ?: RankTier.SIN_RANGO.name,
    byGroup = groups.associate { it.group.name to it.tier.name },
    byGroupDivision = groups.associate { it.group.name to divisionFromProgress(it.progressToNext) }
)

/** Validación del @usuario: minúsculas, 3-20, letras/números/_ (sin espacios ni tildes). */
object UsernameRules {
    private val REGEX = Regex("^[a-z0-9_]{3,20}$")
    fun normalize(input: String): String = input.trim().lowercase().removePrefix("@")
    fun isValid(username: String): Boolean = REGEX.matches(username)
}

/** Un amigo/solicitud para la UI: uid + su @usuario (para mostrar). */
data class FriendRef(val uid: String, val username: String)

/** Mis amistades separadas para la UI. */
data class FriendBuckets(
    val friends: List<FriendRef> = emptyList(),    // amistad aceptada
    val incoming: List<FriendRef> = emptyList(),   // solicitudes recibidas (yo acepto)
    val outgoing: List<FriendRef> = emptyList()    // solicitudes que envié (pendientes)
)

/** Separa una lista de amistades en aceptadas / recibidas / enviadas, desde mi punto de vista. */
fun List<Friendship>.bucketsFor(myUid: String): FriendBuckets {
    val friends = mutableListOf<FriendRef>()
    val incoming = mutableListOf<FriendRef>()
    val outgoing = mutableListOf<FriendRef>()
    for (f in this) {
        val other = f.otherThan(myUid) ?: continue
        val ref = FriendRef(other, f.usernames[other] ?: "")
        when {
            f.isAccepted -> friends += ref
            f.requestedBy == myUid -> outgoing += ref   // pending que yo pedí
            else -> incoming += ref                     // pending que me pidieron
        }
    }
    return FriendBuckets(friends, incoming, outgoing)
}
