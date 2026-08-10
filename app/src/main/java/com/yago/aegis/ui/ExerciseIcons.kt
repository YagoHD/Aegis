package com.yago.aegis.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Mapeo de nombre de icono → ImageVector de Compose (US-04: dependencia de Compose fuera de
 * la capa de datos). `Exercise` guarda solo `iconName: String`; la resolución vive en UI.
 */
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
fun getExerciseIcon(iconName: String): ImageVector =
    globalExerciseIcons.find { it.first == iconName }?.second
        ?: Icons.Default.FitnessCenter
