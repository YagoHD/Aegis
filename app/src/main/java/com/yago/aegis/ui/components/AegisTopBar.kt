package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AegisTopBar(
    title: String,
    subtitle: String? = null, // ✅ Nuevo parámetro opcional
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.height(if (subtitle != null) 64.dp else 48.dp), // Ajusta la altura si hay subtítulo
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (subtitle != null) {
                    Text(
                        text = subtitle.uppercase(),
                        color = AegisBronze, // El color dorado de "IN PROGRESS"
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = title.uppercase(),
                    color = AegisWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Black,
            scrolledContainerColor = Color.Black
        )
    )
}