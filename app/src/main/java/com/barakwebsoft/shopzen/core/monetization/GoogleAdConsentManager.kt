package com.barakwebsoft.shopzen.core.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Production implementation of [AdConsentManager] backed by the Google User Messaging Platform (UMP) SDK.
 *
 * Privacy Guarantees:
 * 1. MobileAds.initialize() is only called when canRequestAds() evaluates to true.
 * 2. MobileAds is guarded by [AtomicBoolean] to ensure exactly one initialization per application process.
 * 3. Offline resilience: network or consent gathering failures gracefully degrade and never block the app.
 * 4. Zero list, price, or item data is passed to ad requests.
 */
class GoogleAdConsentManager(
    private val context: Context,
    private val consentInformationProvider: (Context) -> ConsentInformation = { UserMessagingPlatform.getConsentInformation(it) },
    private val mobileAdsInitializer: (Context, () -> Unit) -> Unit = { ctx, onInit ->
        try {
            MobileAds.initialize(ctx) { onInit() }
        } catch (e: Throwable) {
            try { Log.e(TAG, "Error initializing MobileAds", e) } catch (_: Throwable) {}
        }
    }
) : AdConsentManager {

    private val isMobileAdsInitializedFlag = AtomicBoolean(false)

    private val consentInformation: ConsentInformation by lazy {
        consentInformationProvider(context)
    }

    override fun canRequestAds(): Boolean {
        return try {
            consentInformation.canRequestAds()
        } catch (e: Exception) {
            logWarning("Failed to read canRequestAds", e)
            false
        }
    }

    override fun isMobileAdsInitialized(): Boolean {
        return isMobileAdsInitializedFlag.get()
    }

    override fun isPrivacyOptionsRequired(): Boolean {
        return try {
            consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } catch (e: Exception) {
            logWarning("Failed to read privacyOptionsRequirementStatus", e)
            false
        }
    }

    override fun requestConsentAndInitAds(activity: Activity, onComplete: () -> Unit) {
        // Fast-path: Check immediately if consent was already gathered in a previous session
        if (canRequestAds()) {
            initMobileAds(activity.applicationContext)
        }

        val params = ConsentRequestParameters.Builder().build()

        try {
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    // Consent update succeeded; present the form if required by region/regulations
                    try {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                            if (formError != null) {
                                logWarning("Consent form returned code ${formError.errorCode}: ${formError.message}")
                            }
                            if (canRequestAds()) {
                                initMobileAds(activity.applicationContext)
                            }
                            onComplete()
                        }
                    } catch (e: Exception) {
                        logWarning("Error showing consent form", e)
                        if (canRequestAds()) {
                            initMobileAds(activity.applicationContext)
                        }
                        onComplete()
                    }
                },
                { requestConsentError ->
                    // Offline or network error: never crash, keep offline list workflows completely functional
                    logWarning("Consent info update failed: ${requestConsentError.message}")
                    if (canRequestAds()) {
                        initMobileAds(activity.applicationContext)
                    }
                    onComplete()
                }
            )
        } catch (e: Exception) {
            logError("Exception during requestConsentInfoUpdate", e)
            if (canRequestAds()) {
                initMobileAds(activity.applicationContext)
            }
            onComplete()
        }
    }

    override fun showPrivacyOptionsForm(activity: Activity, onComplete: (Exception?) -> Unit) {
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                if (formError != null) {
                    onComplete(Exception("Privacy options error (${formError.errorCode}): ${formError.message}"))
                } else {
                    onComplete(null)
                }
            }
        } catch (e: Exception) {
            logError("Exception showing privacy options form", e)
            onComplete(e)
        }
    }

    fun initMobileAds(appContext: Context) {
        if (!isMobileAdsInitializedFlag.getAndSet(true)) {
            logDebug("Initializing MobileAds SDK with user consent")
            mobileAdsInitializer(appContext) {
                logDebug("MobileAds initialization complete")
            }
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
        private const val TAG = "ShopZenConsent"
    }
}

