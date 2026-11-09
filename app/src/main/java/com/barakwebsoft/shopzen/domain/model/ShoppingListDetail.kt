package com.barakwebsoft.shopzen.domain.model

import com.barakwebsoft.shopzen.core.util.BudgetMetrics
import com.barakwebsoft.shopzen.core.util.CategorySpendDistribution
import com.barakwebsoft.shopzen.core.util.MoneyCalculator
import com.barakwebsoft.shopzen.core.util.SpendCulprit

data class ShoppingListDetail(
    val list: ShoppingList,
    val items: List<ShoppingItemDetail> = emptyList()
) {
    val totalItemsCount: Int get() = items.size
    val purchasedItemsCount: Int get() = items.count { it.item.isPurchased }
    val pendingItemsCount: Int get() = items.count { !it.item.isPurchased }

    private val rawItems: List<ShoppingItem> get() = items.map { it.item }

    val estimatedTotalMinor: Long
        get() = MoneyCalculator.calculateEstimatedTotal(rawItems)

    val actualSpentMinor: Long
        get() = MoneyCalculator.calculateActualSpent(rawItems)

    val estimatedPendingMinor: Long
        get() = MoneyCalculator.calculatePendingEstimated(rawItems)

    val budgetMetrics: BudgetMetrics
        get() = MoneyCalculator.calculateBudgetMetrics(list.budgetMinor, rawItems)

    val remainingBudgetMinor: Long?
        get() = budgetMetrics.remainingBudgetMinor

    val isOverBudget: Boolean
        get() = budgetMetrics.isOverBudget

    val budgetProgress: Float?
        get() = budgetMetrics.budgetProgress

    val isSpendEstimated: Boolean
        get() = budgetMetrics.isSpendEstimated

    val countPurchasedLackingActualPrice: Int
        get() = budgetMetrics.countPurchasedLackingActualPrice

    val categorySpendDistribution: List<CategorySpendDistribution>
        get() = MoneyCalculator.calculateCategorySpendDistribution(items)

    val topSpendCulprits: List<SpendCulprit>
        get() = MoneyCalculator.calculateTopSpendCulprits(items, list.budgetMinor)
}

