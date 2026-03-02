package com.yago.aegis.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Routine : Screen("routine", "ROUTINE", Icons.Default.FitnessCenter)
    object Weekly : Screen("weekly", "WEEKLY", Icons.Default.DateRange)
    object Profile : Screen("profile", "PROFILE", Icons.Default.Person)
    object Stats : Screen("stats", "STATS", Icons.Default.BarChart)
}