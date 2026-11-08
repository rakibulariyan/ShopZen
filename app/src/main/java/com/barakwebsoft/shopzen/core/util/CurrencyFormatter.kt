package com.barakwebsoft.shopzen.core.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import java.util.Locale

/**
 * Supported currency representation for selection and display.
 */
data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val fractionDigits: Int
)

/**
 * Formats monetary amounts stored in integer minor units (Long) without binary floating point.
 */
object CurrencyFormatter {

    /**
     * Curated list of common ISO 4217 currencies supported in the app.
     */
    val commonCurrencies: List<CurrencyInfo> = listOf(
        CurrencyInfo("USD", "US Dollar", "$", 2),
        CurrencyInfo("EUR", "Euro", "€", 2),
        CurrencyInfo("GBP", "British Pound", "£", 2),
        CurrencyInfo("INR", "Indian Rupee", "₹", 2),
        CurrencyInfo("CAD", "Canadian Dollar", "CA$", 2),
        CurrencyInfo("AUD", "Australian Dollar", "A$", 2),
        CurrencyInfo("JPY", "Japanese Yen", "¥", 0),
        CurrencyInfo("BRL", "Brazilian Real", "R$", 2),
        CurrencyInfo("MXN", "Mexican Peso", "MX$", 2),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", 2),
        CurrencyInfo("CNY", "Chinese Yuan", "¥", 2),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", 2),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", 2),
        CurrencyInfo("KRW", "South Korean Won", "₩", 0),
        CurrencyInfo("SEK", "Swedish Krona", "kr", 2),
        CurrencyInfo("NOK", "Norwegian Krone", "kr", 2),
        CurrencyInfo("DKK", "Danish Krone", "kr", 2),
        CurrencyInfo("PLN", "Polish Zloty", "zł", 2),
        CurrencyInfo("ZAR", "South African Rand", "R", 2),
        CurrencyInfo("AED", "UAE Dirham", "AED", 2)
    )

    fun getCurrencyInfo(code: String): CurrencyInfo {
        val found = commonCurrencies.find { it.code.equals(code, ignoreCase = true) }
        if (found != null) return found

        val currency = try {
            Currency.getInstance(code.uppercase())
        } catch (_: Exception) {
            null
        }

        return CurrencyInfo(
            code = code.uppercase(),
            name = currency?.displayName ?: code.uppercase(),
            symbol = currency?.getSymbol(Locale.getDefault()) ?: code.uppercase(),
            fractionDigits = currency?.defaultFractionDigits?.coerceAtLeast(0) ?: 2
        )
    }

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

        val info = getCurrencyInfo(currencyCode)
        val symbol = info.symbol
        val fractionDigits = info.fractionDigits

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

    /**
     * Parses a user-entered decimal string (e.g. "50.00", "50", "12.5") in major units
     * into exact integer minor units without binary floating point precision loss.
     * Returns null if string is blank or cannot be parsed.
     */
    fun parseToMinor(amountStr: String?, currencyCode: String): Long? {
        val clean = amountStr?.trim()?.replace(",", ".") ?: return null
        if (clean.isBlank()) return null

        val info = getCurrencyInfo(currencyCode)
        val fractionDigits = info.fractionDigits

        return try {
            val bd = BigDecimal(clean)
            if (bd < BigDecimal.ZERO) return null
            val scaled = bd.movePointRight(fractionDigits).setScale(0, RoundingMode.HALF_UP)
            scaled.toLong()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Converts integer minor units to user-editable string in major units (e.g. 5000L -> "50.00").
     * Returns empty string if amount is null.
     */
    fun toMajorUnitsString(amountMinor: Long?, currencyCode: String): String {
        if (amountMinor == null) return ""
        val info = getCurrencyInfo(currencyCode)
        val fractionDigits = info.fractionDigits

        if (fractionDigits <= 0) return amountMinor.toString()

        val factor = (1..fractionDigits).fold(1L) { acc, _ -> acc * 10L }
        val whole = amountMinor / factor
        val fraction = kotlin.math.abs(amountMinor % factor).toString().padStart(fractionDigits, '0')
        return "$whole.$fraction"
    }
}


