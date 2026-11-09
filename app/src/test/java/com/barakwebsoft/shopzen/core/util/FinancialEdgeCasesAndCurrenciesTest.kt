package com.barakwebsoft.shopzen.core.util

import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * Task SZ-036: Comprehensive verification for rounding rules, zero-decimal currencies,
 * three-decimal currencies, large values, and financial boundary edge cases.
 *
 * Guarantees zero binary floating-point drift, strict HALF_UP rounding, safe division,
 * and robust handling of international ISO 4217 currencies.
 */
class FinancialEdgeCasesAndCurrenciesTest {

    // =========================================================================
    // 1. Zero-Decimal Currencies (JPY, KRW)
    // =========================================================================

    @Test
    fun zeroDecimalCurrencies_fractionDigits_isZero() {
        val jpyInfo = CurrencyFormatter.getCurrencyInfo("JPY")
        assertEquals(0, jpyInfo.fractionDigits)
        assertEquals("¥", jpyInfo.symbol)

        val krwInfo = CurrencyFormatter.getCurrencyInfo("KRW")
        assertEquals(0, krwInfo.fractionDigits)
        assertEquals("₩", krwInfo.symbol)
    }

    @Test
    fun zeroDecimalCurrencies_formatting_containsNoDecimals() {
        assertEquals("¥0", CurrencyFormatter.format(0L, "JPY"))
        assertEquals("¥500", CurrencyFormatter.format(500L, "JPY"))
        assertEquals("¥15000", CurrencyFormatter.format(15000L, "JPY"))
        assertEquals("-¥2500", CurrencyFormatter.format(-2500L, "JPY"))

        assertEquals("₩0", CurrencyFormatter.format(0L, "KRW"))
        assertEquals("₩10000", CurrencyFormatter.format(10000L, "KRW"))
    }

    @Test
    fun zeroDecimalCurrencies_parsing_convertsDirectlyToMinorUnits() {
        assertEquals(0L, CurrencyFormatter.parseToMinor("0", "JPY"))
        assertEquals(500L, CurrencyFormatter.parseToMinor("500", "JPY"))
        assertEquals(15000L, CurrencyFormatter.parseToMinor("15000", "JPY"))

        // Rounding fractional input for zero-decimal currency
        assertEquals(500L, CurrencyFormatter.parseToMinor("499.6", "JPY"))
        assertEquals(499L, CurrencyFormatter.parseToMinor("499.4", "JPY"))
        assertEquals(500L, CurrencyFormatter.parseToMinor("499.5", "JPY")) // HALF_UP
    }

    @Test
    fun zeroDecimalCurrencies_toMajorUnitsString_matchesInteger() {
        assertEquals("0", CurrencyFormatter.toMajorUnitsString(0L, "JPY"))
        assertEquals("500", CurrencyFormatter.toMajorUnitsString(500L, "JPY"))
        assertEquals("15000", CurrencyFormatter.toMajorUnitsString(15000L, "JPY"))
    }

    @Test
    fun zeroDecimalCurrencies_fullShoppingListCalculations() {
        val jpyItems = listOf(
            ShoppingItem(
                id = "j1",
                listId = "list_jpy",
                name = "Ramen Noodles",
                quantity = "5",
                estimatedUnitPriceMinor = 350L, // 350 JPY each
                actualUnitPriceMinor = 320L,
                isPurchased = true
            ),
            ShoppingItem(
                id = "j2",
                listId = "list_jpy",
                name = "Wagyu Beef",
                quantity = "1.5", // 1.5 kg
                estimatedUnitPriceMinor = 4000L,
                actualUnitPriceMinor = null,
                isPurchased = false
            )
        )

        val metrics = MoneyCalculator.calculateBudgetMetrics(
            budgetMinor = 8000L, // 8000 JPY budget
            items = jpyItems
        )

        // Item 1 actual: 5 * 320 = 1600 JPY
        assertEquals(1600L, metrics.actualSpentMinor)
        // Item 2 pending: 1.5 * 4000 = 6000 JPY
        assertEquals(6000L, metrics.pendingEstimatedMinor)
        // Total estimated: (5 * 350) + (1.5 * 4000) = 1750 + 6000 = 7750 JPY
        assertEquals(7750L, metrics.estimatedTotalMinor)
        // Remaining: 8000 - 1600 = 6400 JPY
        assertEquals(6400L, metrics.remainingBudgetMinor)
        assertFalse(metrics.isOverBudget)
        assertNull(metrics.overBudgetMinor)
    }

    // =========================================================================
    // 2. Three-Decimal Currencies (BHD, KWD, OMR)
    // =========================================================================

    @Test
    fun threeDecimalCurrencies_fractionDigits_isThree() {
        val kwdInfo = CurrencyFormatter.getCurrencyInfo("KWD")
        assertEquals(3, kwdInfo.fractionDigits)

        val bhdInfo = CurrencyFormatter.getCurrencyInfo("BHD")
        assertEquals(3, bhdInfo.fractionDigits)
    }

    @Test
    fun threeDecimalCurrencies_formatting_formatsThreeFractionDigits() {
        // 1250 minor units = 1.250 KWD
        val formattedKwd = CurrencyFormatter.format(1250L, "KWD")
        assertTrue("KWD format should contain 1.250", formattedKwd.contains("1.250"))

        // 5 minor units = 0.005 KWD
        val formattedSmall = CurrencyFormatter.format(5L, "KWD")
        assertTrue("KWD format should contain 0.005", formattedSmall.contains("0.005"))

        // 0 minor units = 0.000 KWD
        val formattedZero = CurrencyFormatter.format(0L, "KWD")
        assertTrue("KWD format should contain 0.000", formattedZero.contains("0.000"))
    }

    @Test
    fun threeDecimalCurrencies_parsing_preservesThreeDecimalScale() {
        assertEquals(1250L, CurrencyFormatter.parseToMinor("1.250", "KWD"))
        assertEquals(1250L, CurrencyFormatter.parseToMinor("1.25", "KWD"))
        assertEquals(5L, CurrencyFormatter.parseToMinor("0.005", "KWD"))
        assertEquals(5000L, CurrencyFormatter.parseToMinor("5", "KWD"))

        // Rounding at 4th decimal place with HALF_UP
        assertEquals(1235L, CurrencyFormatter.parseToMinor("1.2345", "KWD"))
        assertEquals(1234L, CurrencyFormatter.parseToMinor("1.2344", "KWD"))
    }

    @Test
    fun threeDecimalCurrencies_toMajorUnitsString() {
        assertEquals("1.250", CurrencyFormatter.toMajorUnitsString(1250L, "KWD"))
        assertEquals("0.005", CurrencyFormatter.toMajorUnitsString(5L, "KWD"))
        assertEquals("0.000", CurrencyFormatter.toMajorUnitsString(0L, "KWD"))
    }

    // =========================================================================
    // 3. Exact Rounding Verification (HALF_UP)
    // =========================================================================

    @Test
    fun rounding_halfUpEdgeCases() {
        // 0.5 minor rounds up to 1 minor
        assertEquals(1L, MoneyCalculator.calculateLineTotal(1L, BigDecimal("0.5")))
        // 0.49 minor rounds down to 0 minor
        assertEquals(0L, MoneyCalculator.calculateLineTotal(1L, BigDecimal("0.49")))

        // 1.5 minor rounds up to 2 minor
        assertEquals(2L, MoneyCalculator.calculateLineTotal(1L, BigDecimal("1.5")))
        // 2.5 minor rounds up to 3 minor (standard HALF_UP, not banker's rounding)
        assertEquals(3L, MoneyCalculator.calculateLineTotal(1L, BigDecimal("2.5")))
        // 3.5 minor rounds up to 4 minor
        assertEquals(4L, MoneyCalculator.calculateLineTotal(1L, BigDecimal("3.5")))

        // Repeating decimals: 1/3 kg at $10.00 (1000 minor)
        // 1000 * 0.33333333 = 333.33333 -> 333 minor
        assertEquals(333L, MoneyCalculator.calculateLineTotal(1000L, BigDecimal("0.33333333")))
        // 2/3 kg at $10.00 (1000 minor)
        // 1000 * 0.66666667 = 666.66667 -> 667 minor
        assertEquals(667L, MoneyCalculator.calculateLineTotal(1000L, BigDecimal("0.66666667")))
    }

    // =========================================================================
    // 4. Large Values and 64-bit Overflow Safety
    // =========================================================================

    @Test
    fun largeValues_hundredBillionMinorUnits_calculatesAccurately() {
        // 100 billion cents = 1 billion dollars
        val hundredBillion = 100_000_000_000L

        val largeItem = ShoppingItem(
            id = "huge1",
            listId = "l1",
            name = "Commercial Warehouse Stock",
            quantity = "1000",
            estimatedUnitPriceMinor = hundredBillion / 1000L, // 100 million minor
            actualUnitPriceMinor = hundredBillion / 1000L,
            isPurchased = true
        )

        val metrics = MoneyCalculator.calculateBudgetMetrics(
            budgetMinor = 200_000_000_000L, // 200 billion minor
            items = listOf(largeItem)
        )

        assertEquals(hundredBillion, metrics.actualSpentMinor)
        assertEquals(hundredBillion, metrics.estimatedTotalMinor)
        assertEquals(100_000_000_000L, metrics.remainingBudgetMinor)
        assertFalse(metrics.isOverBudget)
        assertEquals(0.5f, metrics.budgetProgress ?: 0f, 0.001f)

        // Verify currency formatting does not revert to scientific notation "1E11"
        val formatted = CurrencyFormatter.format(hundredBillion, "USD")
        assertTrue("Should contain exact decimal value without exponent", formatted.contains("1000000000.00"))
    }

    @Test
    fun largeValues_extremeBudgetOverrun_calculatesWithoutCrash() {
        val items = listOf(
            ShoppingItem(
                id = "item1",
                listId = "l1",
                name = "High End Appliance",
                quantity = "10",
                estimatedUnitPriceMinor = 50_000_00L, // $50,000
                actualUnitPriceMinor = 50_000_00L,
                isPurchased = true
            )
        )

        // Budget is only $10.00 (1000 minor)
        val metrics = MoneyCalculator.calculateBudgetMetrics(
            budgetMinor = 1000L,
            items = items
        )

        // Spent is $500,000 (50,000,000 minor)
        assertEquals(50_000_000L, metrics.actualSpentMinor)
        assertTrue(metrics.isOverBudget)
        assertEquals(49_999_000L, metrics.overBudgetMinor)
        assertEquals(-49_999_000L, metrics.remainingBudgetMinor)
        // 50,000,000 / 1,000 = 50,000.0f
        assertEquals(50000.0f, metrics.budgetProgress ?: 0f, 0.1f)
    }

    // =========================================================================
    // 5. Boundary & Extreme Edge Cases
    // =========================================================================

    @Test
    fun boundaryEdgeCases_zeroBudgetAndZeroSpend() {
        // Zero budget: should not divide by zero
        assertNull(MoneyCalculator.calculateBudgetProgress(0L, 500L))
        assertNull(MoneyCalculator.calculateBudgetProgress(null, 500L))
        assertFalse(MoneyCalculator.isOverBudget(0L, 500L))
        assertFalse(MoneyCalculator.isOverBudget(null, 500L))

        // Empty items list
        val emptyMetrics = MoneyCalculator.calculateBudgetMetrics(5000L, emptyList())
        assertEquals(0L, emptyMetrics.actualSpentMinor)
        assertEquals(0L, emptyMetrics.estimatedTotalMinor)
        assertEquals(0L, emptyMetrics.pendingEstimatedMinor)
        assertEquals(5000L, emptyMetrics.remainingBudgetMinor)
        assertFalse(emptyMetrics.isOverBudget)
        assertEquals(0f, emptyMetrics.budgetProgress ?: 1f, 0.001f)
        assertEquals(0, emptyMetrics.totalItemsCount)
    }

    @Test
    fun boundaryEdgeCases_extremeFractionalQuantities() {
        // 0.0001 quantity * 10,000 minor = 1 minor
        assertEquals(1L, MoneyCalculator.calculateLineTotal(10_000L, "0.0001"))

        // 0.00004 * 10,000 = 0.4 -> 0 minor
        assertEquals(0L, MoneyCalculator.calculateLineTotal(10_000L, "0.00004"))

        // 0.00005 * 10,000 = 0.5 -> 1 minor
        assertEquals(1L, MoneyCalculator.calculateLineTotal(10_000L, "0.00005"))
    }

    @Test
    fun boundaryEdgeCases_commaAndPeriodDecimalSeparators() {
        // European comma format "12,50" vs standard "12.50"
        assertEquals(1250L, CurrencyFormatter.parseToMinor("12,50", "EUR"))
        assertEquals(1250L, CurrencyFormatter.parseToMinor("12.50", "EUR"))

        // Quantity with comma
        assertEquals(375L, MoneyCalculator.calculateLineTotal(250L, "1,5"))
        assertEquals(375L, MoneyCalculator.calculateLineTotal(250L, "1.5"))
    }

    @Test
    fun boundaryEdgeCases_invalidOrNegativeAmounts_handledSafely() {
        assertNull(CurrencyFormatter.parseToMinor("-10.00", "USD"))
        assertNull(CurrencyFormatter.parseToMinor("not_a_number", "USD"))
        assertNull(CurrencyFormatter.parseToMinor("", "USD"))
        assertNull(CurrencyFormatter.parseToMinor(null, "USD"))

        assertNull(MoneyCalculator.calculateLineTotal(-500L, "2"))
        assertNull(MoneyCalculator.calculateLineTotal(500L, "-2"))
        assertNull(MoneyCalculator.calculateLineTotal(500L, "0"))
    }
}

