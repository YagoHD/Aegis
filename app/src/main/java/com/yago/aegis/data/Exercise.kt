package com.yago.aegis.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class Exercise(
    val name: String,
    val type: String,        // Ejemplo: "COMPOUND" o "MACHINE"
    val muscleGroup: String, // Ejemplo: "LEGS"
    val id: Int = 0,
    val tags: List<String> = emptyList(),
    val iconName: String = "FitnessCenter", // Guardamos el nombre del icono
    val notes: String = ""
)

@Composable
fun getExerciseIcon(iconName: String): ImageVector {
    return when (iconName) {
        "dumbbell" -> Icons.Default.FitnessCenter
        "body" -> Icons.Default.AccessibilityNew
        "legs" -> Icons.Default.DirectionsRun
        "heart" -> Icons.Default.Favorite
        "timer" -> Icons.Default.Timer
        "bolt" -> Icons.Default.Bolt
        "layers" -> Icons.Default.Layers
        else -> Icons.Default.FitnessCenter
    }
}