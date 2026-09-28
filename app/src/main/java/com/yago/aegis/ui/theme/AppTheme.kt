package com.yago.aegis.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Temas de acento de la app. Todos mantienen el fondo negro de lujo; solo cambia el color
 * primario (acento) y su contraste. El resto del sistema usa MaterialTheme.colorScheme.primary,
 * así que cambiar el tema re-tinta la app sin tocar cada pantalla.
 *
 * BRONCE es el tema por defecto (gratuito). El resto se reservan para premium cuando llegue el
 * gating isPremium (por ahora todos disponibles).
 */
enum class AppTheme(
    val id: String,
    val primary: Color,
    val primaryDark: Color,
    val onPrimary: Color,
    val premium: Boolean
) {
    BRONCE("bronce", Color(0xFFB39371), Color(0xFF8A6E51), Color.Black, premium = false),
    ORO("oro", Color(0xFFD4AF37), Color(0xFFA8862A), Color.Black, premium = true),
    PLATINO("platino", Color(0xFFC7CCD1), Color(0xFF9AA0A6), Color.Black, premium = true),
    ESMERALDA("esmeralda", Color(0xFF4FA97C), Color(0xFF3B8261), Color.Black, premium = true),
    RUBI("rubi", Color(0xFFC75B6A), Color(0xFF9E4552), Color.White, premium = true),
    ZAFIRO("zafiro", Color(0xFF6E93CB), Color(0xFF4F6F9E), Color.White, premium = true);

    companion object {
        val DEFAULT = BRONCE
        fun fromId(id: String?): AppTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
