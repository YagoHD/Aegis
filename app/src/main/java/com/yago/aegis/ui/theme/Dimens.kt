package com.yago.aegis.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Tokens de espaciado (O5) y radios (O4). Una sola fuente de verdad para el ritmo
 * y las esquinas. Los valores coinciden con los dominantes actuales para migrar sin
 * cambios visuales bruscos: 8dp es el radio por defecto, 12dp contenedores, etc.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 40.dp
}

object Radius {
    val sm = 4.dp    // chips, elementos pequeños
    val md = 8.dp    // tarjetas y botones (por defecto)
    val lg = 12.dp   // contenedores destacados
    val xl = 16.dp   // tarjetas grandes / hero
    val pill = 50.dp // formas circulares/píldora
}
