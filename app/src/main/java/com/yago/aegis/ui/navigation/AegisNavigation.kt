package com.yago.aegis.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.yago.aegis.viewmodel.PlateCalculatorViewModel
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.StatsViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

private val TAB_ROUTES = listOf("stats", "routine", "train", "panteon", "profile")

private val tabEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    val from = TAB_ROUTES.indexOf(initialState.destination.route)
    val to = TAB_ROUTES.indexOf(targetState.destination.route)
    if (from == -1 || to == -1) EnterTransition.None
    else slideInHorizontally(tween(220)) { if (to > from) it else -it }
}

private val tabExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    val from = TAB_ROUTES.indexOf(initialState.destination.route)
    val to = TAB_ROUTES.indexOf(targetState.destination.route)
    if (from == -1 || to == -1) ExitTransition.None
    else slideOutHorizontally(tween(220)) { if (to > from) -it else it }
}

private val pushEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(tween(250)) { it }
}
private val pushExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(tween(250)) { -it }
}
private val pushPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(tween(250)) { -it }
}
private val pushPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(tween(250)) { it }
}

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
    val application = LocalContext.current.applicationContext as Application
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(application, authRepository, userRepository))

    if (onboardingCompleted == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        return
    }
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val startDest = when {
        !isLoggedIn -> "welcome"
        !authRepository.isEmailVerified -> "email_verification"
        else -> "profile"
    }

    val onboardingRoutes = listOf("welcome", "identity", "metrics", "register", "email_verification")
    val authRoutes = listOf("login", "welcome", "email_verification")
    val isSessionActive = currentRoute?.startsWith("active_session") == true ||
            currentRoute == "custom_session"

    // Redirigir a verificación en cualquier momento si needsEmailVerification es true
    val uiStateGlobal by authViewModel.uiState.collectAsState()
    LaunchedEffect(uiStateGlobal.needsEmailVerification) {
        if (uiStateGlobal.needsEmailVerification) {
            navController.navigate("email_verification") {
                popUpTo(0) { inclusive = false }
            }
        }
    }
    val sharedStatsViewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory(userRepository))
    val plateCalculatorViewModel: PlateCalculatorViewModel = viewModel(factory = PlateCalculatorViewModel.Factory(userRepository))

    val showBottomBar = currentRoute != "settings" &&
            !onboardingRoutes.contains(currentRoute) &&
            !authRoutes.contains(currentRoute) &&
            !isSessionActive &&
            currentRoute != "workout_settings" &&
            currentRoute != "workout_complete" &&
            currentRoute != "workout_history" &&
            currentRoute != "plate_calculator" &&
            currentRoute != "privacy_policy"

    Scaffold(
        bottomBar = { if (showBottomBar) AegisBottomBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(paddingValues).imePadding()
        ) {
            composable(
                route = "login",
                enterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
            ) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = {
                        // Volver a welcome para iniciar el flujo de registro completo
                        navController.navigate("welcome") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onLoginSuccess = {
                        navController.navigate("profile") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("welcome") {
                WelcomeScreen(
                    onLogin = {
                        navController.navigate("login") {
                            popUpTo("welcome") { inclusive = false }
                        }
                    },
                    onRegister = { navController.navigate("identity") }
                )
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
                    onComplete = { height, mass, sex ->
                        profileViewModel.updateHeight(height)
                        profileViewModel.updateMass(mass)
                        profileViewModel.updateSex(sex)
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
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("email_verification") {
                val email = authViewModel.currentUserEmail
                EmailVerificationScreen(
                    authViewModel = authViewModel,
                    email = email,
                    onVerified = {
                        navController.navigate("profile") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = {
                        authViewModel.logout()
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "routine",
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                RoutineScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateToEditRoutine = { id -> navController.navigate("edit_routine/$id") },
                    onNavigateToNewRoutine = { id -> navController.navigate("edit_routine/$id?isNew=true") },
                    onNavigateToExercises = {
                        navController.navigate("ejercicios") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = "stats",
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                StatsScreen(
                    viewModel = sharedStatsViewModel,
                    onNavigateToSettings = { navController.navigate("stats_settings") },
                    onNavigateToExerciseDetail = { exerciseId -> navController.navigate("exercise_detail/$exerciseId") },
                    onNavigateToHistory = { navController.navigate("workout_history") }
                )
            }
            composable(
                route = "stats_settings",
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) { StatsSettingsScreen(viewModel = sharedStatsViewModel) }

            composable(
                route = "profile",
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                MainProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToTrain = {
                        navController.navigate("train") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = "ejercicios",
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                ExercisesLibraryScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateToCreate = { navController.navigate("create_exercise") },
                    onNavigateToEdit = { exerciseName -> navController.navigate("edit_exercise/$exerciseName") },
                    onNavigateToRoutines = {
                        navController.navigate("routine") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = "panteon",
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                PanteonScreen()
            }
            composable(
                route = "exercise_detail/{exerciseId}",
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType }),
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
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
                        authViewModel.logout()
                        navController.navigate("welcome") { popUpTo(0) { inclusive = true } }
                    },
                    onAccountDeleted = {
                        navController.navigate("welcome") { popUpTo(0) { inclusive = true } }
                    },
                    onNavigateToPrivacy = { navController.navigate("privacy_policy") }
                )
            }

            composable(
                route = "edit_routine/{routineId}?isNew={isNew}",
                arguments = listOf(
                    navArgument("routineId") { type = NavType.IntType },
                    navArgument("isNew") { type = NavType.BoolType; defaultValue = false }
                ),
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getInt("routineId") ?: -1
                val isNew = backStackEntry.arguments?.getBoolean("isNew") ?: false
                EditRoutineScreen(
                    routineId = routineId,
                    routinesViewModel = routinesViewModel,
                    navController = navController,
                    isNewRoutine = isNew,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "add_exercise?slotIndex={slotIndex}",
                arguments = listOf(navArgument("slotIndex") { type = NavType.IntType; defaultValue = -1 }),
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) { backStackEntry ->
                val slotIndex = backStackEntry.arguments?.getInt("slotIndex") ?: -1
                AddExerciseScreen(
                    routinesViewModel = routinesViewModel,
                    slotIndex = slotIndex,
                    onNavigateBack = { navController.popBackStack() },
                    onExerciseCreated = {}
                )
            }

            composable(
                route = "edit_exercise/{exerciseName}",
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) { backStackEntry ->
                val exerciseName = backStackEntry.arguments?.getString("exerciseName")
                val exercise = routinesViewModel.allExercises.collectAsState().value.find { it.name == exerciseName }
                EditExerciseScreen(routinesViewModel = routinesViewModel, exerciseToEdit = exercise, onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = "create_exercise",
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) {
                EditExerciseScreen(routinesViewModel = routinesViewModel, exerciseToEdit = null, onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = "train",
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
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
                    onStartWorkout = { routineId -> navController.navigate("active_session/$routineId") },
                    onNavigateToPlateCalculator = { navController.navigate("plate_calculator") },
                    onResumeSession = {
                        val id = workoutViewModel.activeRoutineId.value
                        if (id != null) navController.navigate("active_session/$id")
                        else navController.navigate("custom_session")
                    },
                    onStartCustomWorkout = { name ->
                        workoutViewModel.startCustomWorkout(name)
                        navController.navigate("custom_session")
                    }
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
                    onFinishWorkout = {
                        navController.navigate("workout_complete") {
                            popUpTo("active_session/{routineId}") { inclusive = true }
                        }
                    },
                    onNavigateToSettings = { navController.navigate("workout_settings") },
                    onNavigateToPlateCalculator = { navController.navigate("plate_calculator") },
                    onBack = { navController.popBackStack() }
                )
            }

            // Entrenamiento libre: la sesión ya se creó con startCustomWorkout antes de navegar
            composable("custom_session") {
                ActiveSessionScreen(
                    workoutViewModel = workoutViewModel,
                    routinesViewModel = routinesViewModel,
                    profileViewModel = profileViewModel,
                    onFinishWorkout = {
                        navController.navigate("workout_complete") {
                            popUpTo("custom_session") { inclusive = true }
                        }
                    },
                    onNavigateToSettings = { navController.navigate("workout_settings") },
                    onNavigateToPlateCalculator = { navController.navigate("plate_calculator") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("workout_complete") {
                val summary = workoutViewModel.workoutSummary.collectAsState().value
                val allHistory by sharedStatsViewModel.workoutHistory.collectAsState()

                if (summary != null) {
                    // Buscar el volumen de la penúltima sesión con el mismo nombre de rutina
                    val previousVolume = allHistory
                        .filter { it.routineName == summary.routineName }
                        .dropLast(1) // Quitar la sesión que acabamos de guardar
                        .lastOrNull()
                        ?.exercisesProgress
                        ?.sumOf { prog -> prog.sets.filter { it.isCompleted }.sumOf { it.weight * it.reps } }
                        ?: 0.0

                    WorkoutCompleteScreen(
                        summary = summary,
                        previousVolume = previousVolume,
                        onFinish = { notes ->
                            workoutViewModel.saveSessionNotes(notes)
                            workoutViewModel.clearSummary()
                            navController.navigate("profile") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToHistory = { navController.navigate("workout_history") }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("profile") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }

            composable(
                route = "workout_history",
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) {
                val history by sharedStatsViewModel.workoutHistory.collectAsState()
                WorkoutHistoryScreen(
                    sessions = history,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "workout_settings",
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) {
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

            composable(
                route = "plate_calculator",
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) {
                PlateCalculatorScreen(
                    viewModel = plateCalculatorViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "privacy_policy",
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
