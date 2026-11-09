package com.barakwebsoft.shopzen.feature.settings

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
    val isThemeDialogVisible: Boolean = false
) {
    val currencyInfo: CurrencyInfo
        get() = CurrencyFormatter.getCurrencyInfo(defaultCurrencyCode)
}

