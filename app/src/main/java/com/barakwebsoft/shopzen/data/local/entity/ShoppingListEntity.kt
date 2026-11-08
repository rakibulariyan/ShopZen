package com.barakwebsoft.shopzen.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lifecycle statuses for a shopping list.
 */
object ShoppingListStatus {
    const val ACTIVE = "ACTIVE"
    const val COMPLETED = "COMPLETED"
    const val ARCHIVED = "ARCHIVED"
}

@Entity(
    tableName = "shopping_lists",
    indices = [
        Index(value = ["status"]),
        Index(value = ["updated_at"])
    ]
)
data class ShoppingListEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "budget_minor")
    val budgetMinor: Long? = null,

    @ColumnInfo(name = "status")
    val status: String = ShoppingListStatus.ACTIVE,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null
)
