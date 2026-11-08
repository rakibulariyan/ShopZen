package com.barakwebsoft.shopzen.domain.model

import java.util.UUID

data class ShoppingHistory(
    val id: String = UUID.randomUUID().toString(),
    val originalListId: String? = null,
    val listName: String,
    val currencyCode: String,
    val budgetMinor: Long? = null,
    val totalSpentMinor: Long,
    val purchasedCount: Int,
    val unpricedPurchasedCount: Int,
    val completedAt: Long = System.currentTimeMillis()
)
