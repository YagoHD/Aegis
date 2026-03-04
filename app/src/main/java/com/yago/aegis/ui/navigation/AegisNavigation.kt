package com.yago.aegis.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.yago.aegis.viewmodel.WorkoutViewModel

@Composable
fun AegisNavigation(profileViewModel: ProfileViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val workoutViewModel: WorkoutViewModel = viewModel()
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

            // 📊 PANTALLA DE EJERCICIOS
            composable("ejercicios") {
                ExercisesLibraryScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateToCreate = {
                        navController.navigate("create_exercise")
                    },
                    onNavigateToEdit = { exerciseName ->
                        // ✅ Navegamos a la ruta de edición con el nombre como parámetro
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

            composable(route = "add_exercise") {
                AddExerciseScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onExerciseCreated = { newExercise ->
                        // Aquí es donde el ejercicio vuelve a la pantalla de edición.
                        // Como estamos usando el mismo routinesViewModel,
                        // el ejercicio se guardará correctamente.
                    }
                )
            }
            composable("edit_exercise/{exerciseName}") { backStackEntry ->
                val exerciseName = backStackEntry.arguments?.getString("exerciseName")
                // Buscamos el ejercicio en la lista para pasarlo a la pantalla
                val exercise = routinesViewModel.allExercises.collectAsState().value
                    .find { it.name == exerciseName }

                EditExerciseScreen(
                    routinesViewModel = routinesViewModel,
                    exerciseToEdit = exercise, // Pasamos el ejercicio para "Editar"
                    onNavigateBack = { navController.popBackStack() }
                )
            }

// O una ruta simple para crear
            composable("create_exercise") {
                EditExerciseScreen(
                    routinesViewModel = routinesViewModel,
                    exerciseToEdit = null, // Al ser null, la pantalla se abre vacía para "Crear"
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("train") {
                SelectRoutineScreen(
                    routinesViewModel = routinesViewModel,
                    workoutViewModel = workoutViewModel, // Debes tener este ViewModel inyectado o creado arriba
                    onNavigateToSettings = {
                        navController.navigate("settings") // O la ruta que uses para ajustes
                    },
                    onNavigateToCreateRoutine = {
                        navController.navigate("routine")
                    },
                    onStartWorkout = { routineId ->
                        // Por ahora solo imprimimos para probar que el botón funciona
                        println("Empezando rutina: $routineId")
                        // Más adelante: navController.navigate("active_session/$routineId")
                    }
                )
            }
        }
    }
}