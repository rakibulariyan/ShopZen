package com.barakwebsoft.shopzen.feature.lists.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * One-time UI events emitted by [ListDetailsViewModel].
 */
sealed interface ListDetailsEvent {
    data class ItemPurchaseToggled(
        val action: PurchaseToggleAction,
        val message: String
    ) : ListDetailsEvent
}

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

    private val _events = MutableSharedFlow<ListDetailsEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ListDetailsEvent> = _events.asSharedFlow()

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
        val item = _uiState.value.listDetail?.items?.find { it.item.id == itemId }?.item
        val itemName = item?.name ?: "Item"
        val newPurchased = !currentPurchased

        val action = PurchaseToggleAction(
            itemId = itemId,
            itemName = itemName,
            wasPurchased = currentPurchased,
            newPurchased = newPurchased
        )
        _uiState.update { it.copy(lastPurchaseToggleAction = action) }

        val message = if (newPurchased) {
            "Marked \"$itemName\" as purchased"
        } else {
            "Marked \"$itemName\" as pending"
        }
        _events.tryEmit(ListDetailsEvent.ItemPurchaseToggled(action, message))

        viewModelScope.launch {
            shoppingItemRepository.toggleItemPurchased(itemId, newPurchased)
        }
    }

    /**
     * Undoes the specified or last performed purchase toggle action, restoring
     * the item to its previous purchased status in the repository.
     */
    fun undoPurchaseToggle(action: PurchaseToggleAction? = null) {
        val targetAction = action ?: _uiState.value.lastPurchaseToggleAction ?: return
        viewModelScope.launch {
            shoppingItemRepository.toggleItemPurchased(targetAction.itemId, targetAction.wasPurchased)
            _uiState.update {
                if (it.lastPurchaseToggleAction?.itemId == targetAction.itemId) {
                    it.copy(lastPurchaseToggleAction = null)
                } else {
                    it
                }
            }
        }
    }

    /**
     * Reverts an item's purchased status directly to [previousPurchased].
     */
    fun undoToggle(itemId: String, previousPurchased: Boolean) {
        viewModelScope.launch {
            shoppingItemRepository.toggleItemPurchased(itemId, previousPurchased)
            _uiState.update {
                if (it.lastPurchaseToggleAction?.itemId == itemId) {
                    it.copy(lastPurchaseToggleAction = null)
                } else {
                    it
                }
            }
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

