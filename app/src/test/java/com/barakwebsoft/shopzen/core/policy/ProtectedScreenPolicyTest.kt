package com.barakwebsoft.shopzen.core.policy

import com.barakwebsoft.shopzen.core.navigation.ShopZenRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ProtectedScreenPolicy] asserting strict enforcement of zero-ads policy
 * on Market Mode, Add/Edit Item, Budget screens, and Purchase surfaces per ADR-009,
 * PRD Section 7 & 8, and DESIGN.md Section 8.
 */
class ProtectedScreenPolicyTest {

    @Test
    fun marketModeRoutes_areProtectedAndForbidAds() {
        val patternRoute = ShopZenRoutes.MARKET_MODE
        val instantiatedRoute = ShopZenRoutes.marketMode("test-list-id-123")
        val arbitraryMarketRoute = "market_mode/custom-uuid-456"

        assertTrue("Pattern route must be protected", ProtectedScreenPolicy.isProtectedDestination(patternRoute))
        assertTrue("Instantiated route must be protected", ProtectedScreenPolicy.isProtectedDestination(instantiatedRoute))
        assertTrue("Arbitrary market mode route must be protected", ProtectedScreenPolicy.isProtectedDestination(arbitraryMarketRoute))

        assertFalse("Ads must not be allowed on market mode pattern", ProtectedScreenPolicy.canShowAd(patternRoute))
        assertFalse("Ads must not be allowed on instantiated market mode", ProtectedScreenPolicy.canShowAd(instantiatedRoute))
        assertFalse("Ads must not be allowed on arbitrary market mode", ProtectedScreenPolicy.canShowAd(arbitraryMarketRoute))

        assertThrows(ProtectedScreenAdException::class.java) {
            ProtectedScreenPolicy.assertNoAdsAllowed(patternRoute)
        }
        assertThrows(ProtectedScreenAdException::class.java) {
            ProtectedScreenPolicy.assertNoAdsAllowed(instantiatedRoute)
        }
    }

    @Test
    fun addEditItemRoutes_areProtectedAndForbidAds() {
        val patternRoute = ShopZenRoutes.ADD_EDIT_ITEM
        val newItemRoute = ShopZenRoutes.addEditItem("list-abc")
        val editItemRoute = ShopZenRoutes.addEditItem("list-abc", "item-xyz")

        assertTrue("Add/Edit Item pattern route must be protected", ProtectedScreenPolicy.isProtectedDestination(patternRoute))
        assertTrue("New item route must be protected", ProtectedScreenPolicy.isProtectedDestination(newItemRoute))
        assertTrue("Edit item route with query params must be protected", ProtectedScreenPolicy.isProtectedDestination(editItemRoute))

        assertFalse("Ads must not be allowed on Add/Edit pattern", ProtectedScreenPolicy.canShowAd(patternRoute))
        assertFalse("Ads must not be allowed on new item route", ProtectedScreenPolicy.canShowAd(newItemRoute))
        assertFalse("Ads must not be allowed on edit item route", ProtectedScreenPolicy.canShowAd(editItemRoute))

        assertThrows(ProtectedScreenAdException::class.java) {
            ProtectedScreenPolicy.assertNoAdsAllowed(patternRoute)
        }
        assertThrows(ProtectedScreenAdException::class.java) {
            ProtectedScreenPolicy.assertNoAdsAllowed(newItemRoute)
        }
        assertThrows(ProtectedScreenAdException::class.java) {
            ProtectedScreenPolicy.assertNoAdsAllowed(editItemRoute)
        }
    }

    @Test
    fun budgetRoutes_areProtectedAndForbidAds() {
        val budgetRoutes = listOf(
            "budget",
            "budget/summary",
            "budget/breakdown",
            "/budget/"
        )

        for (route in budgetRoutes) {
            assertTrue("Budget route '$route' must be protected", ProtectedScreenPolicy.isProtectedDestination(route))
            assertFalse("Ads must not be allowed on budget route '$route'", ProtectedScreenPolicy.canShowAd(route))
            assertThrows("Budget route '$route' must throw violation exception", ProtectedScreenAdException::class.java) {
                ProtectedScreenPolicy.assertNoAdsAllowed(route)
            }
        }
    }

    @Test
    fun purchaseAndBillingRoutes_areProtectedAndForbidAds() {
        val purchaseRoutes = listOf(
            "purchase",
            "purchase/remove_ads",
            "billing",
            "billing/flow",
            "remove_ads",
            "subscription"
        )

        for (route in purchaseRoutes) {
            assertTrue("Purchase route '$route' must be protected", ProtectedScreenPolicy.isProtectedDestination(route))
            assertFalse("Ads must not be allowed on purchase route '$route'", ProtectedScreenPolicy.canShowAd(route))
            assertThrows("Purchase route '$route' must throw violation exception", ProtectedScreenAdException::class.java) {
                ProtectedScreenPolicy.assertNoAdsAllowed(route)
            }
        }
    }

    @Test
    fun semanticScreenNames_matchProtectionRules() {
        assertTrue(ProtectedScreenPolicy.isProtectedScreen("MARKET_MODE"))
        assertTrue(ProtectedScreenPolicy.isProtectedScreen("market_mode"))
        assertTrue(ProtectedScreenPolicy.isProtectedScreen("ADD_EDIT_ITEM"))
        assertTrue(ProtectedScreenPolicy.isProtectedScreen("add_edit_item"))
        assertTrue(ProtectedScreenPolicy.isProtectedScreen("BUDGET"))
        assertTrue(ProtectedScreenPolicy.isProtectedScreen("FINANCIAL_METRICS"))
        assertTrue(ProtectedScreenPolicy.isProtectedScreen("PURCHASE"))
        assertTrue(ProtectedScreenPolicy.isProtectedScreen("BILLING"))
        assertTrue(ProtectedScreenPolicy.isProtectedScreen("REMOVE_ADS"))

        assertFalse(ProtectedScreenPolicy.isProtectedScreen("HOME"))
        assertFalse(ProtectedScreenPolicy.isProtectedScreen("HISTORY"))
        assertFalse(ProtectedScreenPolicy.isProtectedScreen("SETTINGS"))
        assertFalse(ProtectedScreenPolicy.isProtectedScreen("LIST_DETAILS"))
        assertFalse(ProtectedScreenPolicy.isProtectedScreen(null))
        assertFalse(ProtectedScreenPolicy.isProtectedScreen(""))
    }

    @Test
    fun nonProtectedRoutes_permitAdPlacements() {
        val nonProtectedRoutes = listOf(
            ShopZenRoutes.HOME,
            ShopZenRoutes.HISTORY,
            ShopZenRoutes.SETTINGS,
            ShopZenRoutes.LIST_DETAILS,
            ShopZenRoutes.listDetails("list-123")
        )

        for (route in nonProtectedRoutes) {
            assertFalse("Route '$route' should not be protected", ProtectedScreenPolicy.isProtectedDestination(route))
            assertTrue("Route '$route' should allow ad placement (subject to consent/entitlement)", ProtectedScreenPolicy.canShowAd(route))
            // Must not throw
            ProtectedScreenPolicy.assertNoAdsAllowed(route)
        }
    }

    @Test
    fun nullAndBlankRoutes_areHandledSafely() {
        assertFalse(ProtectedScreenPolicy.isProtectedDestination(null))
        assertFalse(ProtectedScreenPolicy.isProtectedDestination(""))
        assertFalse(ProtectedScreenPolicy.isProtectedDestination("   "))

        // Null and blank routes must NOT allow ads
        assertFalse(ProtectedScreenPolicy.canShowAd(null))
        assertFalse(ProtectedScreenPolicy.canShowAd(""))
        assertFalse(ProtectedScreenPolicy.canShowAd("   "))
    }

    @Test
    fun routeNormalization_handlesSlashesAndFormatting() {
        assertTrue(ProtectedScreenPolicy.isProtectedDestination("  market_mode/list-123/  "))
        assertTrue(ProtectedScreenPolicy.isProtectedDestination("/MARKET_MODE/list-123"))
        assertTrue(ProtectedScreenPolicy.isProtectedDestination("Add_Edit_Item/list-123?itemId=abc&other=123"))
        assertFalse(ProtectedScreenPolicy.canShowAd("/market_mode/123/"))
    }
}

