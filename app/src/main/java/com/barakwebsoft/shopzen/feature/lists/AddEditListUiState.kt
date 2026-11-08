package com.barakwebsoft.shopzen.feature.lists

/**
 * UI State for Create and Edit Shopping List screen.
 */
data class AddEditListUiState(
    val listId: String? = null,
    val name: String = "",
    val description: String = "",
    val currencyCode: String = "USD",
    val budgetString: String = "",
    val nameError: String? = null,
    val budgetError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedListId: String? = null,
    val isCurrencyPickerOpen: Boolean = false
) {
    val isEditing: Boolean get() = listId != null
}

