package com.barakwebsoft.shopzen.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["history_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["history_id"])
    ]
)
data class HistoryItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "history_id")
    val historyId: String,

    @ColumnInfo(name = "original_item_id")
    val originalItemId: String? = null,

    @ColumnInfo(name = "item_name")
    val itemName: String,

    @ColumnInfo(name = "quantity")
    val quantity: String,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "category_name")
    val categoryName: String? = null,

    @ColumnInfo(name = "actual_unit_price_minor")
    val actualUnitPriceMinor: Long? = null,

    @ColumnInfo(name = "line_total_minor")
    val lineTotalMinor: Long? = null,

    @ColumnInfo(name = "is_purchased")
    val isPurchased: Boolean
)
