package com.barakwebsoft.shopzen.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel managing active shopping lists observation and search query filtering on the Home screen.
 */
class HomeViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    started: SharingStarted = SharingStarted.WhileSubscribed(5_000)
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        shoppingListRepository.getActiveListDetails(),
        _searchQuery
    ) { lists: List<ShoppingListDetail>, query: String ->
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            lists
        } else {
            lists.filter { it.list.name.contains(trimmed, ignoreCase = true) }
        }

        HomeUiState(
            isLoading = false,
            lists = lists,
            searchQuery = query,
            filteredLists = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = started,
        initialValue = HomeUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }
}

