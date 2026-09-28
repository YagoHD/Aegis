package com.yago.aegis.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Temas de la app en 3 dimensiones independientes y editables, para cambiar ABSOLUTAMENTE todo:
 *  - [AppTheme]        → color de ACENTO (primary). Base = "Aegis" (bronce).
 *  - [BackgroundTheme] → el NEGRO base (fondo + capas de superficie). Base = "Aegis".
 *  - [GrayTheme]       → el GRIS secundario (textos/bordes). Base = "Aegis".
 *
 * "Aegis" en cada dimensión son los colores base con los que está pensada la app.
 * `premium` marca cuáles se reservarán a Pro cuando llegue el gating (por ahora todos libres).
 */
enum class AppTheme(
    val id: String,
    val primary: Color,
    val primaryDark: Color,
    val onPrimary: Color,
    val premium: Boolean
) {
    AEGIS("aegis", Color(0xFFB39371), Color(0xFF8A6E51), Color.Black, premium = false),
    ORO("oro", Color(0xFFD4AF37), Color(0xFFA8862A), Color.Black, premium = true),
    PLATINO("platino", Color(0xFFC7CCD1), Color(0xFF9AA0A6), Color.Black, premium = true),
    ESMERALDA("esmeralda", Color(0xFF4FA97C), Color(0xFF3B8261), Color.Black, premium = true),
    RUBI("rubi", Color(0xFFC75B6A), Color(0xFF9E4552), Color.White, premium = true),
    ZAFIRO("zafiro", Color(0xFF6E93CB), Color(0xFF4F6F9E), Color.White, premium = true);

    companion object {
        val DEFAULT = AEGIS
        fun fromId(id: String?): AppTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/** El "negro" base: fondo + capas de superficie coherentes (barras, tarjetas, elevado). */
enum class BackgroundTheme(
    val id: String,
    val background: Color,
    val bars: Color,
    val surfaceVariant: Color,
    val elevated: Color,
    val premium: Boolean
) {
    AEGIS("aegis", Color(0xFF050505), Color(0xFF121212), Color(0xFF0E0E0E), Color(0xFF242424), premium = false),
    MEDIANOCHE("medianoche", Color(0xFF06080F), Color(0xFF10131C), Color(0xFF0B0E16), Color(0xFF1E2231), premium = true),
    GRAFITO("grafito", Color(0xFF101012), Color(0xFF1A1A1D), Color(0xFF151517), Color(0xFF2A2A2E), premium = true),
    EBANO("ebano", Color(0xFF0A0706), Color(0xFF161210), Color(0xFF100C0A), Color(0xFF272019), premium = true);

    companion object {
        val DEFAULT = AEGIS
        fun fromId(id: String?): BackgroundTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/** El "gris" secundario (textos secundarios, bordes, iconos apagados). */
enum class GrayTheme(
    val id: String,
    val color: Color,
    val premium: Boolean
) {
    AEGIS("aegis", Color(0xFF70706B), premium = false),
    ACERO("acero", Color(0xFF6C7480), premium = true),
    ARENA("arena", Color(0xFF8A8175), premium = true),
    HUMO("humo", Color(0xFF9A9A94), premium = true);

    companion object {
        val DEFAULT = AEGIS
        fun fromId(id: String?): GrayTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
