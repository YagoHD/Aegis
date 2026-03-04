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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yago.aegis.R
import com.yago.aegis.data.Screen
import com.yago.aegis.ui.theme.AegisBronze

@Composable
fun AegisBottomBar(navController: NavHostController) {
    // Dividimos los iconos para dejar el centro libre
    val leftItems = listOf(Screen.Weekly, Screen.Routine)
    val rightItems = listOf(Screen.Ejercicios, Screen.Profile)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.Black,
        tonalElevation = 0.dp,
        modifier = Modifier.height(80.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        // 1. Lado Izquierdo
        leftItems.forEach { screen ->
            AegisNavItem(screen, currentRoute, navController)
        }

        // 2. BOTÓN CENTRAL: ENTRENAMIENTO (BOLT)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(AegisBronze)
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
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 3. Lado Derecho
        rightItems.forEach { screen ->
            AegisNavItem(screen, currentRoute, navController)
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

    NavigationBarItem(
        selected = currentRoute == screen.route,
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
                modifier = Modifier.size(20.dp)
            )
        },
        label = {
            Text(
                text = labelText.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AegisBronze,
            selectedTextColor = AegisBronze,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
            indicatorColor = Color.Transparent
        )
    )
}