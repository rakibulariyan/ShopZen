package com.barakwebsoft.shopzen.domain.repository

import com.barakwebsoft.shopzen.domain.model.AppPreferences
import com.barakwebsoft.shopzen.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getPreferences(): Flow<AppPreferences>
    suspend fun setThemePreference(theme: ThemePreference)
    suspend fun setDefaultCurrency(currencyCode: String)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setShowCompletedItems(show: Boolean)
}

