package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.theme.AegisWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AegisTopBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.height(48.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Text(
                text = title.uppercase(),
                color = AegisWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Black,
            scrolledContainerColor = Color.Black
        )
    )
}