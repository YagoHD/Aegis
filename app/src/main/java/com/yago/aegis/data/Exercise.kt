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
    val lastPerformance: String = "",
    val oneRepMax: Double = 0.0,    // Para mostrar "1RM: 115KG" en la lista
    val bestSet: String? = "--",     // Para mostrar el mejor récord histórico
    val history: List<ExerciseRecord> = emptyList()
)

data class ExerciseRecord(
    val date: Long,
    val weight: Double,
    val reps: Int,
    val oneRepMax: Double
)
/**
 * EL REGISTRO: Lo que el usuario anota en cada serie durante el entreno.
 */
data class ExerciseSet(
    val id: String = UUID.randomUUID().toString(),
    val reps: Int = 0,
    val weight: Double = 0.0,
    val isCompleted: Boolean = false
)

/**
 * EL VÍNCULO: Une un ejercicio con las series que se están haciendo en este momento.
 */
data class ExerciseProgress(
    val exercise: Exercise,
    val sets: List<ExerciseSet> = listOf(ExerciseSet())
)

/**
 * LA SESIÓN: El contenedor global de un día de entrenamiento.
 */
data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val routineName: String,
    val date: Long = System.currentTimeMillis(),
    val exercisesProgress: List<ExerciseProgress>
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