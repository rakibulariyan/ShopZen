package com.barakwebsoft.shopzen.feature.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.core.util.CurrencyFormatter
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import com.barakwebsoft.shopzen.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AddEditListViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditListUiState())
    val uiState: StateFlow<AddEditListUiState> = _uiState.asStateFlow()

    private var originalList: ShoppingList? = null

    fun loadList(listId: String?) {
        if (listId == null) {
            // Create mode: load default currency from preferences
            viewModelScope.launch {
                val prefs = userPreferencesRepository.getPreferences().first()
                _uiState.update { it.copy(currencyCode = prefs.defaultCurrencyCode) }
            }
            return
        }

        // Edit mode: load list details
        _uiState.update { it.copy(isLoading = true, listId = listId) }
        viewModelScope.launch {
            val list = shoppingListRepository.getListById(listId)
            if (list != null) {
                originalList = list
                val budgetMajorStr = CurrencyFormatter.toMajorUnitsString(list.budgetMinor, list.currencyCode)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        listId = list.id,
                        name = list.name,
                        description = list.description ?: "",
                        currencyCode = list.currencyCode,
                        budgetString = budgetMajorStr
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = if (name.isNotBlank()) null else it.nameError
            )
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onCurrencyChange(currencyCode: String) {
        _uiState.update { it.copy(currencyCode = currencyCode) }
    }

    fun onBudgetChange(budgetString: String) {
        _uiState.update {
            it.copy(
                budgetString = budgetString,
                budgetError = null
            )
        }
    }

    fun setCurrencyPickerVisible(visible: Boolean) {
        _uiState.update { it.copy(isCurrencyPickerOpen = visible) }
    }

    fun saveList() {
        val currentState = _uiState.value

        val trimmedName = currentState.name.trim()
        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(nameError = "List name is required") }
            return
        }

        val trimmedBudget = currentState.budgetString.trim()
        val budgetMinor: Long? = if (trimmedBudget.isNotEmpty()) {
            val parsed = CurrencyFormatter.parseToMinor(trimmedBudget, currentState.currencyCode)
            if (parsed == null) {
                _uiState.update { it.copy(budgetError = "Enter a valid positive number") }
                return
            }
            parsed
        } else {
            null
        }

        _uiState.update { it.copy(isSaving = true, nameError = null, budgetError = null) }

        viewModelScope.launch {
            if (currentState.isEditing) {
                val listId = currentState.listId ?: return@launch
                val existing = originalList ?: shoppingListRepository.getListById(listId)
                if (existing != null) {
                    val updated = existing.copy(
                        name = trimmedName,
                        description = currentState.description.trim().ifBlank { null },
                        currencyCode = currentState.currencyCode,
                        budgetMinor = budgetMinor,
                        updatedAt = System.currentTimeMillis()
                    )
                    shoppingListRepository.updateList(updated)
                    _uiState.update { it.copy(isSaving = false, isSaved = true, savedListId = listId) }
                } else {
                    _uiState.update { it.copy(isSaving = false) }
                }
            } else {
                val newId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                val newList = ShoppingList(
                    id = newId,
                    name = trimmedName,
                    description = currentState.description.trim().ifBlank { null },
                    currencyCode = currentState.currencyCode,
                    budgetMinor = budgetMinor,
                    createdAt = now,
                    updatedAt = now
                )
                shoppingListRepository.createList(newList)
                _uiState.update { it.copy(isSaving = false, isSaved = true, savedListId = newId) }
            }
        }
    }
}

