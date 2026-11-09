package com.barakwebsoft.shopzen.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GooglePlayBillingManager] testing Play Billing 7.x lifecycle,
 * localized dynamic pricing extraction, and strict purchase state security.
 *
 * Rules verified:
 * 1. Cryptographic purchase proof (`PURCHASED`) is required; a local boolean alone is never trusted.
 * 2. Pending purchases do NOT grant entitlement.
 * 3. Dynamic Play-provided localized price is parsed and exposed.
 * 4. Unacknowledged purchases trigger Play acknowledgment.
 * 5. Lifecycle states (CANCELLED, EXPIRED, UNAVAILABLE) are handled correctly.
 */
class GooglePlayBillingManagerTest {

    private lateinit var fakeClientWrapper: FakePlayBillingClientWrapper
    private lateinit var billingManager: GooglePlayBillingManager

    @Before
    fun setUp() {
        fakeClientWrapper = FakePlayBillingClientWrapper()
        billingManager = GooglePlayBillingManager(
            context = FakeContext(),
            clientWrapperFactory = { fakeClientWrapper }
        )
    }

    private fun createBillingResult(responseCode: Int, debugMessage: String = ""): BillingResult {
        return BillingResult.newBuilder()
            .setResponseCode(responseCode)
            .setDebugMessage(debugMessage)
            .build()
    }

    @Test
    fun onBillingSetupFinished_whenOk_queriesProductDetailsAndPurchases() {
        val okResult = createBillingResult(BillingClient.BillingResponseCode.OK)
        billingManager.onBillingSetupFinished(okResult)

        assertTrue(fakeClientWrapper.queryProductDetailsCalled)
        assertTrue(fakeClientWrapper.queryPurchasesCalled)
    }

    @Test
    fun onBillingSetupFinished_whenUnavailable_updatesStateToUnavailable() {
        val unavailableResult = createBillingResult(
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            "Play Store unavailable"
        )
        billingManager.onBillingSetupFinished(unavailableResult)

        val state = billingManager.billingState.value
        assertEquals(PurchaseStatus.UNAVAILABLE, state.purchaseStatus)
        assertEquals("Play Store unavailable", state.errorMessage)
        assertFalse(state.isAdsRemoved)
    }

    @Test
    fun queryProductDetails_parsesLocalizedDynamicPriceAndOfferDetails() {
        val testDetails = FakePlayProductDetails(
            productId = PRODUCT_ID_REMOVE_ADS_YEARLY,
            title = "ShopZen Premium (Remove Ads)",
            description = "Yearly ad-free subscription",
            formattedPrice = "$4.99",
            priceCurrencyCode = "USD",
            priceAmountMicros = 4990000L,
            offerToken = "test_token_abc"
        )
        fakeClientWrapper.productDetailsList = listOf(testDetails)

        billingManager.queryProductDetails()

        val state = billingManager.billingState.value
        val details = state.subscriptionDetails
        assertNotNull(details)
        assertEquals("$4.99", details?.formattedPrice)
        assertEquals("USD", details?.priceCurrencyCode)
        assertEquals(4990000L, details?.priceAmountMicros)
        assertEquals("test_token_abc", details?.offerToken)
        assertEquals("ShopZen Premium (Remove Ads)", details?.title)
    }

    @Test
    fun processPlayPurchases_whenPurchasedAndUnacknowledged_acknowledgesAndGrantsEntitlement() {
        val purchase = FakePlayPurchase(
            orderId = "GPA.1234-5678-9012",
            products = listOf(PRODUCT_ID_REMOVE_ADS_YEARLY),
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = false,
            purchaseToken = "mock_token_123"
        )

        billingManager.processPlayPurchases(listOf(purchase))

        val state = billingManager.billingState.value
        assertTrue(state.isAdsRemoved)
        assertEquals(PurchaseStatus.PURCHASED, state.purchaseStatus)
        assertNull(state.errorMessage)

        assertTrue(fakeClientWrapper.acknowledgePurchaseCalled)
        assertEquals("mock_token_123", fakeClientWrapper.lastAcknowledgedToken)
    }

    @Test
    fun processPlayPurchases_whenAlreadyAcknowledged_doesNotAcknowledgeAgain() {
        val purchase = FakePlayPurchase(
            orderId = "GPA.1234-5678-9012",
            products = listOf(PRODUCT_ID_REMOVE_ADS_YEARLY),
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = true,
            purchaseToken = "mock_token_123"
        )

        billingManager.processPlayPurchases(listOf(purchase))

        val state = billingManager.billingState.value
        assertTrue(state.isAdsRemoved)
        assertEquals(PurchaseStatus.PURCHASED, state.purchaseStatus)

        assertFalse(fakeClientWrapper.acknowledgePurchaseCalled)
    }

    @Test
    fun processPlayPurchases_whenPending_doesNotGrantEntitlement() {
        val purchase = FakePlayPurchase(
            orderId = "GPA.1234-5678-9012",
            products = listOf(PRODUCT_ID_REMOVE_ADS_YEARLY),
            purchaseState = Purchase.PurchaseState.PENDING,
            isAcknowledged = false,
            purchaseToken = "mock_token_123"
        )

        billingManager.processPlayPurchases(listOf(purchase))

        val state = billingManager.billingState.value
        assertFalse("Entitlement must NOT be granted while purchase is PENDING", state.isAdsRemoved)
        assertEquals(PurchaseStatus.PENDING, state.purchaseStatus)
    }

    @Test
    fun onPurchasesUpdated_whenUserCancelled_setsCancelledStatus() {
        val cancelResult = createBillingResult(BillingClient.BillingResponseCode.USER_CANCELED)
        billingManager.onPurchasesUpdated(cancelResult, null)

        val state = billingManager.billingState.value
        assertEquals(PurchaseStatus.CANCELLED, state.purchaseStatus)
        assertFalse(state.isAdsRemoved)
    }

    @Test
    fun queryPurchases_whenPreviousWasActiveAndNowEmpty_setsExpired() {
        // First grant entitlement
        val activePurchase = FakePlayPurchase(
            orderId = "GPA.1234",
            products = listOf(PRODUCT_ID_REMOVE_ADS_YEARLY),
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = true,
            purchaseToken = "token"
        )
        billingManager.processPlayPurchases(listOf(activePurchase))
        assertTrue(billingManager.billingState.value.isAdsRemoved)

        // Now simulate queryPurchases returning empty list (user subscription lapsed / cancelled)
        fakeClientWrapper.purchasesList = emptyList()
        billingManager.queryPurchases()

        val state = billingManager.billingState.value
        assertFalse(state.isAdsRemoved)
        assertEquals(PurchaseStatus.EXPIRED, state.purchaseStatus)
    }

    @Test
    fun launchBillingFlow_whenProductDetailsAvailable_launchesAndReturnsTrue() {
        fakeClientWrapper.productDetailsList = listOf(
            FakePlayProductDetails(
                productId = PRODUCT_ID_REMOVE_ADS_YEARLY,
                title = "ShopZen Premium",
                description = "Ad-free",
                formattedPrice = "$4.99",
                priceCurrencyCode = "USD",
                priceAmountMicros = 4990000L,
                offerToken = "test_token"
            )
        )
        billingManager.queryProductDetails()

        val success = billingManager.launchBillingFlow(FakeActivity())
        assertTrue(success)
        assertTrue(fakeClientWrapper.launchBillingFlowCalled)
    }

    @Test
    fun launchBillingFlow_whenProductDetailsNotAvailable_returnsFalseWithError() {
        val success = billingManager.launchBillingFlow(FakeActivity())

        assertFalse(success)
        val state = billingManager.billingState.value
        assertEquals(PurchaseStatus.ERROR, state.purchaseStatus)
        assertNotNull(state.errorMessage)
    }

    // --- Pure Test Doubles ---

    private class FakePlayProductDetails(
        override val productId: String,
        override val title: String,
        override val description: String,
        override val formattedPrice: String?,
        override val priceCurrencyCode: String?,
        override val priceAmountMicros: Long?,
        override val offerToken: String?
    ) : PlayProductDetails

    private class FakePlayPurchase(
        override val orderId: String?,
        override val products: List<String>,
        override val purchaseState: Int,
        override val isAcknowledged: Boolean,
        override val purchaseToken: String
    ) : PlayPurchase

    private class FakePlayBillingClientWrapper : PlayBillingClientWrapper {
        override var isReady: Boolean = true

        var startConnectionCalled = false
        var endConnectionCalled = false
        var queryProductDetailsCalled = false
        var queryPurchasesCalled = false
        var acknowledgePurchaseCalled = false
        var launchBillingFlowCalled = false

        var productDetailsResult: BillingResult = BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()
        var productDetailsList: List<PlayProductDetails> = emptyList()

        var purchasesResult: BillingResult = BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()
        var purchasesList: List<PlayPurchase> = emptyList()

        var acknowledgeResult: BillingResult = BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()
        var lastAcknowledgedToken: String? = null

        var launchBillingFlowResult: BillingResult = BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()

        override fun startConnection(onSetupFinished: (BillingResult) -> Unit, onDisconnected: () -> Unit) {
            startConnectionCalled = true
            onSetupFinished(BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build())
        }

        override fun endConnection() {
            endConnectionCalled = true
        }

        override fun queryProductDetails(onResult: (BillingResult, List<PlayProductDetails>) -> Unit) {
            queryProductDetailsCalled = true
            onResult(productDetailsResult, productDetailsList)
        }

        override fun queryPurchases(onResult: (BillingResult, List<PlayPurchase>) -> Unit) {
            queryPurchasesCalled = true
            onResult(purchasesResult, purchasesList)
        }

        override fun acknowledgePurchase(purchaseToken: String, onResult: (BillingResult) -> Unit) {
            acknowledgePurchaseCalled = true
            lastAcknowledgedToken = purchaseToken
            onResult(acknowledgeResult)
        }

        override fun launchBillingFlow(activity: Activity): BillingResult {
            launchBillingFlowCalled = true
            return launchBillingFlowResult
        }
    }

    private class FakeContext : android.content.ContextWrapper(null) {
        override fun getApplicationContext(): Context = this
    }

    private class FakeActivity : Activity() {
        override fun getApplicationContext(): Context = FakeContext()
    }
}
