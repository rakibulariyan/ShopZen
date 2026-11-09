package com.barakwebsoft.shopzen.feature.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import com.barakwebsoft.shopzen.feature.lists.details.PurchaseToggleAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * One-time UI events emitted by [MarketModeViewModel].
 */
sealed interface MarketModeEvent {
    data class ItemPurchaseToggled(
        val action: PurchaseToggleAction,
        val message: String
    ) : MarketModeEvent
}

/**
 * ViewModel managing the in-store Market Mode experience:
 * item purchase toggling with large targets, haptics, keep-awake, and instant undo.
 */
class MarketModeViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingItemRepository: ShoppingItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketModeUiState())
    val uiState: StateFlow<MarketModeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MarketModeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<MarketModeEvent> = _events.asSharedFlow()

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

    fun toggleHaptic() {
        _uiState.update { it.copy(isHapticEnabled = !it.isHapticEnabled) }
    }

    fun toggleKeepScreenOn() {
        _uiState.update { it.copy(isKeepScreenOn = !it.isKeepScreenOn) }
    }

    fun setCategoryFilter(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
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
            "Purchased $itemName"
        } else {
            "Marked $itemName as pending"
        }
        _events.tryEmit(MarketModeEvent.ItemPurchaseToggled(action, message))

        viewModelScope.launch {
            shoppingItemRepository.toggleItemPurchased(itemId, newPurchased)
        }
    }

    fun undoLastToggle() {
        val lastAction = _uiState.value.lastPurchaseToggleAction ?: return
        viewModelScope.launch {
            shoppingItemRepository.toggleItemPurchased(lastAction.itemId, lastAction.wasPurchased)
            _uiState.update { it.copy(lastPurchaseToggleAction = null) }
        }
    }

    fun undoPurchaseToggle(action: PurchaseToggleAction) {
        viewModelScope.launch {
            shoppingItemRepository.toggleItemPurchased(action.itemId, action.wasPurchased)
            _uiState.update {
                if (it.lastPurchaseToggleAction?.itemId == action.itemId) {
                    it.copy(lastPurchaseToggleAction = null)
                } else {
                    it
                }
            }
        }
    }

    fun finishShopping() {
        _uiState.update { it.copy(isFinished = true) }
    }
}

