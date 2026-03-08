package com.yago.aegis.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yago.aegis.ui.components.AegisBottomBar
import com.yago.aegis.ui.components.SettingsMenu
import com.yago.aegis.ui.screens.*
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

@Composable
fun AegisNavigation(
    profileViewModel: ProfileViewModel,
    workoutViewModel: WorkoutViewModel,
    routinesViewModel: RoutinesViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val onboardingCompleted by profileViewModel.onboardingCompleted.collectAsState(initial = null)
    if (onboardingCompleted == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        return
    }
    val startDest = if (onboardingCompleted == true) "profile" else "welcome"

    // ✅ CORRECCIÓN 1: Ocultar la BottomBar en Onboarding y Settings
    val onboardingRoutes = listOf("welcome", "identity", "metrics")
    val showBottomBar = currentRoute != "settings" && !onboardingRoutes.contains(currentRoute)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AegisBottomBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(paddingValues)
        ) {

            // --- BLOQUE ONBOARDING ---
            composable("welcome") {
                WelcomeScreen(onContinue = { navController.navigate("identity") })
            }

            composable("identity") {
                IdentityScreen(
                    viewModel = profileViewModel,
                    onContinue = { name, bio, photoUri ->
                        profileViewModel.updateName(name)
                        navController.navigate("metrics")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("metrics") {
                MetricsScreen(onComplete = { height, mass ->
                    profileViewModel.updateHeight(height)
                    profileViewModel.updateMass(mass)
                    profileViewModel.completeOnboarding()

                    // ✅ CORRECCIÓN 2: Ir a profile y limpiar el rastro del onboarding
                    navController.navigate("profile") {
                        popUpTo("welcome") { inclusive = true }
                    }
                })
            }

            // 🏋️ PANTALLA DE MIS RUTINAS (Gestión/Creación)
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
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToEditRoutine = { id ->
                        navController.navigate("edit_routine/$id")
                    }
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

            // 📊 PANTALLA DE EJERCICIOS (Librería)
            composable("ejercicios") {
                ExercisesLibraryScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateToCreate = {
                        navController.navigate("create_exercise")
                    },
                    onNavigateToEdit = { exerciseName ->
                        navController.navigate("edit_exercise/$exerciseName")
                    }
                )
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

            // ✏️ PANTALLA DE EDICIÓN DE RUTINA
            composable(
                route = "edit_routine/{routineId}",
                arguments = listOf(
                    androidx.navigation.navArgument("routineId") {
                        type = androidx.navigation.NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getInt("routineId") ?: -1

                EditRoutineScreen(
                    routineId = routineId,
                    routinesViewModel = routinesViewModel,
                    navController = navController,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ➕ PANTALLA DE AÑADIR EJERCICIO A RUTINA
            composable(route = "add_exercise") {
                AddExerciseScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onExerciseCreated = { /* Lógica de retorno */ }
                )
            }

            // 📝 PANTALLA DE EDICIÓN DE EJERCICIO
            composable("edit_exercise/{exerciseName}") { backStackEntry ->
                val exerciseName = backStackEntry.arguments?.getString("exerciseName")
                val exercise = routinesViewModel.allExercises.collectAsState().value
                    .find { it.name == exerciseName }

                EditExerciseScreen(
                    routinesViewModel = routinesViewModel,
                    exerciseToEdit = exercise,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 🆕 CREAR NUEVO EJERCICIO
            composable("create_exercise") {
                EditExerciseScreen(
                    routinesViewModel = routinesViewModel,
                    exerciseToEdit = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 🎯 SELECCIONAR RUTINA PARA ENTRENAR
            composable("train") {
                SelectRoutineScreen(
                    routinesViewModel = routinesViewModel,
                    workoutViewModel = workoutViewModel,
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToCreateRoutine = {
                        // Navegamos a la sección de gestión de rutinas
                        navController.navigate("routine") {
                            // Esto evita que se acumulen pantallas en la pila
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onStartWorkout = { routineId ->
                        navController.navigate("active_session/$routineId")
                    }
                )
            }

            composable(
                route = "active_session/{routineId}",
                arguments = listOf(
                    androidx.navigation.navArgument("routineId") {
                        type = androidx.navigation.NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getInt("routineId") ?: -1

                // Buscamos la rutina real para pasarla al ViewModel
                val routine = routinesViewModel.routines.find { it.id == routineId }

                // Si la rutina existe, la iniciamos en el WorkoutViewModel
                LaunchedEffect(routineId) {
                    routine?.let { workoutViewModel.startWorkout(it) }
                }

                ActiveSessionScreen(
                    workoutViewModel = workoutViewModel,
                    routinesViewModel = routinesViewModel,
                    profileViewModel = profileViewModel,
                    onFinishWorkout = { navController.popBackStack() }
                )
            }

        }
    }
}