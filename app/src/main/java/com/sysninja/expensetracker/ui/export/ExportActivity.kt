package com.sysninja.expensetracker.ui.export

import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.domain.CsvExporter
import com.sysninja.expensetracker.domain.PdfExporter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ExportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@ExportActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.export_title)
            setTitleTextColor(ContextCompat.getColor(this@ExportActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@ExportActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@ExportActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val csvBtn = MaterialButton(this).apply {
            text = getString(R.string.export_csv)
            setBackgroundColor(ContextCompat.getColor(this@ExportActivity, R.color.teal_primary))
            setTextColor(ContextCompat.getColor(this@ExportActivity, R.color.text_inverse))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
            setOnClickListener { exportData(isPdf = false) }
        }
        content.addView(csvBtn)

        val pdfBtn = MaterialButton(this).apply {
            text = getString(R.string.export_pdf)
            setBackgroundColor(ContextCompat.getColor(this@ExportActivity, R.color.surface_navy_elevated))
            setTextColor(ContextCompat.getColor(this@ExportActivity, R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { exportData(isPdf = true) }
        }
        content.addView(pdfBtn)

        root.addView(content)
        setContentView(root)
    }

    private fun exportData(isPdf: Boolean) {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            try {
                val transactions = app.repository.getAllTransactionsWithDetails().first()
                val currency = app.preferencesManager.currencySymbolFlow.first()
                val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
                val file = if (isPdf) {
                    File(dir, "ExpenseTracker_${System.currentTimeMillis()}.pdf")
                } else {
                    File(dir, "ExpenseTracker_${System.currentTimeMillis()}.csv")
                }

                FileOutputStream(file).use { os ->
                    if (isPdf) {
                        PdfExporter.generatePdfReport(transactions, "All Transactions", os, currency)
                    } else {
                        CsvExporter.exportTransactionsToCsv(transactions, os, currency)
                    }
                }
                Toast.makeText(this@ExportActivity, "Saved to ${file.name}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@ExportActivity, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
