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
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

// MainActivity limpia:
// - Todos los ViewModels reciben UserRepository (fuente única de datos)
// - ProfileViewModelFactory.kt eliminado (la Factory vive dentro de ProfileViewModel)
// - StatsViewModel ya no necesita SettingsStore aquí: se crea en AegisNavigation con UserRepository
class MainActivity : ComponentActivity() {

    private val settingsStore by lazy { SettingsStore(applicationContext) }
    private val userRepository by lazy { UserRepository(settingsStore) }

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModel.Factory(userRepository)
    }
    private val routinesViewModel: RoutinesViewModel by viewModels {
        RoutinesViewModel.Factory(userRepository)
    }
    // WorkoutViewModel ahora recibe UserRepository, no SettingsStore
    private val workoutViewModel: WorkoutViewModel by viewModels {
        WorkoutViewModel.Factory(userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AegisTheme {
                AegisNavigation(
                    profileViewModel = profileViewModel,
                    routinesViewModel = routinesViewModel,
                    workoutViewModel = workoutViewModel,
                    userRepository = userRepository
                )
            }
        }
    }
}
