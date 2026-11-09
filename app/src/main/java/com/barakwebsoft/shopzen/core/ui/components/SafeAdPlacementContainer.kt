package com.barakwebsoft.shopzen.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.barakwebsoft.shopzen.core.policy.ProtectedScreenPolicy

/**
 * Architectural container that guarantees ads are never rendered on protected screens.
 *
 * Mandated by PRD Sections 7 & 8, DESIGN Section 8, and ADR-009:
 * Protected screens (Market Mode, Add/Edit Item, Budget screen, Purchase screen) must never contain ads.
 *
 * If the [currentRoute] is a protected destination, this composable emits nothing and prevents
 * any child ad view or SDK loader from being invoked or mounted into the composition tree.
 */
@Composable
fun SafeAdPlacementContainer(
    currentRoute: String?,
    modifier: Modifier = Modifier,
    adContent: @Composable () -> Unit
) {
    if (!ProtectedScreenPolicy.canShowAd(currentRoute)) {
        // Enforce zero-ads policy: do not emit child content on protected screens or undefined routes
        return
    }

    Box(modifier = modifier) {
        adContent()
    }
}

