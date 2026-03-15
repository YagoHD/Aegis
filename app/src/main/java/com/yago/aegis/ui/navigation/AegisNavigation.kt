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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yago.aegis.data.FirebaseAuthRepository
import com.yago.aegis.data.UserRepository
import com.yago.aegis.ui.components.AegisBottomBar
import com.yago.aegis.ui.components.SettingsMenu
import com.yago.aegis.ui.screens.*
import com.yago.aegis.viewmodel.AuthViewModel
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.StatsViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

@Composable
fun AegisNavigation(
    profileViewModel: ProfileViewModel,
    workoutViewModel: WorkoutViewModel,
    routinesViewModel: RoutinesViewModel,
    userRepository: UserRepository,
    authRepository: FirebaseAuthRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val onboardingCompleted by profileViewModel.onboardingCompleted.collectAsState(initial = null)
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(authRepository, userRepository))

    if (onboardingCompleted == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        return
    }

    val startDest = when {
        onboardingCompleted == true && authViewModel.isLoggedIn -> "profile"
        onboardingCompleted == true && !authViewModel.isLoggedIn -> "login"
        else -> "welcome"
    }

    val onboardingRoutes = listOf("welcome", "identity", "metrics", "register")
    val authRoutes = listOf("login")
    val isSessionActive = currentRoute?.startsWith("active_session") == true
    val sharedStatsViewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory(userRepository))

    val showBottomBar = currentRoute != "settings" &&
            !onboardingRoutes.contains(currentRoute) &&
            !authRoutes.contains(currentRoute) &&
            !isSessionActive &&
            currentRoute != "workout_settings"

    Scaffold(
        bottomBar = { if (showBottomBar) AegisBottomBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(
                route = "login",
                enterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
            ) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = {
                        navController.navigate("profile") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("welcome") {
                WelcomeScreen(onContinue = { navController.navigate("identity") })
            }
            composable("identity") {
                IdentityScreen(
                    viewModel = profileViewModel,
                    onContinue = { name, _, _ ->
                        profileViewModel.updateName(name)
                        navController.navigate("metrics")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("metrics") {
                MetricsScreen(
                    onComplete = { height, mass ->
                        profileViewModel.updateHeight(height)
                        profileViewModel.updateMass(mass)
                        profileViewModel.completeOnboarding()
                        navController.navigate("register") {
                            popUpTo("welcome") { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("register") {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate("profile") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "routine",
                enterTransition = { slideIntoContainer(animationSpec = tween(300), towards = AnimatedContentTransitionScope.SlideDirection.Right) },
                exitTransition = { slideOutOfContainer(animationSpec = tween(300), towards = AnimatedContentTransitionScope.SlideDirection.Left) }
            ) {
                RoutineScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateToEditRoutine = { id -> navController.navigate("edit_routine/$id") }
                )
            }

            composable("stats") {
                StatsScreen(
                    viewModel = sharedStatsViewModel,
                    onNavigateToSettings = { navController.navigate("stats_settings") },
                    onNavigateToExerciseDetail = { exerciseId -> navController.navigate("exercise_detail/$exerciseId") }
                )
            }
            composable("stats_settings") { StatsSettingsScreen(viewModel = sharedStatsViewModel) }

            composable(
                route = "profile",
                enterTransition = { slideIntoContainer(animationSpec = tween(300), towards = AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(animationSpec = tween(300), towards = AnimatedContentTransitionScope.SlideDirection.Right) }
            ) {
                MainProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("ejercicios") {
                ExercisesLibraryScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateToCreate = { navController.navigate("create_exercise") },
                    onNavigateToEdit = { exerciseName -> navController.navigate("edit_exercise/$exerciseName") }
                )
            }
            composable(
                route = "exercise_detail/{exerciseId}",
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: -1L
                ExerciseDetailScreen(exerciseId = exerciseId, viewModel = sharedStatsViewModel, onBack = { navController.popBackStack() })
            }

            composable(
                route = "settings",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                SettingsMenu(
                    viewModel = profileViewModel,
                    authViewModel = authViewModel,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "edit_routine/{routineId}",
                arguments = listOf(navArgument("routineId") { type = NavType.IntType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getInt("routineId") ?: -1
                EditRoutineScreen(routineId = routineId, routinesViewModel = routinesViewModel, navController = navController, onNavigateBack = { navController.popBackStack() })
            }

            composable(route = "add_exercise") {
                AddExerciseScreen(routinesViewModel = routinesViewModel, onNavigateBack = { navController.popBackStack() }, onExerciseCreated = {})
            }

            composable("edit_exercise/{exerciseName}") { backStackEntry ->
                val exerciseName = backStackEntry.arguments?.getString("exerciseName")
                val exercise = routinesViewModel.allExercises.collectAsState().value.find { it.name == exerciseName }
                EditExerciseScreen(routinesViewModel = routinesViewModel, exerciseToEdit = exercise, onNavigateBack = { navController.popBackStack() })
            }

            composable("create_exercise") {
                EditExerciseScreen(routinesViewModel = routinesViewModel, exerciseToEdit = null, onNavigateBack = { navController.popBackStack() })
            }

            composable("train") {
                SelectRoutineScreen(
                    routinesViewModel = routinesViewModel,
                    workoutViewModel = workoutViewModel,
                    onNavigateToCreateRoutine = {
                        navController.navigate("routine") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onStartWorkout = { routineId -> navController.navigate("active_session/$routineId") }
                )
            }

            composable(
                route = "active_session/{routineId}",
                arguments = listOf(navArgument("routineId") { type = NavType.IntType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getInt("routineId") ?: -1
                val routine = routinesViewModel.routines.find { it.id == routineId }
                LaunchedEffect(routineId) { routine?.let { workoutViewModel.startWorkout(it) } }
                ActiveSessionScreen(
                    workoutViewModel = workoutViewModel,
                    routinesViewModel = routinesViewModel,
                    profileViewModel = profileViewModel,
                    onFinishWorkout = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate("workout_settings") }
                )
            }

            composable("workout_settings") {
                WorkoutSettingsScreen(
                    workoutViewModel = workoutViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSave = { seconds, vibrate, sound, showTimer ->
                        userRepository.updateRestTimerSeconds(seconds)
                        userRepository.updateTimerVibrate(vibrate)
                        userRepository.updateTimerSound(sound)
                        userRepository.updateShowRestTimer(showTimer)
                    }
                )
            }
        }
    }
}
