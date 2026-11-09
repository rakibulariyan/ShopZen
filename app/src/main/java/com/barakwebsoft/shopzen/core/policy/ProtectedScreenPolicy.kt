package com.barakwebsoft.shopzen.core.policy

import com.barakwebsoft.shopzen.core.navigation.ShopZenRoutes

/**
 * Exception thrown when an attempt is made to display or initialize an advertisement
 * on a protected screen surface.
 */
class ProtectedScreenAdException(message: String) : IllegalStateException(message)

/**
 * Advertising restriction policy mandated by PRD Section 7 & 8, DESIGN.md Section 8,
 * and ADR-009 (Ads excluded from critical shopping screens).
 *
 * CRITICAL REQUIREMENTS:
 * Ads MUST NEVER appear on:
 * 1. Market Mode (in-store shopping screen)
 * 2. Add/Edit Item (item creation, editing, price adjustment)
 * 3. Budget screen (budget summary, financial breakdowns, spend culprits)
 * 4. Purchase / In-App Billing screens (Remove Ads entitlement, subscription)
 *
 * This policy provides deterministic route, screen and surface matching to guarantee
 * that ad rendering components, banner hosts, or interstitial triggers will never execute
 * within protected surfaces.
 */
object ProtectedScreenPolicy {

    /**
     * Set of route prefixes that are strictly protected from all advertising.
     */
    val PROTECTED_ROUTE_PREFIXES: Set<String> = setOf(
        "market_mode",
        "add_edit_item",
        "budget",
        "purchase",
        "billing",
        "remove_ads",
        "subscription"
    )

    /**
     * Set of semantic screen names or component identifiers that are protected.
     */
    val PROTECTED_SCREEN_NAMES: Set<String> = setOf(
        "MARKET_MODE",
        "ADD_EDIT_ITEM",
        "BUDGET",
        "FINANCIAL_METRICS",
        "PURCHASE",
        "BILLING",
        "REMOVE_ADS"
    )

    /**
     * Checks if a destination route or path is protected from ads.
     *
     * @param route The navigation route string (e.g., "market_mode/123", "add_edit_item/123?itemId=456", or pattern "market_mode/{listId}")
     * @return true if the route matches a protected screen, false otherwise.
     */
    fun isProtectedDestination(route: String?): Boolean {
        if (route.isNullOrBlank()) return false

        // Normalize route by stripping query parameters and leading/trailing whitespace & slashes
        val cleanRoute = route.substringBefore('?').trim().trim('/')
        if (cleanRoute.isEmpty()) return false

        val baseRoute = cleanRoute.substringBefore('/')

        // Direct pattern checks against route constants
        if (cleanRoute.equals(ShopZenRoutes.MARKET_MODE, ignoreCase = true) ||
            cleanRoute.equals(ShopZenRoutes.ADD_EDIT_ITEM, ignoreCase = true)
        ) {
            return true
        }

        // Check against known protected route prefixes
        return PROTECTED_ROUTE_PREFIXES.any { prefix ->
            baseRoute.equals(prefix, ignoreCase = true) ||
                cleanRoute.startsWith("$prefix/", ignoreCase = true) ||
                cleanRoute.equals(prefix, ignoreCase = true)
        }
    }

    /**
     * Checks if a semantic screen name is protected from ads.
     *
     * @param screenName The semantic identifier of the screen or modal.
     * @return true if the screen name matches a protected screen, false otherwise.
     */
    fun isProtectedScreen(screenName: String?): Boolean {
        if (screenName.isNullOrBlank()) return false
        val normalized = screenName.trim().uppercase()
        return PROTECTED_SCREEN_NAMES.any { it.equals(normalized, ignoreCase = true) }
    }

    /**
     * Determines whether an ad is permitted to be shown for the specified navigation route.
     *
     * Returns true ONLY if the route is non-null, non-blank, and not protected.
     */
    fun canShowAd(route: String?): Boolean {
        if (route.isNullOrBlank()) return false
        return !isProtectedDestination(route)
    }

    /**
     * Enforces the zero-ad policy by asserting that the specified route is not protected.
     * Throws [ProtectedScreenAdException] if the route is a protected destination.
     */
    fun assertNoAdsAllowed(route: String?) {
        if (isProtectedDestination(route)) {
            throw ProtectedScreenAdException(
                "AD VIOLATION: Ads are strictly forbidden on protected destination: '$route' " +
                    "per ADR-009, PRD Section 7 & 8, and DESIGN.md Section 8."
            )
        }
    }
}

