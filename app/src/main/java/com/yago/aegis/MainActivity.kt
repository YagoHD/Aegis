package com.yago.aegis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yago.aegis.data.SettingsStore
import com.yago.aegis.data.UserRepository
import com.yago.aegis.ui.navigation.AegisNavigation
import com.yago.aegis.ui.theme.AegisTheme
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.ProfileViewModelFactory

class MainActivity : ComponentActivity() {

    // Mantenemos la inicialización de datos aquí
    private val settingsStore by lazy { SettingsStore(applicationContext) }
    private val userRepository by lazy { UserRepository(settingsStore) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita que el contenido se vea detrás de la barra de estado y navegación
        enableEdgeToEdge()

        val viewModel: ProfileViewModel by viewModels {
            ProfileViewModelFactory(userRepository)
        }

        setContent {
            AegisTheme {
                // Solo llamamos al componente de navegación global
                // Él se encargará de decidir qué pantalla mostrar
                AegisNavigation(profileViewModel = viewModel)
            }
        }
    }
}