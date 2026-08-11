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
import com.yago.aegis.viewmodel.PanteonViewModel
import com.yago.aegis.viewmodel.PlateCalculatorViewModel
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.StatsViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

private val TAB_ROUTES = listOf(Routes.STATS, Routes.ROUTINE, Routes.TRAIN, Routes.PANTEON, Routes.PROFILE)

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
        !isLoggedIn -> Routes.WELCOME
        !authRepository.isEmailVerified -> Routes.EMAIL_VERIFICATION
        else -> Routes.PROFILE
    }

    val onboardingRoutes = listOf(Routes.WELCOME, Routes.IDENTITY, Routes.METRICS, Routes.REGISTER, Routes.EMAIL_VERIFICATION)
    val authRoutes = listOf(Routes.LOGIN, Routes.WELCOME, Routes.EMAIL_VERIFICATION)
    val isSessionActive = currentRoute?.startsWith("active_session") == true ||
            currentRoute == Routes.CUSTOM_SESSION

    // Redirigir a verificación en cualquier momento si needsEmailVerification es true
    val uiStateGlobal by authViewModel.uiState.collectAsState()
    LaunchedEffect(uiStateGlobal.needsEmailVerification) {
        if (uiStateGlobal.needsEmailVerification) {
            navController.navigate(Routes.EMAIL_VERIFICATION) {
                popUpTo(0) { inclusive = false }
            }
        }
    }
    val sharedStatsViewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory(userRepository))
    val plateCalculatorViewModel: PlateCalculatorViewModel = viewModel(factory = PlateCalculatorViewModel.Factory(userRepository))
    val panteonViewModel: PanteonViewModel = viewModel(factory = PanteonViewModel.Factory(userRepository))

    val showBottomBar = currentRoute != Routes.SETTINGS &&
            !onboardingRoutes.contains(currentRoute) &&
            !authRoutes.contains(currentRoute) &&
            !isSessionActive &&
            currentRoute != Routes.WORKOUT_SETTINGS &&
            currentRoute != Routes.WORKOUT_COMPLETE &&
            currentRoute != Routes.WORKOUT_HISTORY &&
            currentRoute != Routes.PLATE_CALCULATOR &&
            currentRoute != Routes.PRIVACY_POLICY

    Scaffold(
        bottomBar = { if (showBottomBar) AegisBottomBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(paddingValues).imePadding()
        ) {
            composable(
                route = Routes.LOGIN,
                enterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
            ) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = {
                        // Volver a welcome para iniciar el flujo de registro completo
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onLoginSuccess = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.WELCOME) { inclusive = false }
                        }
                    },
                    onRegister = { navController.navigate(Routes.IDENTITY) }
                )
            }
            composable(Routes.IDENTITY) {
                IdentityScreen(
                    viewModel = profileViewModel,
                    onContinue = { name, _, _ ->
                        profileViewModel.updateName(name)
                        navController.navigate(Routes.METRICS)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.METRICS) {
                MetricsScreen(
                    onComplete = { height, mass, sex ->
                        profileViewModel.updateHeight(height)
                        profileViewModel.updateMass(mass)
                        profileViewModel.updateSex(sex)
                        profileViewModel.completeOnboarding()
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.WELCOME) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.EMAIL_VERIFICATION) {
                val email = authViewModel.currentUserEmail
                EmailVerificationScreen(
                    authViewModel = authViewModel,
                    email = email,
                    onVerified = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = {
                        authViewModel.logout()
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Routes.ROUTINE,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                RoutineScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateToEditRoutine = { id -> navController.navigate(Routes.editRoutine(id)) },
                    onNavigateToNewRoutine = { id -> navController.navigate(Routes.editRoutine(id, true)) },
                    onNavigateToExercises = {
                        navController.navigate(Routes.EJERCICIOS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = Routes.STATS,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                StatsScreen(
                    viewModel = sharedStatsViewModel,
                    onNavigateToSettings = { navController.navigate(Routes.STATS_SETTINGS) },
                    onNavigateToExerciseDetail = { exerciseId -> navController.navigate(Routes.exerciseDetail(exerciseId)) },
                    onNavigateToHistory = { navController.navigate(Routes.WORKOUT_HISTORY) }
                )
            }
            composable(
                route = Routes.STATS_SETTINGS,
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) { StatsSettingsScreen(viewModel = sharedStatsViewModel) }

            composable(
                route = Routes.PROFILE,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                MainProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                    onNavigateToTrain = {
                        navController.navigate(Routes.TRAIN) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = Routes.EJERCICIOS,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                ExercisesLibraryScreen(
                    routinesViewModel = routinesViewModel,
                    onNavigateToCreate = { navController.navigate(Routes.CREATE_EXERCISE) },
                    onNavigateToEdit = { exerciseName -> navController.navigate(Routes.editExercise(exerciseName)) },
                    onNavigateToRoutines = {
                        navController.navigate(Routes.ROUTINE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = Routes.PANTEON,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                PanteonScreen(viewModel = panteonViewModel)
            }
            composable(
                route = Routes.EXERCISE_DETAIL,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType }),
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: -1L
                ExerciseDetailScreen(exerciseId = exerciseId, viewModel = sharedStatsViewModel, onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.SETTINGS,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                SettingsMenu(
                    viewModel = profileViewModel,
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.WELCOME) { popUpTo(0) { inclusive = true } }
                    },
                    onAccountDeleted = {
                        navController.navigate(Routes.WELCOME) { popUpTo(0) { inclusive = true } }
                    },
                    onNavigateToPrivacy = { navController.navigate(Routes.PRIVACY_POLICY) }
                )
            }

            composable(
                route = Routes.EDIT_ROUTINE,
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
                route = Routes.ADD_EXERCISE,
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
                route = Routes.EDIT_EXERCISE,
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) { backStackEntry ->
                val exerciseName = backStackEntry.arguments?.getString("exerciseName")
                val exercise = routinesViewModel.allExercises.collectAsState().value.find { it.name == exerciseName }
                EditExerciseScreen(routinesViewModel = routinesViewModel, exerciseToEdit = exercise, onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.CREATE_EXERCISE,
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) {
                EditExerciseScreen(routinesViewModel = routinesViewModel, exerciseToEdit = null, onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.TRAIN,
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                SelectRoutineScreen(
                    routinesViewModel = routinesViewModel,
                    workoutViewModel = workoutViewModel,
                    onNavigateToCreateRoutine = {
                        navController.navigate(Routes.ROUTINE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onStartWorkout = { routineId -> navController.navigate(Routes.activeSession(routineId)) },
                    onNavigateToPlateCalculator = { navController.navigate(Routes.PLATE_CALCULATOR) },
                    onResumeSession = {
                        val id = workoutViewModel.activeRoutineId.value
                        if (id != null) navController.navigate(Routes.activeSession(id))
                        else navController.navigate(Routes.CUSTOM_SESSION)
                    },
                    onStartCustomWorkout = { name ->
                        workoutViewModel.startCustomWorkout(name)
                        navController.navigate(Routes.CUSTOM_SESSION)
                    }
                )
            }

            composable(
                route = Routes.ACTIVE_SESSION,
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
                        navController.navigate(Routes.WORKOUT_COMPLETE) {
                            popUpTo(Routes.ACTIVE_SESSION) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = { navController.navigate(Routes.WORKOUT_SETTINGS) },
                    onNavigateToPlateCalculator = { navController.navigate(Routes.PLATE_CALCULATOR) },
                    onBack = { navController.popBackStack() }
                )
            }

            // Entrenamiento libre: la sesión ya se creó con startCustomWorkout antes de navegar
            composable(Routes.CUSTOM_SESSION) {
                ActiveSessionScreen(
                    workoutViewModel = workoutViewModel,
                    routinesViewModel = routinesViewModel,
                    profileViewModel = profileViewModel,
                    onFinishWorkout = {
                        navController.navigate(Routes.WORKOUT_COMPLETE) {
                            popUpTo(Routes.CUSTOM_SESSION) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = { navController.navigate(Routes.WORKOUT_SETTINGS) },
                    onNavigateToPlateCalculator = { navController.navigate(Routes.PLATE_CALCULATOR) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.WORKOUT_COMPLETE) {
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
                            navController.navigate(Routes.PROFILE) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToHistory = { navController.navigate(Routes.WORKOUT_HISTORY) }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }

            composable(
                route = Routes.WORKOUT_HISTORY,
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
                route = Routes.WORKOUT_SETTINGS,
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
                route = Routes.PLATE_CALCULATOR,
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) {
                PlateCalculatorScreen(
                    viewModel = plateCalculatorViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.PRIVACY_POLICY,
                enterTransition = pushEnter, exitTransition = pushExit,
                popEnterTransition = pushPopEnter, popExitTransition = pushPopExit
            ) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
