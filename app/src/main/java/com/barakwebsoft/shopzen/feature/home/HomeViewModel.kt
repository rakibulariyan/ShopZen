package com.barakwebsoft.shopzen.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing active shopping lists observation, search query filtering,
 * archiving, and deletion of shopping lists on the Home screen.
 */
class HomeViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val historyRepository: HistoryRepository,
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

    fun archiveList(listId: String, keepDuplicateCopy: Boolean) {
        viewModelScope.launch {
            if (keepDuplicateCopy) {
                shoppingListRepository.duplicateList(listId)
            }
            historyRepository.archiveListToHistory(listId)
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            shoppingListRepository.deleteList(listId)
        }
    }

    fun duplicateList(listId: String, newName: String? = null) {
        viewModelScope.launch {
            shoppingListRepository.duplicateList(listId, newName)
        }
    }
}


