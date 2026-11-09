package com.barakwebsoft.shopzen.feature.lists.details

import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail

/**
 * Available sort orders for items in a shopping list.
 */
enum class ItemSortOrder(val displayName: String) {
    CATEGORY("Category"),
    MANUAL("Manual (Custom)"),
    NAME_ASC("Name (A to Z)"),
    PRICE_DESC("Price (High to Low)"),
    PRICE_ASC("Price (Low to High)"),
    PENDING_FIRST("Pending First")
}

/**
 * Filter mode for shopping item purchase status.
 */
enum class ItemPurchaseFilter(val displayName: String) {
    ALL("All"),
    PENDING("Pending"),
    PURCHASED("Purchased")
}

/**
 * Represents an undoable purchase state change action.
 */
data class PurchaseToggleAction(
    val itemId: String,
    val itemName: String,
    val wasPurchased: Boolean,
    val newPurchased: Boolean
)

/**
 * UI State for the List Details screen.
 */
data class ListDetailsUiState(
    val isLoading: Boolean = true,
    val listDetail: ShoppingListDetail? = null,
    val searchQuery: String = "",
    val isSearchExpanded: Boolean = false,
    val purchaseFilter: ItemPurchaseFilter = ItemPurchaseFilter.ALL,
    val selectedCategoryId: String? = null,
    val sortOrder: ItemSortOrder = ItemSortOrder.CATEGORY,
    val lastPurchaseToggleAction: PurchaseToggleAction? = null,
    val isListDeleted: Boolean = false,
    val isListArchived: Boolean = false,
    val isListDuplicated: Boolean = false,
    val duplicatedListId: String? = null
) {
    val totalItemsCount: Int get() = listDetail?.totalItemsCount ?: 0
    val purchasedItemsCount: Int get() = listDetail?.purchasedItemsCount ?: 0
    val pendingItemsCount: Int get() = listDetail?.pendingItemsCount ?: 0

    val isFilterActive: Boolean
        get() = searchQuery.isNotBlank() || selectedCategoryId != null || purchaseFilter != ItemPurchaseFilter.ALL

    val isFilterEmpty: Boolean
        get() = isFilterActive && filteredAndSortedItems.isEmpty() && totalItemsCount > 0

    /**
     * Unique categories present in this list with their item counts.
     */
    val availableCategories: List<Pair<Category?, Int>>
        get() {
            val allItems = listDetail?.items ?: return emptyList()
            val grouped = allItems.groupBy { it.category }
            return grouped.map { (cat, items) -> cat to items.size }
                .sortedBy { it.first?.sortOrder ?: Int.MAX_VALUE }
        }

    /**
     * Items filtered by category, purchase status, and search query,
     * ordered according to [sortOrder].
     */
    val filteredAndSortedItems: List<ShoppingItemDetail>
        get() {
            val allItems = listDetail?.items ?: return emptyList()

            // 1. Category filter
            val categoryFiltered = if (selectedCategoryId == null) {
                allItems
            } else {
                allItems.filter { it.category?.id == selectedCategoryId }
            }

            // 2. Purchase status filter
            val statusFiltered = when (purchaseFilter) {
                ItemPurchaseFilter.ALL -> categoryFiltered
                ItemPurchaseFilter.PENDING -> categoryFiltered.filter { !it.item.isPurchased }
                ItemPurchaseFilter.PURCHASED -> categoryFiltered.filter { it.item.isPurchased }
            }

            // 3. Search query filter
            val trimmedQuery = searchQuery.trim()
            val queryFiltered = if (trimmedQuery.isBlank()) {
                statusFiltered
            } else {
                statusFiltered.filter { detail ->
                    detail.item.name.contains(trimmedQuery, ignoreCase = true) ||
                            (detail.item.notes?.contains(trimmedQuery, ignoreCase = true) == true)
                }
            }

            // 4. Sort order
            return when (sortOrder) {
                ItemSortOrder.CATEGORY -> queryFiltered.sortedWith(
                    compareBy<ShoppingItemDetail> { it.category?.sortOrder ?: Int.MAX_VALUE }
                        .thenBy { it.category?.name ?: "Z" }
                        .thenBy { it.item.sortOrder }
                )
                ItemSortOrder.MANUAL -> queryFiltered.sortedBy { it.item.sortOrder }
                ItemSortOrder.NAME_ASC -> queryFiltered.sortedBy { it.item.name.lowercase() }
                ItemSortOrder.PRICE_DESC -> queryFiltered.sortedByDescending {
                    it.item.actualUnitPriceMinor ?: it.item.estimatedUnitPriceMinor ?: 0L
                }
                ItemSortOrder.PRICE_ASC -> queryFiltered.sortedBy {
                    it.item.actualUnitPriceMinor ?: it.item.estimatedUnitPriceMinor ?: 0L
                }
                ItemSortOrder.PENDING_FIRST -> queryFiltered.sortedWith(
                    compareBy<ShoppingItemDetail> { it.item.isPurchased }
                        .thenBy { it.item.sortOrder }
                )
            }
        }

    /**
     * Grouping of [filteredAndSortedItems] by Category when [sortOrder] is [ItemSortOrder.CATEGORY].
     */
    val groupedByCategory: Map<Category?, List<ShoppingItemDetail>>
        get() = filteredAndSortedItems.groupBy { it.category }
}

