package com.barakwebsoft.shopzen.domain.model

import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import java.util.UUID

data class ShoppingList(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val currencyCode: String = "USD",
    val budgetMinor: Long? = null,
    val status: ListStatus = ListStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
