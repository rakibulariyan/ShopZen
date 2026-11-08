package com.barakwebsoft.shopzen.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class ShopZenRoutesTest {

    @Test
    fun testListDetailsRoute() {
        val route = ShopZenRoutes.listDetails("test-list-123")
        assertEquals("list_details/test-list-123", route)
    }

    @Test
    fun testMarketModeRoute() {
        val route = ShopZenRoutes.marketMode("test-list-123")
        assertEquals("market_mode/test-list-123", route)
    }

    @Test
    fun testAddEditItemRouteWithoutItemId() {
        val route = ShopZenRoutes.addEditItem("test-list-123")
        assertEquals("add_edit_item/test-list-123", route)
    }

    @Test
    fun testAddEditItemRouteWithItemId() {
        val route = ShopZenRoutes.addEditItem("test-list-123", "item-456")
        assertEquals("add_edit_item/test-list-123?itemId=item-456", route)
    }

    @Test
    fun testHistoricalDetailsRoute() {
        val route = ShopZenRoutes.historicalDetails("history-789")
        assertEquals("historical_details/history-789", route)
    }

    @Test
    fun testTopLevelDestinationsExcludesTemplates() {
        val destinations = TopLevelDestination.values().map { it.route }
        assertEquals(listOf("home", "history", "settings"), destinations)
    }
}

