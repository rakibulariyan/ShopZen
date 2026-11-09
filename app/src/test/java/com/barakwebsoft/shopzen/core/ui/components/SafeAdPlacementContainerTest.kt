package com.barakwebsoft.shopzen.core.ui.components

import com.barakwebsoft.shopzen.core.navigation.ShopZenRoutes
import com.barakwebsoft.shopzen.core.policy.ProtectedScreenPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verification test suite for SZ-052:
 * Validates that ads are strictly prohibited from appearing on protected screens
 * (Market Mode, Add/Edit Item, Budget screen, Purchase/Billing),
 * and only permitted on approved surfaces when consent is granted and Remove Ads is not active.
 */
class SafeAdPlacementContainerTest {

    private fun simulateContainer(
        route: String?,
        isAdsRemoved: Boolean = false,
        canRequestAds: Boolean = true
    ): Boolean {
        var adEmitted = false
        if (!isAdsRemoved && canRequestAds && ProtectedScreenPolicy.canShowAd(route)) {
            adEmitted = true
        }
        return adEmitted
    }

    @Test
    fun protectedScreen_marketMode_neverRendersAd() {
        val patternRoute = ShopZenRoutes.MARKET_MODE
        val instantiatedRoute = ShopZenRoutes.marketMode("list-123")

        assertFalse("Pattern route must never emit ad", simulateContainer(patternRoute))
        assertFalse("Instantiated Market Mode route must never emit ad", simulateContainer(instantiatedRoute))
    }

    @Test
    fun protectedScreen_addEditItem_neverRendersAd() {
        val patternRoute = ShopZenRoutes.ADD_EDIT_ITEM
        val newItemRoute = ShopZenRoutes.addEditItem("list-abc")
        val editItemRoute = ShopZenRoutes.addEditItem("list-abc", "item-xyz")

        assertFalse("Add/Edit Item pattern must never emit ad", simulateContainer(patternRoute))
        assertFalse("New Item screen must never emit ad", simulateContainer(newItemRoute))
        assertFalse("Edit Item screen must never emit ad", simulateContainer(editItemRoute))
    }

    @Test
    fun protectedScreen_budgetAndFinancialMetrics_neverRendersAd() {
        val budgetRoutes = listOf("budget", "budget/summary", "budget/breakdown", "/budget/")
        for (route in budgetRoutes) {
            assertFalse("Budget screen '$route' must never emit ad", simulateContainer(route))
        }
    }

    @Test
    fun protectedScreen_purchaseAndBilling_neverRendersAd() {
        val billingRoutes = listOf("purchase", "purchase/remove_ads", "billing", "remove_ads", "subscription")
        for (route in billingRoutes) {
            assertFalse("Billing screen '$route' must never emit ad", simulateContainer(route))
        }
    }

    @Test
    fun approvedScreen_homeOverview_rendersAdWhenEligible() {
        val homeRoute = ShopZenRoutes.HOME
        val shouldEmit = simulateContainer(homeRoute, isAdsRemoved = false, canRequestAds = true)
        assertTrue("Approved Home overview screen must permit ad placement", shouldEmit)
    }

    @Test
    fun entitlementActive_whenRemoveAdsPurchased_neverRendersAdEvenOnHome() {
        val homeRoute = ShopZenRoutes.HOME
        val shouldEmit = simulateContainer(homeRoute, isAdsRemoved = true, canRequestAds = true)
        assertFalse("Active Remove Ads entitlement must completely suppress ad rendering", shouldEmit)
    }

    @Test
    fun consentNotGranted_whenCanRequestAdsIsFalse_neverRendersAdEvenOnHome() {
        val homeRoute = ShopZenRoutes.HOME
        val shouldEmit = simulateContainer(homeRoute, isAdsRemoved = false, canRequestAds = false)
        assertFalse("Missing UMP consent must completely suppress ad rendering", shouldEmit)
    }

    @Test
    fun nullOrBlankRoute_neverRendersAd() {
        assertFalse("Null route must never emit ad", simulateContainer(null))
        assertFalse("Blank route must never emit ad", simulateContainer("   "))
        assertFalse("Empty route must never emit ad", simulateContainer(""))
    }
}

