package com.sysninja.expensetracker.domain

import com.sysninja.expensetracker.data.model.TransactionWithDetails
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object CsvExporter {

    fun exportTransactionsToCsv(
        transactions: List<TransactionWithDetails>,
        outputStream: OutputStream,
        currencySymbol: String = "৳"
    ) {
        val writer = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        // Write UTF-8 BOM so Microsoft Excel correctly displays Bangla and Unicode characters
        writer.write("\uFEFF")

        // CSV Header
        writer.write("Transaction ID,Type,Title,Amount,Category,Account,Destination Account,Payment Method,Date,Note,Tags\n")

        for (item in transactions) {
            val tx = item.transaction
            val catName = item.category?.name ?: ""
            val accName = item.account?.name ?: ""
            val toAccName = item.toAccount?.name ?: ""
            val amountFormatted = MoneyUtils.formatCents(tx.amountCents, currencySymbol)
            val dateFormatted = DateUtils.formatDateTime(tx.timestamp)

            val row = listOf(
                tx.id.toString(),
                escapeCsv(tx.type),
                escapeCsv(tx.title),
                escapeCsv(amountFormatted),
                escapeCsv(catName),
                escapeCsv(accName),
                escapeCsv(toAccName),
                escapeCsv(tx.paymentMethod),
                escapeCsv(dateFormatted),
                escapeCsv(tx.note ?: ""),
                escapeCsv(tx.tags ?: "")
            ).joinToString(",")

            writer.write(row)
            writer.write("\n")
        }

        writer.flush()
    }

    private fun escapeCsv(value: String): String {
        var str = value
        if (str.contains("\"")) {
            str = str.replace("\"", "\"\"")
        }
        return if (str.contains(",") || str.contains("\n") || str.contains("\r") || str.contains("\"")) {
            "\"$str\""
        } else {
            str
        }
    }
}
