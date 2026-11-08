package com.barakwebsoft.shopzen.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.barakwebsoft.shopzen.data.local.entity.CategoryEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingItemEntity

data class ShoppingItemWithCategory(
    @Embedded
    val item: ShoppingItemEntity,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
