package com.nightsound.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Playback : Screen("playback")
    object Settings : Screen("settings")
}
