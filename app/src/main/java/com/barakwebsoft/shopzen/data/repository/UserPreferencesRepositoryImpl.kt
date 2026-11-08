package com.barakwebsoft.shopzen.data.repository

import com.barakwebsoft.shopzen.data.preferences.UserPreferencesDataStore
import com.barakwebsoft.shopzen.domain.model.AppPreferences
import com.barakwebsoft.shopzen.domain.model.ThemePreference
import com.barakwebsoft.shopzen.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class UserPreferencesRepositoryImpl(
    private val preferencesDataStore: UserPreferencesDataStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserPreferencesRepository {

    override fun getPreferences(): Flow<AppPreferences> {
        return preferencesDataStore.userPreferencesFlow
            .flowOn(ioDispatcher)
    }

    override suspend fun setThemePreference(theme: ThemePreference) = withContext(ioDispatcher) {
        preferencesDataStore.setThemePreference(theme)
    }

    override suspend fun setDefaultCurrency(currencyCode: String) = withContext(ioDispatcher) {
        preferencesDataStore.setDefaultCurrency(currencyCode)
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        preferencesDataStore.setHapticFeedbackEnabled(enabled)
    }

    override suspend fun setShowCompletedItems(show: Boolean) = withContext(ioDispatcher) {
        preferencesDataStore.setShowCompletedItems(show)
    }
}

