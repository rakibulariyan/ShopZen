package com.barakwebsoft.shopzen.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level destinations represented in the bottom navigation bar.
 * Templates are deferred to V1 per ADR-008 and PRD.
 */
enum class TopLevelDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        route = "home",
        title = "Lists",
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List
    ),
    HISTORY(
        route = "history",
        title = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    SETTINGS(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

/**
 * Internal route definitions for detail/modal navigation.
 */
object ShopZenRoutes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    const val CREATE_LIST = "create_list"
    const val LIST_DETAILS = "list_details/{listId}"
    const val MARKET_MODE = "market_mode/{listId}"
    const val ADD_EDIT_ITEM = "add_edit_item/{listId}?itemId={itemId}"
    const val HISTORICAL_DETAILS = "historical_details/{historyId}"

    fun listDetails(listId: String): String = "list_details/$listId"
    fun marketMode(listId: String): String = "market_mode/$listId"
    fun addEditItem(listId: String, itemId: String? = null): String {
        return if (itemId != null) {
            "add_edit_item/$listId?itemId=$itemId"
        } else {
            "add_edit_item/$listId"
        }
    }
    fun historicalDetails(historyId: String): String = "historical_details/$historyId"
}

