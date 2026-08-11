package com.yago.aegis.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.yago.aegis.R

/**
 * Pestañas de la barra inferior (US-04: es UI, por eso vive en `ui.navigation`, no en `data`).
 * Las rutas provienen de [Routes] (única fuente de verdad, US-07): así el destino del NavHost
 * y la navegación de la barra no se pueden desincronizar.
 */
sealed class Screen(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    object Routine : Screen(Routes.ROUTINE, R.string.nav_routine, Icons.Default.Assignment)
    object Stats : Screen(Routes.STATS, R.string.nav_stats, Icons.Default.BarChart)
    object Ejercicios : Screen(Routes.EJERCICIOS, R.string.nav_exercices, Icons.Default.FitnessCenter)
    object Profile : Screen(Routes.PROFILE, R.string.nav_profile, Icons.Default.Person)
    object Train : Screen(Routes.TRAIN, R.string.nav_train, Icons.Default.Bolt)
    object Panteon : Screen(Routes.PANTEON, R.string.nav_panteon, Icons.Default.MilitaryTech)
}
