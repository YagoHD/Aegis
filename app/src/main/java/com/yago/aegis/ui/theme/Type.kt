package com.yago.aegis.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.yago.aegis.R

// ─────────────────────────────────────────────────────────────────────────────
// FUENTE DE MARCA — Cinzel (inscripción romana, encaja con el tema "Panteón").
// Descargable vía Google Play Services. Si no está disponible, cae a serif del sistema.
// ─────────────────────────────────────────────────────────────────────────────
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val cinzel = GoogleFont("Cinzel")

/** Familia de marca para el wordmark y los títulos grandes (display/headline). */
val AegisBrandFamily = FontFamily(
    Font(googleFont = cinzel, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = cinzel, fontProvider = googleFontProvider, weight = FontWeight.Black)
)

// ─────────────────────────────────────────────────────────────────────────────
// ESCALA TIPOGRÁFICA (O1). Display/Headline usan la marca; el resto, sans del sistema.
// Los tamaños forman una escala limpia (10/11/13/14/15/18/20/24/32/40) para dar ritmo.
// ─────────────────────────────────────────────────────────────────────────────
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = AegisBrandFamily, fontWeight = FontWeight.Black,
        fontSize = 40.sp, letterSpacing = 4.sp
    ),
    displayMedium = TextStyle(
        fontFamily = AegisBrandFamily, fontWeight = FontWeight.Black,
        fontSize = 32.sp, letterSpacing = 3.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AegisBrandFamily, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AegisBrandFamily, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, letterSpacing = 1.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp
    )
)
