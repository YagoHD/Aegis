package com.yago.aegis.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

/**
 * TU CLASE ORIGINAL: La definición base del ejercicio.
 */
data class Exercise(
    val name: String,
    val type: String,        // Ejemplo: "COMPOUND" o "MACHINE"
    val muscleGroup: String, // Ejemplo: "LEGS"
    val id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
    val tags: List<String> = emptyList(),
    val iconName: String = "dumbbell",
    val notes: String = "",
    val isBodyweight: Boolean = false,
    val lastPerformance: String = "",
    val oneRepMax: Double = 0.0,    // Para mostrar "1RM: 115KG" en la lista
    val bestSet: String? = "--",     // Para mostrar el mejor récord histórico
    val history: List<ExerciseRecord> = emptyList(),
    // Contribución a subgrupos musculares para el Panteón (competitivo). Vacío/null = no puntúa.
    // NULLABLE a propósito: Gson deja null los campos nuevos en datos antiguos, y un campo
    // no-nulo haría petar Exercise.copy() (valida no-null). Nullable lo tolera en toda la app.
    val muscleContributions: List<MuscleContribution>? = emptyList(),
    // Tipo de carga: "NORMAL" | "BODYWEIGHT" | "ASSISTED". null = derivar de isBodyweight (retrocompat Gson).
    val loadType: String? = null
)

/** Cómo se carga un ejercicio: con peso externo, con el peso corporal, o asistido (resta). */
enum class LoadType { NORMAL, BODYWEIGHT, ASSISTED }

/** Resuelve el tipo de carga efectivo (cae en isBodyweight para datos antiguos sin loadType). */
fun Exercise.resolveLoadType(): LoadType = when (loadType) {
    "BODYWEIGHT" -> LoadType.BODYWEIGHT
    "ASSISTED" -> LoadType.ASSISTED
    "NORMAL" -> LoadType.NORMAL
    else -> if (isBodyweight) LoadType.BODYWEIGHT else LoadType.NORMAL
}

/**
 * Peso EFECTIVO de una serie según el tipo de carga.
 * - NORMAL: el peso tecleado ([modifier] es el propio peso).
 * - BODYWEIGHT: peso corporal + lastre.
 * - ASSISTED: peso corporal − asistencia (mínimo 0).
 */
fun effectiveWeight(type: LoadType, bodyweight: Double, modifier: Double): Double = when (type) {
    LoadType.NORMAL -> modifier
    LoadType.BODYWEIGHT -> (bodyweight + modifier).coerceAtLeast(0.0)
    LoadType.ASSISTED -> (bodyweight - modifier).coerceAtLeast(0.0)
}

data class ExerciseRecord(
    val date: Long,
    val weight: Double,
    val reps: Int,
    val oneRepMax: Double
)

/**
 * SLOT: Un hueco en la rutina que puede tener uno o más ejercicios alternativos.
 * El usuario elige en tiempo de entreno cuál hace ese día.
 */
data class ExerciseSlot(
    val id: String = java.util.UUID.randomUUID().toString(),
    val variants: List<Exercise> = emptyList()
)

/**
 * EL REGISTRO: Lo que el usuario anota en cada serie durante el entreno.
 */
data class ExerciseSet(
    val id: String = UUID.randomUUID().toString(),
    val reps: Int = 0,
    val weight: Double = 0.0,        // peso EFECTIVO (en corporal/asistido ya incluye el peso corporal)
    val isCompleted: Boolean = false,
    val loadModifier: Double = 0.0   // lastre (+) o asistencia introducidos por el usuario; el signo lo da el loadType
)

/**
 * EL VÍNCULO: Une un ejercicio con las series que se están haciendo en este momento.
 */
data class ExerciseProgress(
    val exercise: Exercise,
    val sets: List<ExerciseSet> = listOf(ExerciseSet()),
    // Todas las variantes del slot (incluida la actual). Vacío = ejercicio sin variantes.
    val slotVariants: List<Exercise> = emptyList()
)

/**
 * LA SESIÓN: El contenedor global de un día de entrenamiento.
 */
data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val routineName: String,
    val date: Long = System.currentTimeMillis(),
    val exercisesProgress: List<ExerciseProgress>,
    val notes: String = ""  // Default "" — sesiones antiguas sin este campo no se rompen
)

// ICONOS Y UTILIDADES (Mantenemos tu lógica original aquí mismo)
val globalExerciseIcons = listOf(
    "dumbbell" to Icons.Default.FitnessCenter,
    "body" to Icons.Default.AccessibilityNew,
    "kick" to Icons.Default.SportsMartialArts,
    "run" to Icons.Default.DirectionsRun,
    "walk" to Icons.Default.DirectionsWalk,
    "chart" to Icons.Default.ShowChart,
    "timer" to Icons.Default.Timer,
    "yoga" to Icons.Default.SelfImprovement,
    "bolt" to Icons.Default.Bolt,
    "layers" to Icons.Default.Layers
)

@Composable
fun getExerciseIcon(iconName: String): ImageVector {
    return globalExerciseIcons.find { it.first == iconName }?.second
        ?: Icons.Default.FitnessCenter
}