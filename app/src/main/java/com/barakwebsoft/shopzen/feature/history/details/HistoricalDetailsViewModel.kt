package com.barakwebsoft.shopzen.feature.history.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class HistoricalDetailsViewModel(
    private val historyId: String,
    private val historyRepository: HistoryRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingItemRepository: ShoppingItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricalDetailsUiState())
    val uiState: StateFlow<HistoricalDetailsUiState> = _uiState.asStateFlow()

    init {
        loadHistoryDetail()
    }

    private fun loadHistoryDetail() {
        viewModelScope.launch {
            historyRepository.observeHistoryDetail(historyId).collect { detail ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        historyDetail = detail
                    )
                }
            }
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteHistory() {
        viewModelScope.launch {
            historyRepository.deleteHistory(historyId)
            _uiState.update { it.copy(isDeleted = true, showDeleteDialog = false) }
        }
    }

    fun duplicateAsNewList(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val detail = _uiState.value.historyDetail ?: return@launch
            val history = detail.history
            val newListId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val newList = ShoppingList(
                id = newListId,
                name = "${history.listName} (Copy)",
                currencyCode = history.currencyCode,
                budgetMinor = history.budgetMinor,
                status = ListStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )
            shoppingListRepository.createList(newList)

            val newItems = detail.items.mapIndexed { index, hItem ->
                ShoppingItem(
                    id = UUID.randomUUID().toString(),
                    listId = newListId,
                    name = hItem.itemName,
                    quantity = hItem.quantity,
                    unit = hItem.unit,
                    categoryId = null,
                    estimatedUnitPriceMinor = hItem.actualUnitPriceMinor,
                    actualUnitPriceMinor = null,
                    isPurchased = false,
                    sortOrder = index,
                    createdAt = now + index,
                    updatedAt = now + index
                )
            }
            shoppingItemRepository.addItems(newItems)

            _uiState.update { it.copy(clonedListId = newListId) }
            onComplete(newListId)
        }
    }
}

