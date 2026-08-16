package com.yago.aegis.data.social

import com.yago.aegis.data.PanteonResult
import com.yago.aegis.data.RankTier

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
    val byGroup: Map<String, String> = emptyMap()   // "PECHO" -> "PLATINO", ...
)

/** Perfil que ven los amigos. Defaults para deserialización de Firestore. */
data class PublicProfile(
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val level: Int = 0,
    val overallTier: String = RankTier.SIN_RANGO.name,
    val groupTiers: Map<String, String> = emptyMap(),
    val updatedAt: Long = 0L
)

/** Amistad: un doc por pareja. status = "pending" | "accepted". */
data class Friendship(
    val users: List<String> = emptyList(),
    val requestedBy: String = "",
    val status: String = "pending",
    val updatedAt: Long = 0L
) {
    fun otherThan(uid: String): String? = users.firstOrNull { it != uid }
    val isPending: Boolean get() = status == "pending"
    val isAccepted: Boolean get() = status == "accepted"
}

/** Extrae el resumen compartible del resultado del Panteón (lógica pura, testeable). */
fun PanteonResult.toRankSummary(): RankSummary = RankSummary(
    overall = strongest?.tier?.name ?: RankTier.SIN_RANGO.name,
    byGroup = groups.associate { it.group.name to it.tier.name }
)
