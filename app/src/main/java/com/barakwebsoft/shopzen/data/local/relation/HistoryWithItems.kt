package com.barakwebsoft.shopzen.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.barakwebsoft.shopzen.data.local.entity.HistoryItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingHistoryEntity

data class HistoryWithItems(
    @Embedded
    val history: ShoppingHistoryEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "history_id"
    )
    val items: List<HistoryItemEntity>
)
