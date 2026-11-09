package com.barakwebsoft.shopzen.core.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the Google Play Billing contract for ShopZen.
 * Handles dynamic price discovery, subscription checkout, and cryptographic purchase verification.
 */
interface BillingManager {
    /**
     * Observable stream of the current verified billing and entitlement state.
     */
    val billingState: StateFlow<BillingState>

    /**
     * Establishes connection to Google Play Billing service and queries product details and active purchases.
     */
    fun startConnection(onReady: () -> Unit = {})

    /**
     * Launches the Google Play subscription purchase flow for the yearly Remove Ads product.
     * Returns true if the billing flow was launched successfully, false otherwise.
     */
    fun launchBillingFlow(activity: Activity): Boolean

    /**
     * Queries active purchases from Google Play (used for app startup and manual "Restore Purchases").
     */
    fun queryPurchases()

    /**
     * Disconnects the billing client when no longer needed.
     */
    fun endConnection()
}
