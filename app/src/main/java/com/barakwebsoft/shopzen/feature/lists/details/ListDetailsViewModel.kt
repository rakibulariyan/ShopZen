package com.barakwebsoft.shopzen.feature.lists.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing shopping list details observation, item purchase toggles,
 * category filtering, sorting, reordering, and list lifecycle actions.
 */
class ListDetailsViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingItemRepository: ShoppingItemRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListDetailsUiState())
    val uiState: StateFlow<ListDetailsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var currentListId: String? = null

    fun loadList(listId: String) {
        if (currentListId == listId && observeJob != null) return
        currentListId = listId

        observeJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }

        observeJob = viewModelScope.launch {
            shoppingListRepository.observeListDetail(listId).collect { detail ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        listDetail = detail
                    )
                }
            }
        }
    }

    fun setCategoryFilter(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun setPurchaseFilter(filter: ItemPurchaseFilter) {
        _uiState.update { it.copy(purchaseFilter = filter) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearchQuery() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun toggleSearchExpanded(expanded: Boolean? = null) {
        _uiState.update {
            val newExpanded = expanded ?: !it.isSearchExpanded
            it.copy(
                isSearchExpanded = newExpanded,
                searchQuery = if (!newExpanded) "" else it.searchQuery
            )
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedCategoryId = null,
                purchaseFilter = ItemPurchaseFilter.ALL,
                isSearchExpanded = false
            )
        }
    }

    fun setSortOrder(sortOrder: ItemSortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
    }

    fun toggleItemPurchased(itemId: String, currentPurchased: Boolean) {
        viewModelScope.launch {
            shoppingItemRepository.toggleItemPurchased(itemId, !currentPurchased)
        }
    }

    fun updateItemSortOrder(itemId: String, newSortOrder: Int) {
        viewModelScope.launch {
            shoppingItemRepository.updateItemSortOrder(itemId, newSortOrder)
        }
    }

    fun archiveList(keepDuplicate: Boolean) {
        val listId = currentListId ?: return
        viewModelScope.launch {
            if (keepDuplicate) {
                shoppingListRepository.duplicateList(listId)
            }
            historyRepository.archiveListToHistory(listId)
            _uiState.update { it.copy(isListArchived = true) }
        }
    }

    fun deleteList() {
        val listId = currentListId ?: return
        viewModelScope.launch {
            shoppingListRepository.deleteList(listId)
            _uiState.update { it.copy(isListDeleted = true) }
        }
    }

    fun duplicateList(newName: String) {
        val listId = currentListId ?: return
        viewModelScope.launch {
            val newListId = shoppingListRepository.duplicateList(listId, newName)
            _uiState.update { it.copy(isListDuplicated = true, duplicatedListId = newListId) }
        }
    }
}

