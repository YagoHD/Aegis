package com.yago.aegis.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yago.aegis.ui.components.AegisBottomBar
import com.yago.aegis.ui.components.SettingsMenu
import com.yago.aegis.ui.screens.*
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel

@Composable
fun AegisNavigation(profileViewModel: ProfileViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ✅ EXTRAEMOS EL REPOSITORIO Y CREAMOS EL ROUTINESVIEWMODEL CON FACTORY
    val repository = profileViewModel.repository
    val routinesViewModel: RoutinesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RoutinesViewModel(repository) as T
            }
        }
    )

    // No mostramos la barra en settings para que la configuración ocupe toda la pantalla
    val showBottomBar = currentRoute != "settings"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AegisBottomBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "profile",
            modifier = Modifier.padding(paddingValues)
        ) {
            // 🏋️ PANTALLA DE MIS RUTINAS
            composable(
                route = "routine",
                enterTransition = {
                    slideIntoContainer(
                        animationSpec = tween(300),
                        towards = AnimatedContentTransitionScope.SlideDirection.Right
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        animationSpec = tween(300),
                        towards = AnimatedContentTransitionScope.SlideDirection.Left
                    )
                }
            ) {
                RoutineScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            // 📅 PANTALLA DE DISCIPLINA SEMANAL
            composable("weekly") {
                // WeeklyScreen(profileViewModel)
            }

            // 👤 PANTALLA DE PERFIL
            composable(
                route = "profile",
                enterTransition = {
                    slideIntoContainer(
                        animationSpec = tween(300),
                        towards = AnimatedContentTransitionScope.SlideDirection.Left
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        animationSpec = tween(300),
                        towards = AnimatedContentTransitionScope.SlideDirection.Right
                    )
                }
            ) {
                MainProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            // 📊 PANTALLA DE ESTADÍSTICAS
            composable("stats") {
                // StatsScreen(profileViewModel)
            }

            // ⚙️ PANTALLA DE AJUSTES
            composable(
                route = "settings",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                SettingsMenu(
                    viewModel = profileViewModel,
                )
            }
        }
    }
}