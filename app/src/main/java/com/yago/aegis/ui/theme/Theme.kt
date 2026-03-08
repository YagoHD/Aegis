package com.yago.aegis.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AegisBronze,       // 10%: El oro/bronce para acciones
    onPrimary = Color.Black,

    secondary = AegisSteel,      // 30%: El gris técnico para textos secundarios
    onSecondary = AegisWhite,

    background = BackgroundBlack, // 60%: El negro profundo (050505)
    onBackground = AegisWhite,

    // --- LAS CAPAS DE SUPERFICIE ---
    surface = SurfaceBars,        // Para TopBar y BottomBar (121212)
    onSurface = AegisWhite,

    // ESTA ES LA QUE USA TU TARJETA:
    surfaceVariant = SurfaceDark, // Para las tarjetas (0E0E0E o 0C0C0C)
    onSurfaceVariant = AegisWhite,

    error = AegisError,           // Para la papelera y alertas
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AegisTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}