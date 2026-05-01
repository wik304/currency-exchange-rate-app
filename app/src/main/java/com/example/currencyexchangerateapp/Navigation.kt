package com.example.currencyexchangerateapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

data class BottomNavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)

@Composable
fun Navigation(
    settingsManager: SettingsManager
) {
    val factory = MainViewModelFactory(settingsManager)
    val mainViewModel: MainViewModel = viewModel(factory = factory)

    val navController = rememberNavController()

    val items = listOf(
        BottomNavigationItem(
            route = Screen.MainScreen.route,
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            hasNews = false
        ),

//        BottomNavigationItem(
//            route = "details/PLN",
//            title = "Details",
//            selectedIcon = Icons.Filled.Analytics,
//            unselectedIcon = Icons.Outlined.Analytics,
//            hasNews = false
//        ),

        BottomNavigationItem(
            route = Screen.FavouriteScreen.route,
            title = "Favourite",
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder,
            hasNews = false
        ),

        BottomNavigationItem(
            route = Screen.SettingsScreen.route,
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            hasNews = false
        ),
    )

    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedItemIndex == index,
                            onClick = {
                                selectedItemIndex = index
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = {
                                Text(text = item.title)
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.badgeCount != null) {
                                            Badge {
                                                Text(text = item.badgeCount.toString())
                                            }
                                        } else if (item.hasNews) {
                                            Badge()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectedItemIndex == index) {
                                            item.selectedIcon
                                        } else {
                                            item.unselectedIcon
                                        },
                                        contentDescription = item.title
                                    )
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.MainScreen.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = Screen.MainScreen.route) {
                    MainScreen(
                        navController = navController,
                        viewModel = mainViewModel,
                        settingsManager = settingsManager
                    )
                }
                composable(
                    route = "details/{currencyCode}",
                    arguments = listOf(navArgument("currencyCode") { type = NavType.StringType })
                ) { backStackEntry ->
                    val currencyCode = backStackEntry.arguments?.getString("currencyCode") ?: "USD"
                    DetailsScreen(
                        currencyCode = currencyCode,
                        navController = navController,
                        viewModel = mainViewModel,
                        settingsManager = settingsManager
                    )
                }
                composable(route = Screen.FavouriteScreen.route) {
                    FavouriteScreen(
                        navController = navController,
                        viewModel = mainViewModel,
                        settingsManager = settingsManager
                    )
                }
                composable(route = Screen.SettingsScreen.route) {
                    SettingsScreen(
                        viewModel = mainViewModel,
                        settingsManager = settingsManager
                    )
                }
            }
        }
    }
}