package com.barakwebsoft.shopzen.feature.market

import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.feature.lists.details.PurchaseToggleAction

/**
 * UI state for the distraction-free in-store Market Mode screen.
 */
data class MarketModeUiState(
    val isLoading: Boolean = true,
    val listDetail: ShoppingListDetail? = null,
    val isHapticEnabled: Boolean = true,
    val isKeepScreenOn: Boolean = true,
    val selectedCategoryId: String? = null,
    val lastPurchaseToggleAction: PurchaseToggleAction? = null,
    val isFinished: Boolean = false
) {
    val totalItemsCount: Int get() = listDetail?.totalItemsCount ?: 0
    val purchasedItemsCount: Int get() = listDetail?.purchasedItemsCount ?: 0
    val pendingItemsCount: Int get() = listDetail?.pendingItemsCount ?: 0

    val progressPercent: Int
        get() = if (totalItemsCount > 0) {
            (purchasedItemsCount * 100) / totalItemsCount
        } else 0

    val unpricedPurchasedItemsCount: Int
        get() = listDetail?.items?.count { it.item.isPurchased && it.item.actualUnitPriceMinor == null } ?: 0

    /**
     * Items filtered by category and sorted with pending items first,
     * maintaining category and list sort order.
     */
    val filteredItems: List<ShoppingItemDetail>
        get() {
            val items = listDetail?.items ?: return emptyList()
            val catFiltered = if (selectedCategoryId != null) {
                items.filter { it.category?.id == selectedCategoryId }
            } else {
                items
            }
            return catFiltered.sortedWith(
                compareBy<ShoppingItemDetail> { it.item.isPurchased }
                    .thenBy { it.category?.sortOrder ?: Int.MAX_VALUE }
                    .thenBy { it.item.sortOrder }
            )
        }

    val groupedByCategory: Map<Category?, List<ShoppingItemDetail>>
        get() = filteredItems.groupBy { it.category }

    val availableCategories: List<Pair<Category?, Int>>
        get() {
            val allItems = listDetail?.items ?: return emptyList()
            return allItems.groupBy { it.category }
                .map { (cat, items) -> cat to items.size }
                .sortedBy { it.first?.sortOrder ?: Int.MAX_VALUE }
        }
}

