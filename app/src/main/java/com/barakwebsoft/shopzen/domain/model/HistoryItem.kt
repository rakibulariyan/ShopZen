package com.barakwebsoft.shopzen.domain.model

import java.util.UUID

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val historyId: String,
    val originalItemId: String? = null,
    val itemName: String,
    val quantity: String,
    val unit: String,
    val categoryName: String? = null,
    val actualUnitPriceMinor: Long? = null,
    val lineTotalMinor: Long? = null,
    val isPurchased: Boolean = true
)
