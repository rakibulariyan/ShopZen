package com.barakwebsoft.shopzen.domain.model

data class ShoppingListDetail(
    val list: ShoppingList,
    val items: List<ShoppingItemDetail> = emptyList()
) {
    val totalItemsCount: Int get() = items.size
    val purchasedItemsCount: Int get() = items.count { it.item.isPurchased }
    val pendingItemsCount: Int get() = items.count { !it.item.isPurchased }
}
