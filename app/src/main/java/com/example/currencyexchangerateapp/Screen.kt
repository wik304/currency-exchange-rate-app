package com.example.currencyexchangerateapp

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object DetailsScreen : Screen("details_screen")
    object SettingsScreen : Screen("settings_screen")
}