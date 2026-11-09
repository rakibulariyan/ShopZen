package com.barakwebsoft.shopzen.core.util

import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class MoneyCalculatorTest {

    // ---------------- QuantityCalculator Tests ----------------

    @Test
    fun quantityCalculator_parse_supportsPeriodsAndCommas() {
        assertEquals(BigDecimal("2.5"), QuantityCalculator.parse("2.5"))
        assertEquals(BigDecimal("2.5"), QuantityCalculator.parse("2,5"))
        assertEquals(BigDecimal("10"), QuantityCalculator.parse("10"))
        assertEquals(BigDecimal("0.75"), QuantityCalculator.parse(" 0.75 "))
    }

    @Test
    fun quantityCalculator_parse_rejectsInvalidOrNonPositive() {
        assertNull(QuantityCalculator.parse(null))
        assertNull(QuantityCalculator.parse(""))
        assertNull(QuantityCalculator.parse("   "))
        assertNull(QuantityCalculator.parse("abc"))
        assertNull(QuantityCalculator.parse("0"))
        assertNull(QuantityCalculator.parse("-1.5"))
    }

    @Test
    fun quantityCalculator_format_stripsTrailingZeros() {
        assertEquals("2", QuantityCalculator.format(BigDecimal("2.0")))
        assertEquals("2", QuantityCalculator.format(BigDecimal("2.000")))
        assertEquals("2.5", QuantityCalculator.format(BigDecimal("2.50")))
        assertEquals("1.25", QuantityCalculator.format(BigDecimal("1.250")))
    }

    @Test
    fun quantityCalculator_adjust_enforcesMinimumBound() {
        assertEquals("2", QuantityCalculator.adjust("1", 1))
        assertEquals("3", QuantityCalculator.adjust("2", 1))
        assertEquals("2", QuantityCalculator.adjust("3", -1))
        assertEquals("1", QuantityCalculator.adjust("2", -1))
        // Cannot decrement below 1
        assertEquals("1", QuantityCalculator.adjust("1", -1))
        assertEquals("1", QuantityCalculator.adjust("0.5", -1))
    }

    // ---------------- Line Total Tests ----------------

    @Test
    fun calculateLineTotal_exactCalculationsWithRounding() {
        // Standard integer unit price and integer quantity: 250 minor * 3 = 750 minor
        assertEquals(750L, MoneyCalculator.calculateLineTotal(250L, "3"))

        // Decimal quantity: 250 minor * 2.5 = 625 minor
        assertEquals(625L, MoneyCalculator.calculateLineTotal(250L, "2.5"))

        // Decimal quantity with comma: 120 minor * 1,5 = 180 minor
        assertEquals(180L, MoneyCalculator.calculateLineTotal(120L, "1,5"))

        // Rounding HALF_UP check: 100 minor * 1.335 = 133.5 -> 134 minor
        assertEquals(134L, MoneyCalculator.calculateLineTotal(100L, BigDecimal("1.335")))

        // 100 minor * 1.334 = 133.4 -> 133 minor
        assertEquals(133L, MoneyCalculator.calculateLineTotal(100L, BigDecimal("1.334")))
    }

    @Test
    fun calculateLineTotal_handlesNullsAndInvalidInputs() {
        assertNull(MoneyCalculator.calculateLineTotal(null, "2"))
        assertNull(MoneyCalculator.calculateLineTotal(500L, null as String?))
        assertNull(MoneyCalculator.calculateLineTotal(500L, ""))
        assertNull(MoneyCalculator.calculateLineTotal(500L, "invalid"))
        assertNull(MoneyCalculator.calculateLineTotal(500L, "0"))
        assertNull(MoneyCalculator.calculateLineTotal(500L, "-2"))
    }

    // ---------------- Aggregates & Budget Metrics Tests ----------------

    @Test
    fun calculateAggregates_mixedItemsAndFallbacks() {
        val items = listOf(
            // Item 1: Pending, 2.5 kg at $3.00 (300 minor) = 750 minor
            ShoppingItem(
                id = "1",
                listId = "L1",
                name = "Apples",
                quantity = "2.5",
                estimatedUnitPriceMinor = 300L,
                isPurchased = false
            ),
            // Item 2: Purchased, 2 L at estimated $1.50 (150 minor), actual $1.40 (140 minor) = 280 minor
            ShoppingItem(
                id = "2",
                listId = "L1",
                name = "Milk",
                quantity = "2",
                estimatedUnitPriceMinor = 150L,
                actualUnitPriceMinor = 140L,
                isPurchased = true
            ),
            // Item 3: Purchased, 1 pc at estimated $5.00 (500 minor), no actual price entered -> falls back to 500 minor
            ShoppingItem(
                id = "3",
                listId = "L1",
                name = "Coffee",
                quantity = "1",
                estimatedUnitPriceMinor = 500L,
                actualUnitPriceMinor = null,
                isPurchased = true
            ),
            // Item 4: Pending, unpriced
            ShoppingItem(
                id = "4",
                listId = "L1",
                name = "Spices",
                quantity = "1",
                estimatedUnitPriceMinor = null,
                actualUnitPriceMinor = null,
                isPurchased = false
            )
        )

        // Estimated total: 750 (Apples) + 300 (Milk) + 500 (Coffee) + 0 (Spices) = 1550 minor
        assertEquals(1550L, MoneyCalculator.calculateEstimatedTotal(items))

        // Actual spent: 280 (Milk) + 500 (Coffee fallback) = 780 minor
        assertEquals(780L, MoneyCalculator.calculateActualSpent(items))

        // Pending estimated: 750 (Apples) + 0 (Spices) = 750 minor
        assertEquals(750L, MoneyCalculator.calculatePendingEstimated(items))

        // Unpriced items count: 1 (Spices)
        assertEquals(1, MoneyCalculator.countUnpricedItems(items))

        // Unpriced purchased items count: 0 (Coffee had estimated price fallback)
        assertEquals(0, MoneyCalculator.countUnpricedPurchasedItems(items))

        // Purchased lacking explicit actual price: 1 (Coffee)
        assertEquals(1, MoneyCalculator.countPurchasedLackingActualPrice(items))
    }

    @Test
    fun budgetMetrics_withinBudget() {
        val items = listOf(
            ShoppingItem(
                id = "1",
                listId = "L1",
                name = "Item",
                quantity = "1",
                actualUnitPriceMinor = 3000L,
                isPurchased = true
            )
        )
        // Budget = 5000L ($50.00), Spent = 3000L ($30.00)
        val metrics = MoneyCalculator.calculateBudgetMetrics(budgetMinor = 5000L, items = items)

        assertFalse(metrics.isOverBudget)
        assertNull(metrics.overBudgetMinor)
        assertEquals(2000L, metrics.remainingBudgetMinor)
        assertEquals(0.6f, metrics.budgetProgress ?: 0f, 0.001f)
    }

    @Test
    fun budgetMetrics_overBudget() {
        val items = listOf(
            ShoppingItem(
                id = "1",
                listId = "L1",
                name = "Item",
                quantity = "1",
                actualUnitPriceMinor = 6500L,
                isPurchased = true
            )
        )
        // Budget = 5000L ($50.00), Spent = 6500L ($65.00)
        val metrics = MoneyCalculator.calculateBudgetMetrics(budgetMinor = 5000L, items = items)

        assertTrue(metrics.isOverBudget)
        assertEquals(1500L, metrics.overBudgetMinor)
        assertEquals(-1500L, metrics.remainingBudgetMinor)
        assertEquals(1.3f, metrics.budgetProgress ?: 0f, 0.001f)
    }

    @Test
    fun budgetMetrics_noBudgetSet() {
        val items = listOf(
            ShoppingItem(
                id = "1",
                listId = "L1",
                name = "Item",
                quantity = "1",
                actualUnitPriceMinor = 1500L,
                isPurchased = true
            )
        )
        val metrics = MoneyCalculator.calculateBudgetMetrics(budgetMinor = null, items = items)

        assertFalse(metrics.isOverBudget)
        assertNull(metrics.overBudgetMinor)
        assertNull(metrics.remainingBudgetMinor)
        assertNull(metrics.budgetProgress)
    }

    @Test
    fun calculateLineTotal_extremeValues_noOverflow() {
        // Very large unit price: 999_999_999L minor units * 10
        val lineTotal = MoneyCalculator.calculateLineTotal(999_999_999L, "10")
        assertNotNull(lineTotal)
        assertEquals(9_999_999_990L, lineTotal)
    }

    @Test
    fun unpricedPurchasedItems_strictlyIdentifiedPerPrd() {
        val items = listOf(
            ShoppingItem(
                id = "1",
                listId = "L1",
                name = "Mystery Item",
                quantity = "1",
                estimatedUnitPriceMinor = null,
                actualUnitPriceMinor = null,
                isPurchased = true
            )
        )
        assertEquals(1, MoneyCalculator.countUnpricedPurchasedItems(items))
        val metrics = MoneyCalculator.calculateBudgetMetrics(5000L, items)
        assertEquals(1, metrics.unpricedPurchasedCount)
    }

    @Test
    fun calculateBudgetMetrics_whenOverBudget_setsIsOverBudgetAndCalculatesOverBudgetMinor() {
        val items = listOf(
            ShoppingItem(
                id = "1",
                listId = "L1",
                name = "Expensive Groceries",
                quantity = "1",
                estimatedUnitPriceMinor = 1500L,
                actualUnitPriceMinor = 1850L,
                isPurchased = true
            )
        )
        val metrics = MoneyCalculator.calculateBudgetMetrics(budgetMinor = 1500L, items = items)

        assertTrue(metrics.isOverBudget)
        assertEquals(350L, metrics.overBudgetMinor)
        assertEquals(-350L, metrics.remainingBudgetMinor)
        assertEquals(1850L, metrics.actualSpentMinor)
        assertNotNull(metrics.budgetProgress)
        assertTrue(metrics.budgetProgress!! > 1.0f)
    }

    @Test
    fun calculateBudgetMetrics_whenPurchasedItemsLacksActualPrice_flagsIsSpendEstimated() {
        val items = listOf(
            // Item 1: Purchased with confirmed actual price
            ShoppingItem(
                id = "1",
                listId = "L1",
                name = "Priced Item",
                quantity = "1",
                estimatedUnitPriceMinor = 200L,
                actualUnitPriceMinor = 250L,
                isPurchased = true
            ),
            // Item 2: Purchased but NO actual price
            ShoppingItem(
                id = "2",
                listId = "L1",
                name = "Unpriced Item",
                quantity = "1",
                estimatedUnitPriceMinor = 300L,
                actualUnitPriceMinor = null,
                isPurchased = true
            )
        )
        val metrics = MoneyCalculator.calculateBudgetMetrics(budgetMinor = 1000L, items = items)

        assertEquals(1, metrics.countPurchasedLackingActualPrice)
        assertTrue(metrics.isSpendEstimated)
    }

    @Test
    fun calculateCategorySpendDistribution_aggregatesSpendAndPercentages() {
        val catProduce = Category(id = "c1", name = "Produce", colorToken = "primary")
        val catDairy = Category(id = "c2", name = "Dairy", colorToken = "secondary")

        val items = listOf(
            ShoppingItemDetail(
                item = ShoppingItem(id = "1", listId = "L1", name = "Apples", quantity = "2", actualUnitPriceMinor = 200L, isPurchased = true),
                category = catProduce
            ),
            ShoppingItemDetail(
                item = ShoppingItem(id = "2", listId = "L1", name = "Milk", quantity = "1", actualUnitPriceMinor = 600L, isPurchased = true),
                category = catDairy
            )
        )
        // Produce: 2 * 200 = 400. Dairy: 1 * 600 = 600. Total = 1000.
        val dist = MoneyCalculator.calculateCategorySpendDistribution(items)
        assertEquals(2, dist.size)

        // Sorted descending by spend: Dairy first (600L, 60%), then Produce (400L, 40%)
        assertEquals("Dairy", dist[0].categoryName)
        assertEquals(600L, dist[0].totalSpendMinor)
        assertEquals(60, dist[0].percentageOfTotal)

        assertEquals("Produce", dist[1].categoryName)
        assertEquals(400L, dist[1].totalSpendMinor)
        assertEquals(40, dist[1].percentageOfTotal)
    }

    @Test
    fun calculateTopSpendCulprits_identifiesHighestCostItems() {
        val items = listOf(
            ShoppingItemDetail(
                item = ShoppingItem(id = "1", listId = "L1", name = "Cheap Item", quantity = "1", actualUnitPriceMinor = 100L, isPurchased = true),
                category = null
            ),
            ShoppingItemDetail(
                item = ShoppingItem(id = "2", listId = "L1", name = "Expensive Honey", quantity = "1", actualUnitPriceMinor = 800L, isPurchased = true),
                category = null
            ),
            ShoppingItemDetail(
                item = ShoppingItem(id = "3", listId = "L1", name = "Medium Coffee", quantity = "1", actualUnitPriceMinor = 500L, isPurchased = true),
                category = null
            )
        )
        val culprits = MoneyCalculator.calculateTopSpendCulprits(items, budgetMinor = 1000L, limit = 2)
        assertEquals(2, culprits.size)
        assertEquals("Expensive Honey", culprits[0].itemName)
        assertEquals(800L, culprits[0].lineTotalMinor)
        assertEquals(80, culprits[0].percentOfBudget)

        assertEquals("Medium Coffee", culprits[1].itemName)
        assertEquals(500L, culprits[1].lineTotalMinor)
        assertEquals(50, culprits[1].percentOfBudget)
    }
}

