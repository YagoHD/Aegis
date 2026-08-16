package com.yago.aegis

import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.yago.aegis.ui.navigation.AegisNavigation
import com.yago.aegis.ui.theme.AegisTheme
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {

    // US-05: las dependencias vienen del AppContainer (creado en AegisApplication),
    // no se instancian aquí. Los ViewModels siguen Activity-scoped para conservar el
    // estado compartido de la sesión activa y sobrevivir a cambios de configuración.
    private val container get() = (application as AegisApplication).container

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModel.Factory(container.userRepository)
    }
    private val routinesViewModel: RoutinesViewModel by viewModels {
        RoutinesViewModel.Factory(container.userRepository)
    }
    private val workoutViewModel: WorkoutViewModel by viewModels {
        WorkoutViewModel.Factory(application, container.userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // WindowCompat permite que el contenido suba con el teclado
        // compatible con adjustResize en el Manifest
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AegisTheme {
                AegisNavigation(
                    profileViewModel = profileViewModel,
                    routinesViewModel = routinesViewModel,
                    workoutViewModel = workoutViewModel,
                    userRepository = container.userRepository,
                    authRepository = container.authRepository,
                    socialDataSource = container.socialDataSource
                )
            }
        }
    }
}
