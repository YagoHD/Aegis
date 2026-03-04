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
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel
import com.yago.aegis.viewmodel.WorkoutViewModelFactory

class MainActivity : ComponentActivity() {

    // 1. Instancias únicas de datos
    private val settingsStore by lazy { SettingsStore(applicationContext) }
    private val userRepository by lazy { UserRepository(settingsStore) }

    // 2. Declaramos los ViewModels usando sus Factories (Una sola vez aquí arriba está bien)
    private val profileViewModel: ProfileViewModel by viewModels { ProfileViewModelFactory(userRepository) }
    private val routinesViewModel: RoutinesViewModel by viewModels {
        RoutinesViewModel.RoutinesViewModelFactory(
            userRepository
        )
    }
    private val workoutViewModel: WorkoutViewModel by viewModels { WorkoutViewModelFactory(settingsStore) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AegisTheme {
                // 3. Pasamos los TRES ViewModels a la navegación
                AegisNavigation(
                    profileViewModel = profileViewModel,
                    routinesViewModel = routinesViewModel, // <-- No olvides este
                    workoutViewModel = workoutViewModel
                )
            }
        }
    }
}