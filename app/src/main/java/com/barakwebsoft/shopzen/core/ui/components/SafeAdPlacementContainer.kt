package com.barakwebsoft.shopzen.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.barakwebsoft.shopzen.core.policy.ProtectedScreenPolicy

/**
 * Architectural container that guarantees ads are never rendered on protected screens.
 *
 * Mandated by PRD Sections 5 & 6, DESIGN Section 8, and ADR-009 / ADR-015:
 * Protected screens (Market Mode, Add/Edit Item, Budget screen, Purchase screen) must never contain ads.
 *
 * If [currentRoute] is a protected destination, or if [isAdsRemoved] is true, or if [canRequestAds] is false,
 * this composable emits nothing and prevents child ad views from mounting.
 */
@Composable
fun SafeAdPlacementContainer(
    currentRoute: String?,
    modifier: Modifier = Modifier,
    isAdsRemoved: Boolean = false,
    canRequestAds: Boolean = true,
    adContent: @Composable () -> Unit
) {
    if (isAdsRemoved) {
        // User purchased Remove Ads: suppress all ads
        return
    }

    if (!canRequestAds) {
        // Consent not granted or unavailable: suppress ads
        return
    }

    if (!ProtectedScreenPolicy.canShowAd(currentRoute)) {
        // Enforce zero-ads policy: do not emit child content on protected screens or undefined routes
        return
    }

    Box(modifier = modifier) {
        adContent()
    }
}

