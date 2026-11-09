package com.barakwebsoft.shopzen.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Domain representation of Play Store product details.
 */
interface PlayProductDetails {
    val productId: String
    val title: String
    val description: String
    val formattedPrice: String?
    val priceCurrencyCode: String?
    val priceAmountMicros: Long?
    val offerToken: String?
}

/**
 * Domain representation of Play Store purchase data.
 */
interface PlayPurchase {
    val orderId: String?
    val products: List<String>
    val purchaseState: Int
    val isAcknowledged: Boolean
    val purchaseToken: String
}

class RealPlayProductDetails(val details: ProductDetails) : PlayProductDetails {
    override val productId: String get() = details.productId
    override val title: String get() = details.title
    override val description: String get() = details.description
    override val formattedPrice: String?
        get() = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
    override val priceCurrencyCode: String?
        get() = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceCurrencyCode
    override val priceAmountMicros: Long?
        get() = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros
    override val offerToken: String?
        get() = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
}

class RealPlayPurchase(val purchase: Purchase) : PlayPurchase {
    override val orderId: String? get() = purchase.orderId
    override val products: List<String> get() = purchase.products
    override val purchaseState: Int get() = purchase.purchaseState
    override val isAcknowledged: Boolean get() = purchase.isAcknowledged
    override val purchaseToken: String get() = purchase.purchaseToken
}

/**
 * Abstraction layer over [BillingClient] to enable fully isolated, fast, and deterministic
 * unit testing without runtime bytecode manipulation or unmocked Android JSON stubs.
 */
interface PlayBillingClientWrapper {
    val isReady: Boolean
    fun startConnection(onSetupFinished: (BillingResult) -> Unit, onDisconnected: () -> Unit)
    fun endConnection()
    fun queryProductDetails(onResult: (BillingResult, List<PlayProductDetails>) -> Unit)
    fun queryPurchases(onResult: (BillingResult, List<PlayPurchase>) -> Unit)
    fun acknowledgePurchase(purchaseToken: String, onResult: (BillingResult) -> Unit)
    fun launchBillingFlow(activity: Activity): BillingResult
}

class DefaultPlayBillingClientWrapper(
    context: Context,
    listener: PurchasesUpdatedListener
) : PlayBillingClientWrapper {

    private var cachedRawProductDetails: ProductDetails? = null

    private val billingClient: BillingClient = run {
        val pendingParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .enablePrepaidPlans()
            .build()

        BillingClient.newBuilder(context)
            .setListener(listener)
            .enablePendingPurchases(pendingParams)
            .build()
    }

    override val isReady: Boolean
        get() = billingClient.isReady

    override fun startConnection(onSetupFinished: (BillingResult) -> Unit, onDisconnected: () -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                onSetupFinished(billingResult)
            }

            override fun onBillingServiceDisconnected() {
                onDisconnected()
            }
        })
    }

    override fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    override fun queryProductDetails(onResult: (BillingResult, List<PlayProductDetails>) -> Unit) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_REMOVE_ADS_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            val wrapped = productDetailsList?.map {
                cachedRawProductDetails = it
                RealPlayProductDetails(it)
            } ?: emptyList()
            onResult(billingResult, wrapped)
        }
    }

    override fun queryPurchases(onResult: (BillingResult, List<PlayPurchase>) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            val wrapped = purchases?.map { RealPlayPurchase(it) } ?: emptyList()
            onResult(billingResult, wrapped)
        }
    }

    override fun acknowledgePurchase(purchaseToken: String, onResult: (BillingResult) -> Unit) {
        val ackParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.acknowledgePurchase(ackParams) { result ->
            onResult(result)
        }
    }

    override fun launchBillingFlow(activity: Activity): BillingResult {
        val details = cachedRawProductDetails
        val offerToken = details?.subscriptionOfferDetails?.firstOrNull()?.offerToken

        if (details == null || offerToken == null) {
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.DEVELOPER_ERROR)
                .setDebugMessage("ProductDetails or offerToken not cached")
                .build()
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        return billingClient.launchBillingFlow(activity, flowParams)
    }
}
