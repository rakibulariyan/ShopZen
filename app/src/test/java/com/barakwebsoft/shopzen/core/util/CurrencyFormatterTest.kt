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

    @Test
    fun parseToMinor_twoDecimalCurrencies() {
        // "50" -> 5000L
        assertEquals(5000L, CurrencyFormatter.parseToMinor("50", "USD"))
        // "50.00" -> 5000L
        assertEquals(5000L, CurrencyFormatter.parseToMinor("50.00", "USD"))
        // "12.5" -> 1250L
        assertEquals(1250L, CurrencyFormatter.parseToMinor("12.5", "USD"))
        // "0.99" -> 99L
        assertEquals(99L, CurrencyFormatter.parseToMinor("0.99", "USD"))
        // Comma decimal separator "12,50" -> 1250L
        assertEquals(1250L, CurrencyFormatter.parseToMinor("12,50", "USD"))
    }

    @Test
    fun parseToMinor_zeroDecimalCurrencies() {
        // JPY
        assertEquals(1500L, CurrencyFormatter.parseToMinor("1500", "JPY"))
        assertEquals(0L, CurrencyFormatter.parseToMinor("0", "JPY"))
    }

    @Test
    fun parseToMinor_blankOrInvalid_returnsNull() {
        assertEquals(null, CurrencyFormatter.parseToMinor(null, "USD"))
        assertEquals(null, CurrencyFormatter.parseToMinor("", "USD"))
        assertEquals(null, CurrencyFormatter.parseToMinor("   ", "USD"))
        assertEquals(null, CurrencyFormatter.parseToMinor("abc", "USD"))
        assertEquals(null, CurrencyFormatter.parseToMinor("-50", "USD"))
    }

    @Test
    fun toMajorUnitsString_conversion() {
        assertEquals("50.00", CurrencyFormatter.toMajorUnitsString(5000L, "USD"))
        assertEquals("0.50", CurrencyFormatter.toMajorUnitsString(50L, "USD"))
        assertEquals("0.05", CurrencyFormatter.toMajorUnitsString(5L, "USD"))
        assertEquals("1500", CurrencyFormatter.toMajorUnitsString(1500L, "JPY"))
        assertEquals("", CurrencyFormatter.toMajorUnitsString(null, "USD"))
    }
}

