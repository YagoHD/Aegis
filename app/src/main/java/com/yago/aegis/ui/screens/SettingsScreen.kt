package com.yago.aegis.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.SettingsMenu
import com.yago.aegis.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ProfileViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            // El componente AegisTopBar ya incluye el HorizontalDivider
            // y el containerColor en 'surface' (121212)
            AegisTopBar(
                title = "CONFIGURACIÓN",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            // Usamos onBackground para que sea un blanco nítido
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        // 60%: BackgroundBlack (050505) - El vacío profundo
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Box que contiene el menú, asegurando que el contenido empiece
        // justo después del separador de la TopBar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsMenu(viewModel)
        }
    }
}