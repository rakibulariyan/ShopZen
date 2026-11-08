package com.barakwebsoft.shopzen.domain.model

data class AppPreferences(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val defaultCurrencyCode: String = "USD",
    val hapticFeedbackEnabled: Boolean = true,
    val showCompletedItems: Boolean = true
)

