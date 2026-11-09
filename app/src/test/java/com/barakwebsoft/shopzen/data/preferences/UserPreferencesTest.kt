package com.barakwebsoft.shopzen.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.barakwebsoft.shopzen.data.repository.UserPreferencesRepositoryImpl
import com.barakwebsoft.shopzen.domain.model.AppPreferences
import com.barakwebsoft.shopzen.domain.model.ThemePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UserPreferencesTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun themePreference_hasExpectedValues() {
        val themes = ThemePreference.values()
        assertEquals(3, themes.size)
        assertTrue(themes.contains(ThemePreference.SYSTEM))
        assertTrue(themes.contains(ThemePreference.LIGHT))
        assertTrue(themes.contains(ThemePreference.DARK))
    }

    @Test
    fun themeResolution_correctlyMapsSystemLightAndDark() {
        fun resolveDarkTheme(pref: ThemePreference, isSystemInDarkTheme: Boolean): Boolean {
            return when (pref) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }
        }

        // System follows system
        assertTrue(resolveDarkTheme(ThemePreference.SYSTEM, isSystemInDarkTheme = true))
        assertFalse(resolveDarkTheme(ThemePreference.SYSTEM, isSystemInDarkTheme = false))

        // Light is always light
        assertFalse(resolveDarkTheme(ThemePreference.LIGHT, isSystemInDarkTheme = true))
        assertFalse(resolveDarkTheme(ThemePreference.LIGHT, isSystemInDarkTheme = false))

        // Dark is always dark
        assertTrue(resolveDarkTheme(ThemePreference.DARK, isSystemInDarkTheme = true))
        assertTrue(resolveDarkTheme(ThemePreference.DARK, isSystemInDarkTheme = false))
    }

    @Test
    fun appPreferences_defaultsAreCorrect() {
        val prefs = AppPreferences()
        assertEquals(ThemePreference.SYSTEM, prefs.themePreference)
        assertEquals("USD", prefs.defaultCurrencyCode)
        assertTrue(prefs.hapticFeedbackEnabled)
        assertTrue(prefs.showCompletedItems)
    }

    @Test
    fun defaultLocaleCurrencyCode_returnsValidCurrency() {
        val currencyCode = UserPreferencesDataStore.getDefaultLocaleCurrencyCode()
        assertNotNull(currencyCode)
        assertEquals(3, currencyCode.length)
        assertTrue(currencyCode.all { it.isLetter() })
    }

    @Test
    fun userPreferencesDataStore_persistsAndUpdatesSettings() = runBlocking {
        val testFile = tempFolder.newFile("test_user_prefs.preferences_pb")
        val testScope = CoroutineScope(Dispatchers.Unconfined + Job())
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )

        val preferencesDataStore = UserPreferencesDataStore(dataStore)
        val repository = UserPreferencesRepositoryImpl(preferencesDataStore, Dispatchers.Unconfined)

        // Initial default observation
        val initial = repository.getPreferences().first()
        assertEquals(ThemePreference.SYSTEM, initial.themePreference)
        assertTrue(initial.hapticFeedbackEnabled)
        assertTrue(initial.showCompletedItems)

        // Update theme to DARK
        repository.setThemePreference(ThemePreference.DARK)
        assertEquals(ThemePreference.DARK, repository.getPreferences().first().themePreference)

        // Update default currency to EUR
        repository.setDefaultCurrency("EUR")
        assertEquals("EUR", repository.getPreferences().first().defaultCurrencyCode)

        // Update haptic feedback to false
        repository.setHapticFeedbackEnabled(false)
        assertFalse(repository.getPreferences().first().hapticFeedbackEnabled)

        // Update show completed to false
        repository.setShowCompletedItems(false)
        assertFalse(repository.getPreferences().first().showCompletedItems)

        // Restore to true
        repository.setHapticFeedbackEnabled(true)
        repository.setShowCompletedItems(true)
        repository.setThemePreference(ThemePreference.LIGHT)

        val updated = repository.getPreferences().first()
        assertEquals(ThemePreference.LIGHT, updated.themePreference)
        assertEquals("EUR", updated.defaultCurrencyCode)
        assertTrue(updated.hapticFeedbackEnabled)
        assertTrue(updated.showCompletedItems)
    }
}

