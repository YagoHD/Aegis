package com.yago.aegis.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class Exercise(
    val name: String,
    val type: String,        // Ejemplo: "COMPOUND" o "MACHINE"
    val muscleGroup: String, // Ejemplo: "LEGS"
    val id: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val iconName: String = "dumbbell", // Guardamos el nombre del icono
    val notes: String = ""
)
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