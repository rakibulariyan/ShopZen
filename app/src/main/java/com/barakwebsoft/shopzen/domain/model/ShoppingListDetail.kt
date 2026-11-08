package com.barakwebsoft.shopzen.domain.model

import java.math.BigDecimal

data class ShoppingListDetail(
    val list: ShoppingList,
    val items: List<ShoppingItemDetail> = emptyList()
) {
    val totalItemsCount: Int get() = items.size
    val purchasedItemsCount: Int get() = items.count { it.item.isPurchased }
    val pendingItemsCount: Int get() = items.count { !it.item.isPurchased }

    val estimatedTotalMinor: Long
        get() = items.fold(0L) { acc, detail ->
            val price = detail.item.estimatedUnitPriceMinor ?: 0L
            val qty = detail.item.quantity.toBigDecimalOrNull() ?: BigDecimal.ONE
            acc + (BigDecimal.valueOf(price) * qty).toLong()
        }

    val actualSpentMinor: Long
        get() = items.filter { it.item.isPurchased }.fold(0L) { acc, detail ->
            val price = detail.item.actualUnitPriceMinor ?: detail.item.estimatedUnitPriceMinor ?: 0L
            val qty = detail.item.quantity.toBigDecimalOrNull() ?: BigDecimal.ONE
            acc + (BigDecimal.valueOf(price) * qty).toLong()
        }

    val estimatedPendingMinor: Long
        get() = items.filter { !it.item.isPurchased }.fold(0L) { acc, detail ->
            val price = detail.item.estimatedUnitPriceMinor ?: 0L
            val qty = detail.item.quantity.toBigDecimalOrNull() ?: BigDecimal.ONE
            acc + (BigDecimal.valueOf(price) * qty).toLong()
        }
}

