package com.barakwebsoft.shopzen.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Immutable shopping trip snapshot entity.
 * Note: originalListId is kept for audit/reference, but has no cascading foreign key,
 * ensuring history remains preserved even if the source shopping list is deleted.
 */
@Entity(
    tableName = "shopping_history",
    indices = [
        Index(value = ["completed_at"]),
        Index(value = ["original_list_id"])
    ]
)
data class ShoppingHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "original_list_id")
    val originalListId: String? = null,

    @ColumnInfo(name = "list_name")
    val listName: String,

    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "budget_minor")
    val budgetMinor: Long? = null,

    @ColumnInfo(name = "total_spent_minor")
    val totalSpentMinor: Long,

    @ColumnInfo(name = "purchased_count")
    val purchasedCount: Int,

    @ColumnInfo(name = "unpriced_purchased_count")
    val unpricedPurchasedCount: Int,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long
)
