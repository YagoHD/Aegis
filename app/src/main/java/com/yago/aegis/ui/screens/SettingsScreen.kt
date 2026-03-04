package com.yago.aegis.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.SettingsMenu
import com.yago.aegis.ui.theme.AegisWhite
import com.yago.aegis.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ProfileViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            // ✅ Sustitución por el componente unificado del Canvas
            AegisTopBar(
                title = "CONFIGURACIÓN",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            // Nota: Puedes usar Icons.Default o Icons.AutoMirrored
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = AegisWhite
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            SettingsMenu(viewModel)
        }
    }
}