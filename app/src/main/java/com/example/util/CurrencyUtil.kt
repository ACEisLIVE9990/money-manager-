package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtil {
    private val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    private val wholeFormatter = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))

    val SUPPORTED_CURRENCIES = listOf(
        CurrencyOption("₹", "INR (Indian Rupee)", "₹"),
        CurrencyOption("$", "USD (US Dollar)", "$"),
        CurrencyOption("€", "EUR (Euro)", "€"),
        CurrencyOption("£", "GBP (British Pound)", "£"),
        CurrencyOption("¥", "JPY (Japanese Yen)", "¥"),
        CurrencyOption("C$", "CAD (Canadian Dollar)", "C$"),
        CurrencyOption("A$", "AUD (Australian Dollar)", "A$"),
        CurrencyOption("AED", "AED (UAE Dirham)", "AED "),
        CurrencyOption("₱", "PHP (Philippine Peso)", "₱")
    )

    data class CurrencyOption(val symbol: String, val label: String, val prefix: String)

    fun format(amount: Double, symbol: String = "₹"): String {
        if (amount.isNaN() || amount.isInfinite()) return "$symbol 0.00"
        return if (amount % 1.0 == 0.0) {
            "$symbol ${wholeFormatter.format(amount)}"
        } else {
            "$symbol ${formatter.format(amount)}"
        }
    }

    fun formatCompact(amount: Double, symbol: String = "₹"): String {
        if (amount.isNaN() || amount.isInfinite()) return "$symbol 0"
        return when {
            kotlin.math.abs(amount) >= 1_000_000 -> "$symbol ${wholeFormatter.format(amount / 1_000_000)}M"
            kotlin.math.abs(amount) >= 100_000 -> "$symbol ${wholeFormatter.format(amount / 1_000)}k"
            else -> format(amount, symbol)
        }
    }

    fun parseAmount(input: String): Double? {
        val sanitized = input.trim().replace(",", "").replace(" ", "")
        return sanitized.toDoubleOrNull()
    }
}
