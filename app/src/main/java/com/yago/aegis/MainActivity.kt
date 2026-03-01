package com.yago.aegis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yago.aegis.data.SettingsStore
import com.yago.aegis.data.UserRepository
import com.yago.aegis.ui.screens.MainProfileScreen
import com.yago.aegis.ui.screens.SettingsScreen
import com.yago.aegis.ui.theme.AegisTheme
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.ProfileViewModelFactory
import android.content.Intent

class MainActivity : ComponentActivity() {

    private val settingsStore by lazy { SettingsStore(applicationContext) }
    private val userRepository by lazy { UserRepository(settingsStore) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: ProfileViewModel by viewModels {
            ProfileViewModelFactory(userRepository)
        }

        setContent {
            AegisTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "profile"
                ) {
                    composable("profile") {
                        MainProfileScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }

                    composable(
                        route = "settings",
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
    private fun takePersistableUriPermission(uri: android.net.Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}