package com.barakwebsoft.shopzen.data.mapper

import com.barakwebsoft.shopzen.data.local.entity.ShoppingItemEntity
import com.barakwebsoft.shopzen.data.local.relation.ShoppingItemWithCategory
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail

fun ShoppingItemEntity.toDomain(): ShoppingItem = ShoppingItem(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    unit = unit,
    categoryId = categoryId,
    estimatedUnitPriceMinor = estimatedUnitPriceMinor,
    actualUnitPriceMinor = actualUnitPriceMinor,
    isPurchased = isPurchased,
    notes = notes,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ShoppingItem.toEntity(): ShoppingItemEntity = ShoppingItemEntity(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    unit = unit,
    categoryId = categoryId,
    estimatedUnitPriceMinor = estimatedUnitPriceMinor,
    actualUnitPriceMinor = actualUnitPriceMinor,
    isPurchased = isPurchased,
    notes = notes,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ShoppingItemWithCategory.toDomainDetail(): ShoppingItemDetail = ShoppingItemDetail(
    item = item.toDomain(),
    category = category?.toDomain()
)
