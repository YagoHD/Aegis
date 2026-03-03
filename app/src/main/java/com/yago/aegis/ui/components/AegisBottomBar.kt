package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    val items = listOf(
        Screen.Routine,
        Screen.Weekly,
        Screen.Stats,
        Screen.Profile,
    )

    NavigationBar(
        containerColor = Color.Black,
        tonalElevation = 0.dp,
        modifier = Modifier.height(56.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            // ✅ Obtenemos el texto del XML una sola vez para este item
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
                        contentDescription = labelText, // ✅ Usamos el texto del XML
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = labelText, // ✅ Usamos el texto del XML
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
    }
}