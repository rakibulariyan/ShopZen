package com.barakwebsoft.shopzen.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.barakwebsoft.shopzen.data.local.entity.ShoppingItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingListEntity

data class ShoppingListWithItems(
    @Embedded
    val list: ShoppingListEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "list_id"
    )
    val items: List<ShoppingItemEntity>
)
