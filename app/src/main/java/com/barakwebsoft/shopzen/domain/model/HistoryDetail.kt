package com.barakwebsoft.shopzen.domain.model

data class HistoryDetail(
    val history: ShoppingHistory,
    val items: List<HistoryItem> = emptyList()
) {
    val totalItemsCount: Int get() = items.size
    val purchasedItemsCount: Int get() = items.count { it.isPurchased }
}
