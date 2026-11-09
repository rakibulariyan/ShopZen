package com.barakwebsoft.shopzen.core.monetization

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Unit tests for [GoogleAdConsentManager] verifying privacy-safe initialization,
 * UMP consent checks, atomic single-initialization guards, and offline resilience.
 */
class GoogleAdConsentManagerTest {

    private lateinit var fakeConsentInfo: FakeConsentInformation
    private var mockInitCallCount = AtomicInteger(0)

    @Before
    fun setUp() {
        fakeConsentInfo = FakeConsentInformation()
        mockInitCallCount.set(0)
    }

    private fun createManager(
        canRequestAds: Boolean = false,
        privacyOptionsRequired: Boolean = false,
        simulateSuccess: Boolean = true
    ): GoogleAdConsentManager {
        fakeConsentInfo.canRequestAdsValue = canRequestAds
        fakeConsentInfo.privacyOptionsStatus = if (privacyOptionsRequired) {
            PrivacyOptionsRequirementStatus.REQUIRED
        } else {
            PrivacyOptionsRequirementStatus.NOT_REQUIRED
        }
        fakeConsentInfo.shouldSucceed = simulateSuccess

        return GoogleAdConsentManager(
            context = FakeContext(),
            consentInformationProvider = { fakeConsentInfo },
            mobileAdsInitializer = { _, onInit ->
                mockInitCallCount.incrementAndGet()
                onInit()
            }
        )
    }

    @Test
    fun canRequestAds_reflectsConsentInformationState() {
        val managerDisallowed = createManager(canRequestAds = false)
        assertFalse(managerDisallowed.canRequestAds())

        val managerAllowed = createManager(canRequestAds = true)
        assertTrue(managerAllowed.canRequestAds())
    }

    @Test
    fun isMobileAdsInitialized_initiallyFalse() {
        val manager = createManager(canRequestAds = false)
        assertFalse(manager.isMobileAdsInitialized())
    }

    @Test
    fun isPrivacyOptionsRequired_reflectsConsentInformationStatus() {
        val managerNotRequired = createManager(privacyOptionsRequired = false)
        assertFalse(managerNotRequired.isPrivacyOptionsRequired())

        val managerRequired = createManager(privacyOptionsRequired = true)
        assertTrue(managerRequired.isPrivacyOptionsRequired())
    }

    @Test
    fun initMobileAds_isGuardedByAtomicFlagAndExecutesExactlyOnce() {
        val manager = createManager(canRequestAds = true)
        assertFalse(manager.isMobileAdsInitialized())

        manager.initMobileAds(FakeContext())
        assertTrue(manager.isMobileAdsInitialized())
        assertEquals(1, mockInitCallCount.get())

        // Subsequent calls must be ignored
        manager.initMobileAds(FakeContext())
        manager.initMobileAds(FakeContext())
        assertEquals("MobileAds.initialize must only be called once per process", 1, mockInitCallCount.get())
    }

    @Test
    fun requestConsentAndInitAds_fastPathInitializesMobileAdsIfConsentAlreadyGranted() {
        val manager = createManager(canRequestAds = true)
        var completed = false

        manager.requestConsentAndInitAds(FakeActivity()) {
            completed = true
        }

        assertTrue("MobileAds must be initialized on fast-path", manager.isMobileAdsInitialized())
        assertEquals(1, mockInitCallCount.get())
        assertTrue("Callback must be invoked", completed)
    }

    @Test
    fun requestConsentAndInitAds_offlineFailureDoesNotCrashAndInvokesCallback() {
        val manager = createManager(canRequestAds = false, simulateSuccess = false)
        var completed = false

        // In offline mode, requestConsentInfoUpdate fails, but callback is still safely invoked
        manager.requestConsentAndInitAds(FakeActivity()) {
            completed = true
        }

        assertFalse("Ads should not initialize if consent cannot be requested", manager.isMobileAdsInitialized())
        assertEquals(0, mockInitCallCount.get())
        assertTrue("Callback must be invoked even upon consent update failure", completed)
    }

    @Test
    fun requestConsentAndInitAds_offlineWithSavedConsent_stillInitializesViaFastPath() {
        // Scenario: Returning user opens app offline, but previously granted consent
        val manager = createManager(canRequestAds = true, simulateSuccess = false)
        var completed = false

        manager.requestConsentAndInitAds(FakeActivity()) {
            completed = true
        }

        assertTrue("MobileAds must initialize from previously saved consent despite offline update failure", manager.isMobileAdsInitialized())
        assertEquals(1, mockInitCallCount.get())
        assertTrue("Callback must complete", completed)
    }

    // --- Test Doubles ---

    private class FakeContext : android.content.ContextWrapper(null) {
        override fun getApplicationContext(): Context = this
    }

    private class FakeActivity : Activity() {
        override fun getApplicationContext(): Context = FakeContext()
    }

    private class FakeConsentInformation : ConsentInformation {
        var canRequestAdsValue: Boolean = false
        var privacyOptionsStatus: PrivacyOptionsRequirementStatus = PrivacyOptionsRequirementStatus.NOT_REQUIRED
        var shouldSucceed: Boolean = true

        override fun canRequestAds(): Boolean = canRequestAdsValue

        override fun getPrivacyOptionsRequirementStatus(): PrivacyOptionsRequirementStatus = privacyOptionsStatus

        override fun getConsentStatus(): Int = if (canRequestAdsValue) ConsentInformation.ConsentStatus.OBTAINED else ConsentInformation.ConsentStatus.REQUIRED

        override fun isConsentFormAvailable(): Boolean = true

        override fun requestConsentInfoUpdate(
            activity: Activity,
            params: ConsentRequestParameters,
            onSuccess: ConsentInformation.OnConsentInfoUpdateSuccessListener,
            onFailure: ConsentInformation.OnConsentInfoUpdateFailureListener
        ) {
            if (shouldSucceed) {
                onSuccess.onConsentInfoUpdateSuccess()
            } else {
                onFailure.onConsentInfoUpdateFailure(FakeFormError(1, "Offline / No connection"))
            }
        }

        override fun reset() {
            canRequestAdsValue = false
            privacyOptionsStatus = PrivacyOptionsRequirementStatus.NOT_REQUIRED
        }
    }

    private class FakeFormError(private val code: Int, private val msg: String) : FormError(code, msg) {
        override fun getErrorCode(): Int = code
        override fun getMessage(): String = msg
    }
}

