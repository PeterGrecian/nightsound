package com.nightsound.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nightsound.ui.screens.main.MainScreen
import com.nightsound.ui.screens.playback.PlaybackScreen
import com.nightsound.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToPlayback = {
                    navController.navigate(Screen.Playback.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Playback.route) {
            PlaybackScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
