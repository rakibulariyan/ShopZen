package com.barakwebsoft.shopzen.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barakwebsoft.shopzen.feature.history.HistoryScreen
import com.barakwebsoft.shopzen.feature.home.HomeScreen
import com.barakwebsoft.shopzen.feature.settings.SettingsScreen

@Composable
fun ShopZenApp(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = TopLevelDestination.values().any { it.route == currentRoute }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelDestination) {
                ShopZenBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToDestination = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ShopZenRoutes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ShopZenRoutes.HOME) {
                HomeScreen(
                    onCreateListClick = {
                        // Will navigate to CREATE_LIST in Phase 2
                    }
                )
            }
            composable(ShopZenRoutes.HISTORY) {
                HistoryScreen()
            }
            composable(ShopZenRoutes.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun ShopZenBottomBar(
    currentRoute: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        TopLevelDestination.values().forEach { destination ->
            val isSelected = currentRoute == destination.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.title
                    )
                },
                label = { Text(destination.title) }
            )
        }
    }
}

