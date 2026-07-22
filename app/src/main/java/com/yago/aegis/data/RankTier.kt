package com.yago.aegis.data

/**
 * Rangos competitivos del Panteón, de menor a mayor.
 * [colorHex] es ARGB (Long) para no acoplar la capa de datos a Compose;
 * en la UI se convierte con Color(tier.colorHex).
 */
enum class RankTier(val display: String, val colorHex: Long) {
    SIN_RANGO("Sin Rango", 0xFF3A3A3A),
    BRONCE("Bronce", 0xFFCD7F32),
    PLATA("Plata", 0xFFC0C0C0),
    ORO("Oro", 0xFFD4AF37),
    PLATINO("Platino", 0xFFE5E4E2),
    DIAMANTE("Diamante", 0xFF4FC3F7),
    TITAN("Titán", 0xFFB57BFF);

    companion object {
        /** Tiers "reales" (excluye SIN_RANGO), de menor a mayor. */
        val ladder: List<RankTier> = listOf(BRONCE, PLATA, ORO, PLATINO, DIAMANTE, TITAN)
    }
}

/**
 * Un rango concreto con su división interna.
 * [division] 3 = III (más baja), 2 = II, 1 = I (más alta dentro del tier).
 */
data class Rank(
    val tier: RankTier = RankTier.SIN_RANGO,
    val division: Int = 3
) {
    /** Etiqueta legible, ej. "DIAMANTE III". */
    val label: String
        get() = if (tier == RankTier.SIN_RANGO) tier.display.uppercase()
                else "${tier.display.uppercase()} ${"I".repeat(division)}"
}
