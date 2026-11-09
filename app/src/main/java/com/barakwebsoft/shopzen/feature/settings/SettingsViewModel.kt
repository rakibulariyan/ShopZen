package com.barakwebsoft.shopzen.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.domain.model.ThemePreference
import com.barakwebsoft.shopzen.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5000)
) : ViewModel() {

    private val _isCurrencyPickerVisible = MutableStateFlow(false)
    private val _isThemeDialogVisible = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.getPreferences(),
        _isCurrencyPickerVisible,
        _isThemeDialogVisible
    ) { prefs, currencyPickerOpen, themeDialogOpen ->
        SettingsUiState(
            isLoading = false,
            themePreference = prefs.themePreference,
            defaultCurrencyCode = prefs.defaultCurrencyCode,
            hapticFeedbackEnabled = prefs.hapticFeedbackEnabled,
            showCompletedItems = prefs.showCompletedItems,
            isCurrencyPickerVisible = currencyPickerOpen,
            isThemeDialogVisible = themeDialogOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = sharingStarted,
        initialValue = SettingsUiState(isLoading = true)
    )

    fun setDefaultCurrency(currencyCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setDefaultCurrency(currencyCode)
            _isCurrencyPickerVisible.value = false
        }
    }

    fun setCurrencyPickerVisible(visible: Boolean) {
        _isCurrencyPickerVisible.value = visible
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            userPreferencesRepository.setThemePreference(theme)
            _isThemeDialogVisible.value = false
        }
    }

    fun setThemeDialogVisible(visible: Boolean) {
        _isThemeDialogVisible.value = visible
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setHapticFeedbackEnabled(enabled)
        }
    }

    fun setShowCompletedItems(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowCompletedItems(show)
        }
    }
}

