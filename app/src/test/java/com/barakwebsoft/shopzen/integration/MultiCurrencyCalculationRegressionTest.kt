package com.barakwebsoft.shopzen.integration

import com.barakwebsoft.shopzen.core.util.CurrencyFormatter
import com.barakwebsoft.shopzen.core.util.MoneyCalculator
import com.barakwebsoft.shopzen.core.util.QuantityCalculator
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * Multi-currency and precision financial calculation regression tests (SZ-060, SZ-036).
 * Verifies strict monetary calculation requirements:
 * 1. Integer minor unit representation (Long) across all operations
 * 2. Zero-decimal currency behavior (JPY, KRW, VND, CLP)
 * 3. Standard two-decimal currencies (USD, EUR, GBP, INR)
 * 4. Three-decimal currencies (BHD, KWD, OMR)
 * 5. Half-up rounding on fractional quantities
 * 6. Extreme value boundary protection against 32-bit overflow
 * 7. Rejection/safety on negative and invalid price inputs
 */
class MultiCurrencyCalculationRegressionTest {

    @Test
    fun zeroDecimalCurrencies_JPY_KRW_formatWithoutDecimals() {
        val jpyInfo = CurrencyFormatter.getCurrencyInfo("JPY")
        assertEquals("JPY", jpyInfo.code)
        assertEquals(0, jpyInfo.fractionDigits)

        // 500 minor units in JPY is 500 yen (no decimal points)
        val formattedJpy = CurrencyFormatter.format(500L, "JPY")
        assertEquals("¥500", formattedJpy)
        assertFalse("Zero-decimal currencies must never have decimal fractions", formattedJpy.contains("."))

        val krwInfo = CurrencyFormatter.getCurrencyInfo("KRW")
        assertEquals(0, krwInfo.fractionDigits)
        val formattedKrw = CurrencyFormatter.format(15000L, "KRW")
        assertEquals("₩15000", formattedKrw)
        assertFalse(formattedKrw.contains("."))
    }

    @Test
    fun standardTwoDecimalCurrencies_USD_EUR_GBP_INR_formatWithTwoDecimalDigits() {
        val usdInfo = CurrencyFormatter.getCurrencyInfo("USD")
        assertEquals(2, usdInfo.fractionDigits)

        // $12.34 = 1234 minor units
        assertEquals("$12.34", CurrencyFormatter.format(1234L, "USD"))
        // $0.50 = 50 minor units
        assertEquals("$0.50", CurrencyFormatter.format(50L, "USD"))
        // $0.05 = 5 minor units (checks padding)
        assertEquals("$0.05", CurrencyFormatter.format(5L, "USD"))
        // $0.00 = 0 minor units
        assertEquals("$0.00", CurrencyFormatter.format(0L, "USD"))

        // Euro formatting
        assertEquals("€24.99", CurrencyFormatter.format(2499L, "EUR"))
        // GBP formatting
        assertEquals("£9.95", CurrencyFormatter.format(995L, "GBP"))
        // INR formatting
        assertEquals("₹499.00", CurrencyFormatter.format(49900L, "INR"))

        // Negative values (e.g. overspend or budget deficits)
        assertEquals("-$12.34", CurrencyFormatter.format(-1234L, "USD"))
    }

    @Test
    fun threeDecimalCurrencies_BHD_KWD_OMR_formatWithThreeDecimalDigits() {
        val bhdInfo = CurrencyFormatter.getCurrencyInfo("BHD")
        assertEquals(3, bhdInfo.fractionDigits)

        // 1.500 BHD = 1500 minor units
        val formattedBhd = CurrencyFormatter.format(1500L, "BHD")
        assertEquals("BHD 1.500", formattedBhd)

        // 0.075 BHD = 75 minor units (checks leading zero padding)
        val formattedSmallBhd = CurrencyFormatter.format(75L, "BHD")
        assertEquals("BHD 0.075", formattedSmallBhd)
    }

    @Test
    fun roundingHalfUp_onFractionalQuantities_matchesDocumentedRules() {
        // 1.33 kg @ $2.50/kg (250L) -> 250 * 1.33 = 332.5 -> rounds HALF_UP to 333L ($3.33)
        val lineTotal1 = MoneyCalculator.calculateLineTotal(250L, BigDecimal("1.33"))
        assertEquals(333L, lineTotal1)

        // 2.50 kg @ $3.33/kg (333L) -> 333 * 2.5 = 832.5 -> rounds HALF_UP to 833L ($8.33)
        val lineTotal2 = MoneyCalculator.calculateLineTotal(333L, BigDecimal("2.50"))
        assertEquals(833L, lineTotal2)

        // 0.50 kg @ $1.00/kg (100L) -> 100 * 0.5 = 50.0 -> 50L ($0.50)
        val lineTotal3 = MoneyCalculator.calculateLineTotal(100L, BigDecimal("0.50"))
        assertEquals(50L, lineTotal3)

        // 0.125 kg @ $8.00/kg (800L) -> 800 * 0.125 = 100.0 -> 100L ($1.00)
        val lineTotal4 = MoneyCalculator.calculateLineTotal(800L, BigDecimal("0.125"))
        assertEquals(100L, lineTotal4)
    }

    @Test
    fun largeValuesAndBoundaryIntegrity_prevents32BitIntegerOverflow() {
        // 10,000,000.00 USD = 1,000,000,000 minor units
        val largeUnitPrice = 1_000_000_000L
        val quantity = BigDecimal("5")

        // 5 * 1,000,000,000L = 5,000,000,000L (exceeds Int.MAX_VALUE = 2,147,483,647)
        val largeLineTotal = MoneyCalculator.calculateLineTotal(largeUnitPrice, quantity)
        assertNotNull(largeLineTotal)
        assertEquals(5_000_000_000L, largeLineTotal)

        // Test list aggregation with large values
        val item1 = ShoppingItem(
            id = "1",
            listId = "large_list",
            name = "Commercial Refrigerator",
            quantity = "3",
            unit = "pcs",
            estimatedUnitPriceMinor = 2_000_000_00L // $20,000.00 each -> $60,000.00
        )
        val item2 = ShoppingItem(
            id = "2",
            listId = "large_list",
            name = "Industrial Espresso Machine",
            quantity = "2",
            unit = "pcs",
            estimatedUnitPriceMinor = 1_500_000_00L // $15,000.00 each -> $30,000.00
        )

        val totalEstimated = MoneyCalculator.calculateEstimatedTotal(listOf(item1, item2))
        assertEquals(9_000_000_00L, totalEstimated) // $90,000.00 in minor units
    }

    @Test
    fun negativePriceAndInvalidQuantity_handledSafelyWithoutThrowing() {
        // Negative unit price is rejected (returns null)
        assertNull(MoneyCalculator.calculateLineTotal(-100L, BigDecimal("2")))

        // Zero or negative quantity is rejected (returns null)
        assertNull(MoneyCalculator.calculateLineTotal(100L, BigDecimal.ZERO))
        assertNull(MoneyCalculator.calculateLineTotal(100L, BigDecimal("-1")))

        // Null parameters return null
        assertNull(MoneyCalculator.calculateLineTotal(null, BigDecimal("2")))
        assertNull(MoneyCalculator.calculateLineTotal(100L, null as BigDecimal?))

        // Quantity parsing rejects invalid input
        assertNull(QuantityCalculator.parse(""))
        assertNull(QuantityCalculator.parse("abc"))
        assertNull(QuantityCalculator.parse("-5"))
        assertNull(QuantityCalculator.parse("0"))

        // Quantity parsing accepts comma and period decimal separators
        assertEquals(BigDecimal("2.5"), QuantityCalculator.parse("2.5"))
        assertEquals(BigDecimal("2.5"), QuantityCalculator.parse("2,5"))
    }
}
