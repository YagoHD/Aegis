package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AegisTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    Column {
        CenterAlignedTopAppBar(
            modifier = Modifier.height(if (subtitle != null) 72.dp else 56.dp),
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (subtitle != null) {
                        Text(
                            text = subtitle.uppercase(),
                            color = MaterialTheme.colorScheme.primary, // AegisBronze
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        )
                    }
                    Text(
                        text = title.uppercase(),
                        color = MaterialTheme.colorScheme.onBackground, // AegisWhite
                        fontSize = 15.sp, // Ajuste sutil de tamaño para elegancia
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            },
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                // CAMBIO CLAVE: Usamos 'surface' (SurfaceBars) en lugar de 'background'
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        // --- SEPARADOR TÉCNICO (La línea de 1.dp que marca la diferencia) ---
        HorizontalDivider(
            thickness = 1.dp,
            // AegisSteel con baja opacidad para que sea un brillo, no una línea pesada
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )
    }
}