package com.barakwebsoft.shopzen.core.util

import java.util.Currency
import java.util.Locale

/**
 * Formats monetary amounts stored in integer minor units (Long) without binary floating point.
 */
object CurrencyFormatter {

    /**
     * Formats an amount in minor units with the appropriate currency symbol and fractional digits.
     * If [amountMinor] is null, returns [nullFallback] (default "—").
     *
     * Examples:
     * - USD (fractionDigits = 2): 5000L -> "$50.00", 50L -> "$0.50"
     * - JPY (fractionDigits = 0): 500L -> "¥500"
     * - BHD (fractionDigits = 3): 1250L -> "BHD 1.250"
     */
    fun format(
        amountMinor: Long?,
        currencyCode: String,
        nullFallback: String = "—"
    ): String {
        if (amountMinor == null) return nullFallback

        val currency = try {
            Currency.getInstance(currencyCode)
        } catch (_: Exception) {
            null
        }

        val symbol = currency?.getSymbol(Locale.getDefault()) ?: currencyCode
        val fractionDigits = currency?.defaultFractionDigits ?: 2

        val isNegative = amountMinor < 0
        val absAmount = kotlin.math.abs(amountMinor)

        val formattedNumber = if (fractionDigits <= 0) {
            absAmount.toString()
        } else {
            val factor = (1..fractionDigits).fold(1L) { acc, _ -> acc * 10L }
            val whole = absAmount / factor
            val fraction = absAmount % factor
            val fractionStr = fraction.toString().padStart(fractionDigits, '0')
            "$whole.$fractionStr"
        }

        val formattedWithSymbol = if (symbol.length == 1 || symbol == "$" || symbol == "€" || symbol == "£" || symbol == "¥" || symbol == "₹") {
            "$symbol$formattedNumber"
        } else {
            "$symbol $formattedNumber"
        }

        return if (isNegative) "-$formattedWithSymbol" else formattedWithSymbol
    }
}

