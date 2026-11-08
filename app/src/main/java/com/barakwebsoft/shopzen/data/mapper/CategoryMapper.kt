package com.barakwebsoft.shopzen.data.mapper

import com.barakwebsoft.shopzen.data.local.entity.CategoryEntity
import com.barakwebsoft.shopzen.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    iconName = iconName,
    colorToken = colorToken,
    isDefault = isDefault,
    sortOrder = sortOrder
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconName = iconName,
    colorToken = colorToken,
    isDefault = isDefault,
    sortOrder = sortOrder
)
