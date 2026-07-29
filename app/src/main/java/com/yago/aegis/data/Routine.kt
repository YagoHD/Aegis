package com.yago.aegis.data

data class Routine(
    val id: Int,
    val name: String,
    val exercises: List<Exercise> = emptyList(),          // Kept for Gson backward compat
    val exerciseSlots: List<ExerciseSlot> = emptyList(),  // New: replaces exercises
    val iconName: String? = "dumbbell",
    val lastCompletedDates: List<Long>? = emptyList()
)

/**
 * Gson bypasea los default values de Kotlin y puede inyectar null en campos non-null.
 * Llamar esto al cargar desde DataStore garantiza que los campos List nunca sean null.
 */
@Suppress("SENSELESS_COMPARISON")
fun Routine.withSafeDefaults() = copy(
    exercises = if (exercises == null) emptyList() else exercises,
    exerciseSlots = if (exerciseSlots == null) emptyList() else exerciseSlots
)

/**
 * Devuelve los slots efectivos de la rutina.
 * Rutinas antiguas (sin exerciseSlots) se migran automáticamente: cada ejercicio → un slot con 1 variante.
 */
@Suppress("SENSELESS_COMPARISON")
fun Routine.effectiveSlots(): List<ExerciseSlot> {
    val safeSlots = if (exerciseSlots == null) emptyList() else exerciseSlots
    val safeExercises = if (exercises == null) emptyList() else exercises
    val slots = if (safeSlots.isNotEmpty()) safeSlots
    else safeExercises.map { ex -> ExerciseSlot(variants = listOf(ex)) }
    // Descarta slots sin variantes (datos corruptos/Gson) para no romper `.first()` aguas abajo
    // (arranque de sesión, guardado de rutina). Un slot vacío no aporta nada.
    return slots.filter { it.variants != null && it.variants.isNotEmpty() }
}