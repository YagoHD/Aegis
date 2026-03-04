package com.yago.aegis.data

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.yago.aegis.R

sealed class Screen(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    object Routine : Screen("routine", R.string.nav_routine, Icons.Default.Assignment)
    object Weekly : Screen("weekly", R.string.nav_weekly, Icons.Default.DateRange)
    object Ejercicios : Screen("Ejercicios", R.string.nav_exercices, Icons.Default.FitnessCenter)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
}