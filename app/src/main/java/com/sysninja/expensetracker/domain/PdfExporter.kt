package com.sysninja.expensetracker.domain

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.sysninja.expensetracker.data.model.TransactionWithDetails
import java.io.OutputStream

object PdfExporter {

    private const val PAGE_WIDTH = 595 // A4 standard width
    private const val PAGE_HEIGHT = 842 // A4 standard height
    private const val MARGIN = 36

    fun generatePdfReport(
        transactions: List<TransactionWithDetails>,
        periodTitle: String,
        outputStream: OutputStream,
        currencySymbol: String = "৳"
    ) {
        val document = PdfDocument()

        val titlePaint = Paint().apply {
            color = Color.rgb(11, 18, 32)
            textSize = 18f
            isFakeBoldText = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 10f
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
        }

        val incomePaint = Paint().apply {
            color = Color.rgb(16, 185, 129)
            textSize = 9f
            isFakeBoldText = true
        }

        val expensePaint = Paint().apply {
            color = Color.rgb(239, 68, 68)
            textSize = 9f
            isFakeBoldText = true
        }

        val boxPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        val totalIncome = transactions.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amountCents }
        val totalExpense = transactions.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amountCents }
        val netCashFlow = totalIncome - totalExpense

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var y = MARGIN.toFloat() + 20f

        // Document Title
        canvas.drawText("Expense Tracker — Financial Report", MARGIN.toFloat(), y, titlePaint)
        y += 16f
        canvas.drawText("Period: $periodTitle  |  Generated on: ${DateUtils.formatDateTime(System.currentTimeMillis())}", MARGIN.toFloat(), y, subTitlePaint)
        y += 24f

        // Summary Boxes
        val boxWidth = (PAGE_WIDTH - (MARGIN * 2) - 20) / 3f
        val boxHeight = 44f

        // Income box
        canvas.drawRect(MARGIN.toFloat(), y, MARGIN.toFloat() + boxWidth, y + boxHeight, boxPaint)
        canvas.drawText("Total Income", MARGIN.toFloat() + 8f, y + 16f, subTitlePaint)
        canvas.drawText(MoneyUtils.formatCents(totalIncome, currencySymbol), MARGIN.toFloat() + 8f, y + 34f, incomePaint)

        // Expense box
        val expenseBoxLeft = MARGIN.toFloat() + boxWidth + 10f
        canvas.drawRect(expenseBoxLeft, y, expenseBoxLeft + boxWidth, y + boxHeight, boxPaint)
        canvas.drawText("Total Expense", expenseBoxLeft + 8f, y + 16f, subTitlePaint)
        canvas.drawText(MoneyUtils.formatCents(totalExpense, currencySymbol), expenseBoxLeft + 8f, y + 34f, expensePaint)

        // Net Cash Flow box
        val netBoxLeft = expenseBoxLeft + boxWidth + 10f
        canvas.drawRect(netBoxLeft, y, netBoxLeft + boxWidth, y + boxHeight, boxPaint)
        canvas.drawText("Net Cash Flow", netBoxLeft + 8f, y + 16f, subTitlePaint)
        val netColorPaint = if (netCashFlow >= 0) incomePaint else expensePaint
        canvas.drawText(MoneyUtils.formatCents(netCashFlow, currencySymbol), netBoxLeft + 8f, y + 34f, netColorPaint)

        y += boxHeight + 30f

        // Transactions Table Header
        fun drawTableHeader() {
            canvas.drawRect(MARGIN.toFloat(), y - 12f, (PAGE_WIDTH - MARGIN).toFloat(), y + 8f, boxPaint)
            canvas.drawText("Date", MARGIN.toFloat() + 4f, y, headerPaint)
            canvas.drawText("Title", MARGIN.toFloat() + 90f, y, headerPaint)
            canvas.drawText("Category", MARGIN.toFloat() + 230f, y, headerPaint)
            canvas.drawText("Account", MARGIN.toFloat() + 340f, y, headerPaint)
            canvas.drawText("Amount", MARGIN.toFloat() + 440f, y, headerPaint)
            y += 18f
        }

        drawTableHeader()

        // Rows
        for (item in transactions) {
            if (y > PAGE_HEIGHT - MARGIN - 30) {
                // Next page
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN.toFloat() + 20f
                drawTableHeader()
            }

            val tx = item.transaction
            val dateStr = DateUtils.formatDate(tx.timestamp)
            val titleStr = if (tx.title.length > 22) tx.title.take(20) + ".." else tx.title
            val catStr = item.category?.name ?: "-"
            val catShort = if (catStr.length > 18) catStr.take(16) + ".." else catStr
            val accStr = item.account?.name ?: "-"
            val amountFormatted = MoneyUtils.formatCents(tx.amountCents, currencySymbol)

            canvas.drawText(dateStr, MARGIN.toFloat() + 4f, y, bodyPaint)
            canvas.drawText(titleStr, MARGIN.toFloat() + 90f, y, bodyPaint)
            canvas.drawText(catShort, MARGIN.toFloat() + 230f, y, bodyPaint)
            canvas.drawText(accStr, MARGIN.toFloat() + 340f, y, bodyPaint)

            val amtPaint = when (tx.type) {
                "INCOME" -> incomePaint
                "EXPENSE" -> expensePaint
                else -> bodyPaint
            }
            canvas.drawText(amountFormatted, MARGIN.toFloat() + 440f, y, amtPaint)

            canvas.drawLine(MARGIN.toFloat(), y + 4f, (PAGE_WIDTH - MARGIN).toFloat(), y + 4f, linePaint)
            y += 18f
        }

        // Footer
        canvas.drawText("Page $pageNumber  |  Expense Tracker developed by Khalequzzaman", MARGIN.toFloat(), PAGE_HEIGHT - 20f, subTitlePaint)

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }
}
