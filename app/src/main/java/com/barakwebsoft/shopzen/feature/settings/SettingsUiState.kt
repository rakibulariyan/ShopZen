package com.barakwebsoft.shopzen.feature.settings

import com.barakwebsoft.shopzen.core.billing.PurchaseStatus
import com.barakwebsoft.shopzen.core.util.CurrencyFormatter
import com.barakwebsoft.shopzen.core.util.CurrencyInfo
import com.barakwebsoft.shopzen.domain.model.ThemePreference

data class SettingsUiState(
    val isLoading: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val defaultCurrencyCode: String = "USD",
    val hapticFeedbackEnabled: Boolean = true,
    val showCompletedItems: Boolean = true,
    val isCurrencyPickerVisible: Boolean = false,
    val isThemeDialogVisible: Boolean = false,
    val isAdsRemoved: Boolean = false,
    val purchaseStatus: PurchaseStatus = PurchaseStatus.IDLE,
    val subscriptionPriceFormatted: String? = null,
    val subscriptionTitle: String? = null
) {
    val currencyInfo: CurrencyInfo
        get() = CurrencyFormatter.getCurrencyInfo(defaultCurrencyCode)
}

