package com.barakwebsoft.shopzen.data.mapper

import com.barakwebsoft.shopzen.data.local.entity.HistoryItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingHistoryEntity
import com.barakwebsoft.shopzen.data.local.relation.HistoryWithItems
import com.barakwebsoft.shopzen.domain.model.HistoryDetail
import com.barakwebsoft.shopzen.domain.model.HistoryItem
import com.barakwebsoft.shopzen.domain.model.ShoppingHistory

fun ShoppingHistoryEntity.toDomain(): ShoppingHistory = ShoppingHistory(
    id = id,
    originalListId = originalListId,
    listName = listName,
    currencyCode = currencyCode,
    budgetMinor = budgetMinor,
    totalSpentMinor = totalSpentMinor,
    purchasedCount = purchasedCount,
    unpricedPurchasedCount = unpricedPurchasedCount,
    completedAt = completedAt
)

fun ShoppingHistory.toEntity(): ShoppingHistoryEntity = ShoppingHistoryEntity(
    id = id,
    originalListId = originalListId,
    listName = listName,
    currencyCode = currencyCode,
    budgetMinor = budgetMinor,
    totalSpentMinor = totalSpentMinor,
    purchasedCount = purchasedCount,
    unpricedPurchasedCount = unpricedPurchasedCount,
    completedAt = completedAt
)

fun HistoryItemEntity.toDomain(): HistoryItem = HistoryItem(
    id = id,
    historyId = historyId,
    originalItemId = originalItemId,
    itemName = itemName,
    quantity = quantity,
    unit = unit,
    categoryName = categoryName,
    actualUnitPriceMinor = actualUnitPriceMinor,
    lineTotalMinor = lineTotalMinor,
    isPurchased = isPurchased
)

fun HistoryItem.toEntity(): HistoryItemEntity = HistoryItemEntity(
    id = id,
    historyId = historyId,
    originalItemId = originalItemId,
    itemName = itemName,
    quantity = quantity,
    unit = unit,
    categoryName = categoryName,
    actualUnitPriceMinor = actualUnitPriceMinor,
    lineTotalMinor = lineTotalMinor,
    isPurchased = isPurchased
)

fun HistoryWithItems.toDomainDetail(): HistoryDetail = HistoryDetail(
    history = history.toDomain(),
    items = items.map { it.toDomain() }
)
