package com.sysninja.expensetracker.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyUtils {

    /**
     * Converts a user entered decimal amount string (e.g. "1250.50") into minor units / cents (Long).
     * Rejects invalid, negative, or malformed amounts.
     */
    fun parseToCents(amountStr: String?): Long {
        if (amountStr.isNullOrBlank()) return 0L
        val cleanStr = amountStr.trim().replace(",", "")
        return try {
            val bd = BigDecimal(cleanStr)
            if (bd <= BigDecimal.ZERO) return 0L
            bd.setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .longValueExact()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Converts minor units (Long) to BigDecimal.
     */
    fun toBigDecimal(cents: Long): BigDecimal {
        return BigDecimal(cents).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
    }

    /**
     * Formats minor units into readable currency format.
     * E.g. cents = 125050, currencySymbol = "৳" -> "৳ 1,250.50"
     */
    fun formatCents(cents: Long, currencySymbol: String = "৳"): String {
        val bd = toBigDecimal(cents)
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,##0.00", symbols)
        val formattedNumber = formatter.format(bd)
        return if (currencySymbol.isBlank()) formattedNumber else "$currencySymbol $formattedNumber"
    }

    /**
     * Formats without decimals if .00 (e.g. for charts and compact spaces)
     */
    fun formatCompact(cents: Long, currencySymbol: String = "৳"): String {
        val bd = toBigDecimal(cents)
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = if (cents % 100L == 0L) {
            DecimalFormat("#,##0", symbols)
        } else {
            DecimalFormat("#,##0.00", symbols)
        }
        val formattedNumber = formatter.format(bd)
        return if (currencySymbol.isBlank()) formattedNumber else "$currencySymbol $formattedNumber"
    }

    /**
     * Calculates percentage safely, avoiding division by zero.
     */
    fun calculatePercentage(numeratorCents: Long, denominatorCents: Long): Int {
        if (denominatorCents <= 0L) return 0
        val percentage = (numeratorCents.toDouble() / denominatorCents.toDouble()) * 100.0
        return percentage.coerceIn(0.0, 1000.0).toInt()
    }
}
