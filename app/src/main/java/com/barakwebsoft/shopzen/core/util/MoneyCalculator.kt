package com.barakwebsoft.shopzen.core.util

import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Robust quantity parsing, validation, and stepper adjustment utilities.
 * Uses BigDecimal internally to avoid binary floating-point inaccuracies.
 */
object QuantityCalculator {

    val MIN_QUANTITY: BigDecimal = BigDecimal.ONE

    /**
     * Parses a user-input quantity string into a positive [BigDecimal].
     * Handles both comma and period decimal separators.
     * Returns null if blank, malformed, zero, or negative.
     */
    fun parse(quantityStr: String?): BigDecimal? {
        val clean = quantityStr?.trim()?.replace(",", ".") ?: return null
        if (clean.isBlank()) return null
        return try {
            val bd = BigDecimal(clean)
            if (bd > BigDecimal.ZERO) bd else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Checks whether the given quantity string represents a valid positive number.
     */
    fun isValid(quantityStr: String?): Boolean {
        return parse(quantityStr) != null
    }

    /**
     * Formats a [BigDecimal] quantity into a clean string representation,
     * stripping unnecessary trailing zeroes (e.g. 2.0 -> "2", 1.50 -> "1.5").
     */
    fun format(quantity: BigDecimal): String {
        val stripped = quantity.stripTrailingZeros()
        return if (stripped.scale() <= 0) {
            stripped.toBigInteger().toString()
        } else {
            stripped.toPlainString()
        }
    }

    /**
     * Adjusts a quantity string by a given [delta] step, enforcing [min] lower bound.
     */
    fun adjust(
        currentStr: String?,
        delta: BigDecimal,
        min: BigDecimal = MIN_QUANTITY
    ): String {
        val current = parse(currentStr) ?: min
        val next = (current + delta).coerceAtLeast(min)
        return format(next)
    }

    /**
     * Convenience adjustment by integer delta (+1, -1).
     */
    fun adjust(
        currentStr: String?,
        delta: Int,
        min: BigDecimal = MIN_QUANTITY
    ): String {
        return adjust(currentStr, BigDecimal.valueOf(delta.toLong()), min)
    }
}

/**
 * Data structure representing budget health and financial metrics
 * for a shopping list or market mode session.
 */
data class BudgetMetrics(
    val budgetMinor: Long?,
    val estimatedTotalMinor: Long,
    val actualSpentMinor: Long,
    val pendingEstimatedMinor: Long,
    val remainingBudgetMinor: Long?,
    val overBudgetMinor: Long?,
    val isOverBudget: Boolean,
    val budgetProgress: Float?,
    val totalItemsCount: Int,
    val purchasedCount: Int,
    val pendingCount: Int,
    val unpricedItemsCount: Int,
    val unpricedPurchasedCount: Int,
    val countPurchasedLackingActualPrice: Int = 0
) {
    /**
     * PRD mandate: If purchased items lack actual prices, the UI clearly discloses them
     * and flags the spend total as incomplete or estimated; it must not silently treat unknown spend as zero.
     */
    val isSpendEstimated: Boolean get() = countPurchasedLackingActualPrice > 0
}

/**
 * Department or category spend distribution metrics for high-level budget breakdown.
 */
data class CategorySpendDistribution(
    val categoryId: String?,
    val categoryName: String,
    val totalSpendMinor: Long,
    val percentageOfTotal: Int,
    val colorToken: String? = null
)

/**
 * Highlights high-cost items contributing significantly to budget consumption or overruns.
 */
data class SpendCulprit(
    val itemId: String,
    val itemName: String,
    val lineTotalMinor: Long,
    val percentOfBudget: Int,
    val isPurchased: Boolean
)

/**
 * Core financial calculation engine for ShopZen.
 * Operates purely on integer minor units (Long) and BigDecimal quantity scales.
 * Binary floating-point types (Float/Double) are strictly disallowed for money.
 */
object MoneyCalculator {

    /**
     * Calculates line total for an item: `unitPriceMinor * quantity`.
     * Applies exact HALF_UP rounding to integer minor units.
     * Returns null if [unitPriceMinor] or [quantity] is null/invalid.
     */
    fun calculateLineTotal(unitPriceMinor: Long?, quantity: BigDecimal?): Long? {
        if (unitPriceMinor == null || unitPriceMinor < 0L || quantity == null || quantity <= BigDecimal.ZERO) return null
        return (BigDecimal.valueOf(unitPriceMinor) * quantity)
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()
    }

    /**
     * Convenience line total calculation from string quantity.
     */
    fun calculateLineTotal(unitPriceMinor: Long?, quantityStr: String?): Long? {
        val qty = QuantityCalculator.parse(quantityStr) ?: return null
        return calculateLineTotal(unitPriceMinor, qty)
    }

    /**
     * Sum of estimated line totals for all items in the list.
     * Items without estimated price contribute 0 to the sum.
     */
    fun calculateEstimatedTotal(items: Collection<ShoppingItem>): Long {
        return items.fold(0L) { acc, item ->
            val lineTotal = calculateLineTotal(item.estimatedUnitPriceMinor, item.quantity) ?: 0L
            acc + lineTotal
        }
    }

    /**
     * Sum of actual spent amounts for purchased items.
     * Uses [ShoppingItem.actualUnitPriceMinor] when available;
     * falls back to [ShoppingItem.estimatedUnitPriceMinor] if actual price is not yet entered.
     */
    fun calculateActualSpent(items: Collection<ShoppingItem>): Long {
        return items.filter { it.isPurchased }.fold(0L) { acc, item ->
            val price = item.actualUnitPriceMinor ?: item.estimatedUnitPriceMinor
            val lineTotal = calculateLineTotal(price, item.quantity) ?: 0L
            acc + lineTotal
        }
    }

    /**
     * Sum of estimated line totals for pending (unpurchased) items.
     */
    fun calculatePendingEstimated(items: Collection<ShoppingItem>): Long {
        return items.filter { !it.isPurchased }.fold(0L) { acc, item ->
            val lineTotal = calculateLineTotal(item.estimatedUnitPriceMinor, item.quantity) ?: 0L
            acc + lineTotal
        }
    }

    /**
     * Total number of items in the list with no price specified (neither estimated nor actual).
     */
    fun countUnpricedItems(items: Collection<ShoppingItem>): Int {
        return items.count { it.estimatedUnitPriceMinor == null && it.actualUnitPriceMinor == null }
    }

    /**
     * Total number of purchased items where the user has not entered an actual price
     * (and no estimated price was available).
     * PRD: "Clearly indicate purchased items without actual prices; do not silently treat unknown spend as zero."
     */
    fun countUnpricedPurchasedItems(items: Collection<ShoppingItem>): Int {
        return items.count { it.isPurchased && it.actualUnitPriceMinor == null && it.estimatedUnitPriceMinor == null }
    }

    /**
     * Total number of purchased items that lack an explicitly confirmed actual unit price.
     */
    fun countPurchasedLackingActualPrice(items: Collection<ShoppingItem>): Int {
        return items.count { it.isPurchased && it.actualUnitPriceMinor == null }
    }

    /**
     * Remaining budget in minor units: `budgetMinor - actualSpentMinor`.
     * Returns null if [budgetMinor] is null.
     * If spent exceeds budget, returns negative value (indicates deficit).
     */
    fun calculateRemainingBudget(budgetMinor: Long?, actualSpentMinor: Long): Long? {
        if (budgetMinor == null) return null
        return budgetMinor - actualSpentMinor
    }

    /**
     * The amount by which actual spend exceeds budget in minor units.
     * Returns null if [budgetMinor] is null or if spend does not exceed budget.
     */
    fun calculateOverBudgetAmount(budgetMinor: Long?, actualSpentMinor: Long): Long? {
        if (budgetMinor == null) return null
        val diff = actualSpentMinor - budgetMinor
        return if (diff > 0) diff else null
    }

    /**
     * Whether actual spend exceeds the allocated budget.
     */
    fun isOverBudget(budgetMinor: Long?, actualSpentMinor: Long): Boolean {
        if (budgetMinor == null || budgetMinor <= 0L) return false
        return actualSpentMinor > budgetMinor
    }

    /**
     * Budget utilization ratio as a Float between 0.0f and 1.0f (or > 1.0f when over budget).
     * Returns null if no budget is set.
     */
    fun calculateBudgetProgress(budgetMinor: Long?, actualSpentMinor: Long): Float? {
        if (budgetMinor == null || budgetMinor <= 0L) return null
        return (actualSpentMinor.toFloat() / budgetMinor.toFloat()).coerceAtLeast(0f)
    }

    /**
     * Generates complete [BudgetMetrics] for a shopping list and its items.
     */
    fun calculateBudgetMetrics(
        budgetMinor: Long?,
        items: Collection<ShoppingItem>
    ): BudgetMetrics {
        val totalCount = items.size
        val purchasedCount = items.count { it.isPurchased }
        val pendingCount = totalCount - purchasedCount

        val estimatedTotal = calculateEstimatedTotal(items)
        val actualSpent = calculateActualSpent(items)
        val pendingEstimated = calculatePendingEstimated(items)

        val remainingBudget = calculateRemainingBudget(budgetMinor, actualSpent)
        val overBudget = calculateOverBudgetAmount(budgetMinor, actualSpent)
        val overBudgetFlag = isOverBudget(budgetMinor, actualSpent)
        val progress = calculateBudgetProgress(budgetMinor, actualSpent)

        val unpricedTotal = countUnpricedItems(items)
        val unpricedPurchased = countUnpricedPurchasedItems(items)
        val purchasedLackingActual = countPurchasedLackingActualPrice(items)

        return BudgetMetrics(
            budgetMinor = budgetMinor,
            estimatedTotalMinor = estimatedTotal,
            actualSpentMinor = actualSpent,
            pendingEstimatedMinor = pendingEstimated,
            remainingBudgetMinor = remainingBudget,
            overBudgetMinor = overBudget,
            isOverBudget = overBudgetFlag,
            budgetProgress = progress,
            totalItemsCount = totalCount,
            purchasedCount = purchasedCount,
            pendingCount = pendingCount,
            unpricedItemsCount = unpricedTotal,
            unpricedPurchasedCount = unpricedPurchased,
            countPurchasedLackingActualPrice = purchasedLackingActual
        )
    }

    /**
     * Calculates the distribution of spend across unique categories.
     * Uses actual prices where available, falling back to estimated prices.
     */
    fun calculateCategorySpendDistribution(
        items: Collection<ShoppingItemDetail>
    ): List<CategorySpendDistribution> {
        if (items.isEmpty()) return emptyList()
        val totalSpend = items.fold(0L) { acc, itemDetail ->
            val price = itemDetail.item.actualUnitPriceMinor ?: itemDetail.item.estimatedUnitPriceMinor ?: 0L
            acc + (calculateLineTotal(price, itemDetail.item.quantity) ?: 0L)
        }
        if (totalSpend <= 0L) return emptyList()

        val grouped = items.groupBy { it.category }
        return grouped.map { (cat, catItems) ->
            val catSpend = catItems.fold(0L) { acc, itemDetail ->
                val price = itemDetail.item.actualUnitPriceMinor ?: itemDetail.item.estimatedUnitPriceMinor ?: 0L
                acc + (calculateLineTotal(price, itemDetail.item.quantity) ?: 0L)
            }
            val pct = ((catSpend * 100) / totalSpend).toInt()
            CategorySpendDistribution(
                categoryId = cat?.id,
                categoryName = cat?.name ?: "Uncategorized",
                totalSpendMinor = catSpend,
                percentageOfTotal = pct,
                colorToken = cat?.colorToken
            )
        }.sortedByDescending { it.totalSpendMinor }
    }

    /**
     * Identifies the highest-impact items consuming budget.
     */
    fun calculateTopSpendCulprits(
        items: Collection<ShoppingItemDetail>,
        budgetMinor: Long?,
        limit: Int = 3
    ): List<SpendCulprit> {
        return items.mapNotNull { itemDetail ->
            val price = itemDetail.item.actualUnitPriceMinor ?: itemDetail.item.estimatedUnitPriceMinor ?: return@mapNotNull null
            val lineTotal = calculateLineTotal(price, itemDetail.item.quantity) ?: return@mapNotNull null
            val percentOfBudget = if (budgetMinor != null && budgetMinor > 0L) {
                ((lineTotal * 100) / budgetMinor).toInt()
            } else 0
            SpendCulprit(
                itemId = itemDetail.item.id,
                itemName = itemDetail.item.name,
                lineTotalMinor = lineTotal,
                percentOfBudget = percentOfBudget,
                isPurchased = itemDetail.item.isPurchased
            )
        }.sortedByDescending { it.lineTotalMinor }.take(limit)
    }
}

