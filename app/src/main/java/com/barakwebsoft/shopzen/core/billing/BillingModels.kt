package com.barakwebsoft.shopzen.core.billing

/**
 * Standard product identifier for the yearly Remove Ads subscription in Google Play Console.
 */
const val PRODUCT_ID_REMOVE_ADS_YEARLY = "remove_ads_yearly"

/**
 * Lifecycle states for Play Billing subscription purchases per PRD Section 5 and SECURITY.md Section 6.
 */
enum class PurchaseStatus {
    IDLE,
    PENDING,
    PURCHASED,
    CANCELLED,
    EXPIRED,
    UNAVAILABLE,
    ERROR
}

/**
 * Holds dynamic localized subscription details retrieved from Google Play.
 * Mandated by PRD Section 5: "Do not hardcode subscription price; use the localized price returned by Google Play."
 */
data class SubscriptionDetails(
    val productId: String,
    val formattedPrice: String,
    val priceCurrencyCode: String,
    val priceAmountMicros: Long,
    val title: String,
    val description: String,
    val offerToken: String
)

/**
 * Immutable UI state exposing the verified Google Play purchase entitlement.
 * Security rule: A local boolean alone is never trusted as proof of purchase;
 * [isAdsRemoved] is derived strictly from verified cryptographic Play purchase state.
 */
data class BillingState(
    val isAdsRemoved: Boolean = false,
    val purchaseStatus: PurchaseStatus = PurchaseStatus.IDLE,
    val subscriptionDetails: SubscriptionDetails? = null,
    val errorMessage: String? = null
)
