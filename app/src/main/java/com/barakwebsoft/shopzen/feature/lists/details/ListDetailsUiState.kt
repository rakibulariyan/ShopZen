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
 * UI State for the List Details screen.
 */
data class ListDetailsUiState(
    val isLoading: Boolean = true,
    val listDetail: ShoppingListDetail? = null,
    val selectedCategoryId: String? = null,
    val sortOrder: ItemSortOrder = ItemSortOrder.CATEGORY,
    val isListDeleted: Boolean = false,
    val isListArchived: Boolean = false,
    val isListDuplicated: Boolean = false,
    val duplicatedListId: String? = null
) {
    val totalItemsCount: Int get() = listDetail?.totalItemsCount ?: 0
    val purchasedItemsCount: Int get() = listDetail?.purchasedItemsCount ?: 0
    val pendingItemsCount: Int get() = listDetail?.pendingItemsCount ?: 0

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
     * Items filtered by category and ordered according to [sortOrder].
     */
    val filteredAndSortedItems: List<ShoppingItemDetail>
        get() {
            val allItems = listDetail?.items ?: return emptyList()
            val categoryFiltered = if (selectedCategoryId == null) {
                allItems
            } else {
                allItems.filter { it.category?.id == selectedCategoryId }
            }

            return when (sortOrder) {
                ItemSortOrder.CATEGORY -> categoryFiltered.sortedWith(
                    compareBy<ShoppingItemDetail> { it.category?.sortOrder ?: Int.MAX_VALUE }
                        .thenBy { it.category?.name ?: "Z" }
                        .thenBy { it.item.sortOrder }
                )
                ItemSortOrder.MANUAL -> categoryFiltered.sortedBy { it.item.sortOrder }
                ItemSortOrder.NAME_ASC -> categoryFiltered.sortedBy { it.item.name.lowercase() }
                ItemSortOrder.PRICE_DESC -> categoryFiltered.sortedByDescending {
                    it.item.actualUnitPriceMinor ?: it.item.estimatedUnitPriceMinor ?: 0L
                }
                ItemSortOrder.PRICE_ASC -> categoryFiltered.sortedBy {
                    it.item.actualUnitPriceMinor ?: it.item.estimatedUnitPriceMinor ?: 0L
                }
                ItemSortOrder.PENDING_FIRST -> categoryFiltered.sortedWith(
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

