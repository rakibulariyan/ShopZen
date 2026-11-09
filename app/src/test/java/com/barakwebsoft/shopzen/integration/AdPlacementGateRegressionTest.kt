package com.barakwebsoft.shopzen.integration

import com.barakwebsoft.shopzen.core.navigation.ShopZenRoutes
import com.barakwebsoft.shopzen.core.policy.ProtectedScreenPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ad placement gate regression tests (SZ-060, SZ-052).
 * Verifies strict policy enforcement across all navigation destinations:
 * PRD & ADR-009 / ADR-015: Ads must NEVER appear on Market Mode, Add/Edit Item,
 * Budget screens, or Purchase/Billing screens.
 */
class AdPlacementGateRegressionTest {

    @Test
    fun protectedRoutes_strictlyProhibitAds_regardlessOfParameters() {
        val protectedRoutes = listOf(
            ShopZenRoutes.MARKET_MODE,
            "market_mode/list-123",
            "market_mode/list-456?refresh=true",
            ShopZenRoutes.ADD_EDIT_ITEM,
            "add_edit_item/list-123",
            "add_edit_item/list-123?itemId=item-456",
            "budget",
            "budget/list-123",
            "budget_metrics/list-123",
            "purchase",
            "billing",
            "remove_ads",
            "subscription"
        )

        for (route in protectedRoutes) {
            assertTrue("Route '$route' must be detected as protected", ProtectedScreenPolicy.isProtectedDestination(route))
            assertFalse("canShowAd must strictly return false for protected route '$route'", ProtectedScreenPolicy.canShowAd(route))
        }
    }

    @Test
    fun protectedScreenNames_strictlyIdentified() {
        val protectedScreens = listOf(
            "MARKET_MODE",
            "market_mode",
            "ADD_EDIT_ITEM",
            "add_edit_item",
            "BUDGET",
            "budget",
            "FINANCIAL_METRICS",
            "PURCHASE",
            "BILLING",
            "REMOVE_ADS"
        )

        for (screen in protectedScreens) {
            assertTrue("Screen name '$screen' must be identified as protected", ProtectedScreenPolicy.isProtectedScreen(screen))
        }
    }

    @Test
    fun nullAndBlankRoutes_prohibitAds() {
        assertFalse("null route must never allow ads", ProtectedScreenPolicy.canShowAd(null))
        assertFalse("empty route must never allow ads", ProtectedScreenPolicy.canShowAd(""))
        assertFalse("blank route must never allow ads", ProtectedScreenPolicy.canShowAd("   "))
    }

    @Test
    fun approvedHomeRoute_permitsAdPlacement() {
        assertTrue("Home screen is an approved ad placement surface", ProtectedScreenPolicy.canShowAd(ShopZenRoutes.HOME))
        assertFalse("Home screen is not a protected destination", ProtectedScreenPolicy.isProtectedDestination(ShopZenRoutes.HOME))
    }
}
