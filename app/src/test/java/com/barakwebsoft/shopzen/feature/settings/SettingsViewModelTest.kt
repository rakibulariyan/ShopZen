package com.barakwebsoft.shopzen.feature.settings

import com.barakwebsoft.shopzen.domain.model.AppPreferences
import com.barakwebsoft.shopzen.domain.model.ThemePreference
import com.barakwebsoft.shopzen.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePrefsRepository: FakeUserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePrefsRepository = FakeUserPreferencesRepository()
        viewModel = SettingsViewModel(
            userPreferencesRepository = fakePrefsRepository,
            sharingStarted = SharingStarted.Eagerly
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsUserPreferences() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("USD", state.defaultCurrencyCode)
        assertEquals("$", state.currencyInfo.symbol)
        assertEquals(ThemePreference.SYSTEM, state.themePreference)
        assertTrue(state.hapticFeedbackEnabled)
        assertTrue(state.showCompletedItems)
    }

    @Test
    fun setDefaultCurrency_updatesRepositoryAndClosesPicker() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.setCurrencyPickerVisible(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isCurrencyPickerVisible)

        viewModel.setDefaultCurrency("EUR")
        advanceUntilIdle()

        assertEquals("EUR", viewModel.uiState.value.defaultCurrencyCode)
        assertEquals("EUR", fakePrefsRepository.currentPreferences.defaultCurrencyCode)
        assertEquals("€", viewModel.uiState.value.currencyInfo.symbol)
        assertFalse(viewModel.uiState.value.isCurrencyPickerVisible)
    }

    @Test
    fun setThemePreference_updatesRepositoryAndClosesDialog() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.setThemeDialogVisible(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isThemeDialogVisible)

        viewModel.setThemePreference(ThemePreference.DARK)
        advanceUntilIdle()

        assertEquals(ThemePreference.DARK, viewModel.uiState.value.themePreference)
        assertEquals(ThemePreference.DARK, fakePrefsRepository.currentPreferences.themePreference)
        assertFalse(viewModel.uiState.value.isThemeDialogVisible)
    }

    @Test
    fun setToggles_updatesRepository() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.setHapticFeedbackEnabled(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.hapticFeedbackEnabled)
        assertFalse(fakePrefsRepository.currentPreferences.hapticFeedbackEnabled)

        viewModel.setShowCompletedItems(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showCompletedItems)
        assertFalse(fakePrefsRepository.currentPreferences.showCompletedItems)
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        var currentPreferences = AppPreferences()
            private set
        private val prefsFlow = MutableStateFlow(currentPreferences)

        override fun getPreferences(): Flow<AppPreferences> = prefsFlow

        override suspend fun setThemePreference(theme: ThemePreference) {
            currentPreferences = currentPreferences.copy(themePreference = theme)
            prefsFlow.value = currentPreferences
        }

        override suspend fun setDefaultCurrency(currencyCode: String) {
            currentPreferences = currentPreferences.copy(defaultCurrencyCode = currencyCode)
            prefsFlow.value = currentPreferences
        }

        override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
            currentPreferences = currentPreferences.copy(hapticFeedbackEnabled = enabled)
            prefsFlow.value = currentPreferences
        }

        override suspend fun setShowCompletedItems(show: Boolean) {
            currentPreferences = currentPreferences.copy(showCompletedItems = show)
            prefsFlow.value = currentPreferences
        }
    }
}

