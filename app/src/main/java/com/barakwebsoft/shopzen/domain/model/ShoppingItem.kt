package com.barakwebsoft.shopzen.domain.model

import java.util.UUID

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val listId: String,
    val name: String,
    val quantity: String = "1",
    val unit: String = "pcs",
    val categoryId: String? = null,
    val estimatedUnitPriceMinor: Long? = null,
    val actualUnitPriceMinor: Long? = null,
    val isPurchased: Boolean = false,
    val notes: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
