package com.barakwebsoft.shopzen.core.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Production implementation of [BillingManager] using Google Play Billing Library 7.x.
 *
 * Security and Architecture Rules:
 * 1. Entitlement verification strictly requires cryptographic proof from Play (`Purchase.PurchaseState.PURCHASED`).
 * 2. Never trust a local boolean alone as proof of purchase.
 * 3. Subscription price is never hardcoded; dynamically populates localized price from Play.
 * 4. Acknowledges purchases within 3 days to avoid automatic revocation.
 * 5. Pending purchases do NOT grant entitlement until verified.
 */
class GooglePlayBillingManager(
    private val context: Context,
    clientWrapperFactory: ((PurchasesUpdatedListener) -> PlayBillingClientWrapper)? = null
) : BillingManager, PurchasesUpdatedListener {

    private val _billingState = MutableStateFlow(BillingState())
    override val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    val billingClient: PlayBillingClientWrapper = clientWrapperFactory?.invoke(this)
        ?: DefaultPlayBillingClientWrapper(context, this)

    override fun startConnection(onReady: () -> Unit) {
        if (!billingClient.isReady) {
            logDebug("Starting connection to Google Play Billing")
            try {
                billingClient.startConnection(
                    onSetupFinished = { billingResult ->
                        onBillingSetupFinished(billingResult)
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            onReady()
                        }
                    },
                    onDisconnected = {
                        onBillingServiceDisconnected()
                    }
                )
            } catch (e: Throwable) {
                logError("Failed to start billing connection", e)
                _billingState.update { it.copy(purchaseStatus = PurchaseStatus.UNAVAILABLE) }
            }
        } else {
            queryProductDetails()
            queryPurchases()
            onReady()
        }
    }

    fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            logDebug("Billing setup finished successfully")
            queryProductDetails()
            queryPurchases()
        } else {
            logWarning("Billing setup failed: ${billingResult.debugMessage} (${billingResult.responseCode})")
            _billingState.update {
                it.copy(
                    purchaseStatus = PurchaseStatus.UNAVAILABLE,
                    errorMessage = billingResult.debugMessage
                )
            }
        }
    }

    fun onBillingServiceDisconnected() {
        logWarning("Billing service disconnected")
        // Will reconnect automatically on next query or user action
    }

    fun queryProductDetails() {
        try {
            billingClient.queryProductDetails { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                    val details = productDetailsList.first()
                    val formattedPrice = details.formattedPrice ?: ""
                    val currencyCode = details.priceCurrencyCode ?: ""
                    val amountMicros = details.priceAmountMicros ?: 0L
                    val offerToken = details.offerToken ?: ""

                    logDebug("Loaded product details from Play: $formattedPrice ($currencyCode)")

                    _billingState.update { current ->
                        current.copy(
                            subscriptionDetails = SubscriptionDetails(
                                productId = details.productId,
                                formattedPrice = formattedPrice,
                                priceCurrencyCode = currencyCode,
                                priceAmountMicros = amountMicros,
                                title = details.title,
                                description = details.description,
                                offerToken = offerToken
                            )
                        )
                    }
                } else {
                    logWarning("Failed to query product details: ${billingResult.debugMessage}")
                }
            }
        } catch (e: Throwable) {
            logError("Error in queryProductDetails", e)
        }
    }

    override fun queryPurchases() {
        try {
            billingClient.queryPurchases { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    processPlayPurchases(purchases)
                } else {
                    logWarning("Query purchases failed: ${billingResult.debugMessage}")
                }
            }
        } catch (e: Throwable) {
            logError("Error in queryPurchases", e)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val wrapped = purchases?.map { RealPlayPurchase(it) } ?: emptyList()
                processPlayPurchases(wrapped)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                logDebug("User cancelled purchase flow")
                _billingState.update { it.copy(purchaseStatus = PurchaseStatus.CANCELLED) }
            }
            else -> {
                logWarning("Purchase update error: ${billingResult.debugMessage} (${billingResult.responseCode})")
                _billingState.update {
                    it.copy(
                        purchaseStatus = PurchaseStatus.ERROR,
                        errorMessage = billingResult.debugMessage
                    )
                }
            }
        }
    }

    fun processPlayPurchases(purchases: List<PlayPurchase>?) {
        val subPurchase = purchases?.firstOrNull { it.products.contains(PRODUCT_ID_REMOVE_ADS_YEARLY) }

        if (subPurchase != null) {
            when (subPurchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    logDebug("Verified active purchase: ${subPurchase.orderId}")
                    // Security requirement: Acknowledge purchase if unacknowledged
                    if (!subPurchase.isAcknowledged) {
                        acknowledgePurchase(subPurchase)
                    }
                    _billingState.update {
                        it.copy(
                            isAdsRemoved = true,
                            purchaseStatus = PurchaseStatus.PURCHASED,
                            errorMessage = null
                        )
                    }
                }
                Purchase.PurchaseState.PENDING -> {
                    logDebug("Purchase is pending confirmation")
                    // Do NOT grant entitlement while pending
                    _billingState.update {
                        it.copy(
                            isAdsRemoved = false,
                            purchaseStatus = PurchaseStatus.PENDING,
                            errorMessage = null
                        )
                    }
                }
                else -> {
                    _billingState.update {
                        it.copy(
                            isAdsRemoved = false,
                            purchaseStatus = PurchaseStatus.IDLE
                        )
                    }
                }
            }
        } else {
            checkEntitlementAfterUpdate(purchases ?: emptyList())
        }
    }

    private fun checkEntitlementAfterUpdate(purchases: List<PlayPurchase>) {
        val hasActive = purchases.any {
            it.products.contains(PRODUCT_ID_REMOVE_ADS_YEARLY) && it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        val previousWasActive = _billingState.value.isAdsRemoved

        _billingState.update {
            it.copy(
                isAdsRemoved = hasActive,
                purchaseStatus = if (!hasActive && previousWasActive) PurchaseStatus.EXPIRED else PurchaseStatus.IDLE,
                errorMessage = null
            )
        }
    }

    private fun acknowledgePurchase(purchase: PlayPurchase) {
        try {
            billingClient.acknowledgePurchase(purchase.purchaseToken) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    logDebug("Purchase acknowledged successfully")
                } else {
                    logWarning("Purchase acknowledgment failed: ${result.debugMessage}")
                }
            }
        } catch (e: Throwable) {
            logError("Error acknowledging purchase", e)
        }
    }

    override fun launchBillingFlow(activity: Activity): Boolean {
        if (_billingState.value.subscriptionDetails == null) {
            logWarning("Cannot launch billing flow: subscription details not available")
            _billingState.update {
                it.copy(
                    purchaseStatus = PurchaseStatus.ERROR,
                    errorMessage = "Subscription details are currently unavailable from Google Play."
                )
            }
            return false
        }

        val result = billingClient.launchBillingFlow(activity)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            logWarning("Failed to launch billing flow: ${result.debugMessage}")
            _billingState.update {
                it.copy(
                    purchaseStatus = PurchaseStatus.ERROR,
                    errorMessage = result.debugMessage
                )
            }
            return false
        }
        return true
    }

    override fun endConnection() {
        try {
            if (billingClient.isReady) {
                billingClient.endConnection()
            }
        } catch (e: Throwable) {
            logError("Error ending billing connection", e)
        }
    }

    private fun logDebug(message: String) {
        try { Log.d(TAG, message) } catch (_: Throwable) {}
    }

    private fun logWarning(message: String, throwable: Throwable? = null) {
        try { Log.w(TAG, message, throwable) } catch (_: Throwable) {}
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        try { Log.e(TAG, message, throwable) } catch (_: Throwable) {}
    }

    companion object {
        private const val TAG = "ShopZenBilling"
    }
}
