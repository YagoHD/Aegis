package com.yago.aegis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yago.aegis.data.Screen

@Composable
fun AegisBottomBar(navController: NavHostController) {
    val leftItems = listOf(Screen.Stats, Screen.Routine)
    val rightItems = listOf(Screen.Ejercicios, Screen.Profile)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Column {
        // --- SEPARADOR TÉCNICO SUPERIOR ---
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )

        // Usamos surface (121212) para que destaque sobre el background (050505)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = Modifier.height(84.dp),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            // 1. Lado Izquierdo
            leftItems.forEach { screen ->
                AegisNavItem(screen, currentRoute, navController)
            }

            // 2. BOTÓN CENTRAL: ACCIÓN ELITE
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp) // Un pelín más grande para impacto visual
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            if (currentRoute != Screen.Train.route) {
                                navController.navigate(Screen.Train.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Empezar entrenamiento",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // 3. Lado Derecho
            rightItems.forEach { screen ->
                AegisNavItem(screen, currentRoute, navController)
            }
        }
    }
}

@Composable
fun RowScope.AegisNavItem(
    screen: Screen,
    currentRoute: String?,
    navController: NavHostController
) {
    val labelText = stringResource(screen.labelRes)
    val isSelected = currentRoute == screen.route

    NavigationBarItem(
        selected = isSelected,
        onClick = {
            if (currentRoute != screen.route) {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = labelText,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = labelText.uppercase(),
                style = TextStyle(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp // Ajustado para que quepa bien en el nuevo ancho
                )
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.secondary,
            unselectedTextColor = MaterialTheme.colorScheme.secondary,
            indicatorColor = Color.Transparent
        )
    )
}