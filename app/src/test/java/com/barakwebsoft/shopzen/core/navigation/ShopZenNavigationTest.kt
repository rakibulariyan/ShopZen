package com.barakwebsoft.shopzen.core.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopZenNavigationTest {

    @Test
    fun testIsTopLevelDestination() {
        val topLevelRoutes = TopLevelDestination.values().map { it.route }.toSet()

        assertTrue(topLevelRoutes.contains("home"))
        assertTrue(topLevelRoutes.contains("history"))
        assertTrue(topLevelRoutes.contains("settings"))

        // Sub-destinations should not be in bottom navigation
        assertFalse(topLevelRoutes.contains(ShopZenRoutes.CREATE_LIST))
        assertFalse(topLevelRoutes.contains(ShopZenRoutes.MARKET_MODE))
        assertFalse(topLevelRoutes.contains(ShopZenRoutes.LIST_DETAILS))
        assertFalse(topLevelRoutes.contains("templates"))
    }
}

