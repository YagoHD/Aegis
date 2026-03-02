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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yago.aegis.ui.components.AegisBottomBar
import com.yago.aegis.ui.components.SettingsMenu
import com.yago.aegis.ui.screens.*
import com.yago.aegis.viewmodel.ProfileViewModel

@Composable
fun AegisNavigation(viewModel: ProfileViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate("settings") } // ✅ Añade esto para corregir el error
                )
            }

            // Pantalla de Disciplina Semanal (Próximamente)
            composable("weekly") {
                // WeeklyScreen(viewModel)
            }

            // Tu pantalla de Perfil actual
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
                    viewModel = viewModel,
                    // ✅ ESTA LÍNEA ES LA QUE HACE QUE EL BOTÓN FUNCIONE:
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            // Pantalla de Estadísticas/Gráficas (Próximamente)
            composable("stats") {
                // StatsScreen(viewModel)
            }

            // Pantalla de Ajustes con animaciones premium
            composable(
                route = "settings",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                SettingsMenu(
                    viewModel = viewModel,
                )
            }
        }
    }
}