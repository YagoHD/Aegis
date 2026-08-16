package com.yago.aegis.ui.navigation

import android.net.Uri

/**
 * Rutas de navegación CENTRALIZADAS (US-07). Un único punto de verdad para los nombres de ruta:
 * evita strings repetidos y typos entre la definición del `composable` y las llamadas a `navigate`.
 * Las rutas con argumentos exponen la plantilla (constante) para definirlas y una función tipada
 * para construir la ruta concreta al navegar.
 */
object Routes {
    // Onboarding / autenticación
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val IDENTITY = "identity"
    const val METRICS = "metrics"
    const val REGISTER = "register"
    const val EMAIL_VERIFICATION = "email_verification"

    // Pestañas de la barra inferior
    const val PROFILE = "profile"
    const val ROUTINE = "routine"
    const val TRAIN = "train"
    const val EJERCICIOS = "ejercicios"
    const val STATS = "stats"
    const val PANTEON = "panteon"

    // Pantallas superpuestas
    const val SETTINGS = "settings"
    const val STATS_SETTINGS = "stats_settings"
    const val PRIVACY_POLICY = "privacy_policy"
    const val PLATE_CALCULATOR = "plate_calculator"
    const val WORKOUT_SETTINGS = "workout_settings"
    const val WORKOUT_COMPLETE = "workout_complete"
    const val WORKOUT_HISTORY = "workout_history"
    const val CUSTOM_SESSION = "custom_session"
    const val CREATE_EXERCISE = "create_exercise"
    const val FRIENDS = "friends"

    // Rutas con argumentos: plantilla (definición) + builder tipado (navegación)
    const val ACTIVE_SESSION = "active_session/{routineId}"
    fun activeSession(routineId: Int) = "active_session/$routineId"

    const val EXERCISE_DETAIL = "exercise_detail/{exerciseId}"
    fun exerciseDetail(exerciseId: Long) = "exercise_detail/$exerciseId"

    const val EDIT_EXERCISE = "edit_exercise/{exerciseName}"
    // El nombre puede contener '/', '?', '#'… que romperían el matching de un único segmento.
    // Se codifica al construir la ruta; Navigation lo decodifica al leer el argumento (round-trip).
    fun editExercise(exerciseName: String) = "edit_exercise/${Uri.encode(exerciseName)}"

    const val EDIT_ROUTINE = "edit_routine/{routineId}?isNew={isNew}"
    // isNew=null -> sin query (usa el default del navArgument); si no, lo incluye explícito.
    fun editRoutine(routineId: Int, isNew: Boolean? = null) =
        if (isNew == null) "edit_routine/$routineId" else "edit_routine/$routineId?isNew=$isNew"

    const val ADD_EXERCISE = "add_exercise?slotIndex={slotIndex}"
    fun addExercise(slotIndex: Int) = "add_exercise?slotIndex=$slotIndex"
}
