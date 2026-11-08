package com.barakwebsoft.shopzen.data.mapper

import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.data.local.entity.ShoppingListEntity
import com.barakwebsoft.shopzen.domain.model.ShoppingList

fun ShoppingListEntity.toDomain(): ShoppingList = ShoppingList(
    id = id,
    name = name,
    description = description,
    currencyCode = currencyCode,
    budgetMinor = budgetMinor,
    status = try {
        ListStatus.valueOf(status)
    } catch (_: Exception) {
        ListStatus.ACTIVE
    },
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt
)

fun ShoppingList.toEntity(): ShoppingListEntity = ShoppingListEntity(
    id = id,
    name = name,
    description = description,
    currencyCode = currencyCode,
    budgetMinor = budgetMinor,
    status = status.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt
)
