package com.yago.aegis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yago.aegis.data.FirebaseAuthRepository
import com.yago.aegis.data.SettingsStore
import com.yago.aegis.data.UserRepository
import com.yago.aegis.ui.navigation.AegisNavigation
import com.yago.aegis.ui.theme.AegisTheme
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {

    private val settingsStore by lazy { SettingsStore(applicationContext) }
    private val userRepository by lazy { UserRepository(settingsStore) }
    private val authRepository by lazy { FirebaseAuthRepository() }

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModel.Factory(userRepository)
    }
    private val routinesViewModel: RoutinesViewModel by viewModels {
        RoutinesViewModel.Factory(userRepository)
    }
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
                    userRepository = userRepository,
                    authRepository = authRepository
                )
            }
        }
    }
}
