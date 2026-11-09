package com.barakwebsoft.shopzen.core.monetization

import android.app.Activity

/**
 * Interface defining the Google Mobile Ads and User Messaging Platform (UMP) consent contract.
 * Enforces privacy-safe, consent-first initialization and provides privacy options access.
 */
interface AdConsentManager {
    /**
     * Whether ads can currently be requested according to UMP consent status.
     */
    fun canRequestAds(): Boolean

    /**
     * Whether the Google Mobile Ads SDK has already been initialized in the current process.
     */
    fun isMobileAdsInitialized(): Boolean

    /**
     * Requests updated consent info and presents the consent form if required by law/region (e.g. EEA/UK).
     * If consent is granted or not required, initializes Google Mobile Ads SDK safely.
     * Offline resilient: if network/consent request fails, core app functionality is never blocked.
     */
    fun requestConsentAndInitAds(activity: Activity, onComplete: () -> Unit = {})

    /**
     * Whether the user can change their consent preferences (e.g. via Privacy Options in Settings).
     */
    fun isPrivacyOptionsRequired(): Boolean

    /**
     * Displays the UMP privacy options form allowing the user to modify or revoke consent.
     */
    fun showPrivacyOptionsForm(activity: Activity, onComplete: (Exception?) -> Unit = {})
}

