package com.example.currencyexchangerateapp.ui.navigation

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object DetailsScreen : Screen("details_screen")
    object FavouriteScreen : Screen("favourite_screen")
    object SettingsScreen : Screen("settings_screen")
}