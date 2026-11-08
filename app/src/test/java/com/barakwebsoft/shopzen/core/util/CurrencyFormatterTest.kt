package com.barakwebsoft.shopzen.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun format_nullAmount_returnsFallback() {
        assertEquals("—", CurrencyFormatter.format(null, "USD"))
        assertEquals("No budget", CurrencyFormatter.format(null, "USD", nullFallback = "No budget"))
    }

    @Test
    fun format_twoDecimalCurrencies() {
        // 5000 cents = $50.00
        val usd50 = CurrencyFormatter.format(5000L, "USD")
        assertTrue("USD format should contain 50.00", usd50.contains("50.00"))

        // 50 cents = $0.50
        val usdCent = CurrencyFormatter.format(50L, "USD")
        assertTrue("USD format should contain 0.50", usdCent.contains("0.50"))

        // 0 cents = $0.00
        val usdZero = CurrencyFormatter.format(0L, "USD")
        assertTrue("USD format should contain 0.00", usdZero.contains("0.00"))
    }

    @Test
    fun format_zeroDecimalCurrencies() {
        // JPY has 0 fraction digits
        val jpy = CurrencyFormatter.format(1500L, "JPY")
        assertTrue("JPY format should contain 1500 without decimal points", jpy.contains("1500") && !jpy.contains(".00"))
    }

    @Test
    fun format_largeAmountsWithoutPrecisionLoss() {
        // 100 million minor units
        val large = CurrencyFormatter.format(100_000_000_00L, "USD")
        assertTrue("Large amount should contain 100000000.00 without exponent notation", large.contains("100000000.00"))
    }

    @Test
    fun format_negativeAmount() {
        val neg = CurrencyFormatter.format(-2500L, "USD")
        assertTrue("Negative amount should be prefixed with -", neg.startsWith("-") && neg.contains("25.00"))
    }
}

