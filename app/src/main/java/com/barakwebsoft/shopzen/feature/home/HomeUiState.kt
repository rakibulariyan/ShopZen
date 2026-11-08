package com.barakwebsoft.shopzen.feature.home

import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail

/**
 * UI State for the Home screen.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val lists: List<ShoppingListDetail> = emptyList(),
    val searchQuery: String = "",
    val filteredLists: List<ShoppingListDetail> = emptyList()
) {
    /**
     * Whether the user has no lists created at all (true zero-state).
     */
    val isTrueEmpty: Boolean
        get() = !isLoading && lists.isEmpty()

    /**
     * Total count of active shopping lists.
     */
    val totalActiveLists: Int
        get() = lists.size
}

