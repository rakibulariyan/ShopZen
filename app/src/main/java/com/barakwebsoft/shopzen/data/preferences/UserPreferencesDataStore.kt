package com.barakwebsoft.shopzen.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.barakwebsoft.shopzen.domain.model.AppPreferences
import com.barakwebsoft.shopzen.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Currency
import java.util.Locale

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "shopzen_user_preferences")

class UserPreferencesDataStore(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_THEME = stringPreferencesKey("theme_preference")
        val KEY_DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val KEY_SHOW_COMPLETED = booleanPreferencesKey("show_completed_items")

        fun getDefaultLocaleCurrencyCode(): String {
            return try {
                val locale = Locale.getDefault()
                val currency = Currency.getInstance(locale)
                currency.currencyCode
            } catch (_: Exception) {
                "USD"
            }
        }
    }

    val userPreferencesFlow: Flow<AppPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeString = preferences[KEY_THEME]
            val theme = themeString?.let {
                try {
                    ThemePreference.valueOf(it)
                } catch (_: Exception) {
                    ThemePreference.SYSTEM
                }
            } ?: ThemePreference.SYSTEM

            val defaultCurrency = preferences[KEY_DEFAULT_CURRENCY] ?: getDefaultLocaleCurrencyCode()
            val haptic = preferences[KEY_HAPTIC_FEEDBACK] ?: true
            val showCompleted = preferences[KEY_SHOW_COMPLETED] ?: true

            AppPreferences(
                themePreference = theme,
                defaultCurrencyCode = defaultCurrency,
                hapticFeedbackEnabled = haptic,
                showCompletedItems = showCompleted
            )
        }

    suspend fun setThemePreference(theme: ThemePreference) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme.name
        }
    }

    suspend fun setDefaultCurrency(currencyCode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_CURRENCY] = currencyCode
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun setShowCompletedItems(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_COMPLETED] = show
        }
    }
}

