package com.sysninja.expensetracker.ui.export

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.ActivityExportBinding
import com.sysninja.expensetracker.domain.CsvExporter
import com.sysninja.expensetracker.domain.PdfExporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportBinding
    private var isPdfSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupFormatSelection()
        setupListeners()
    }

    private fun setupFormatSelection() {
        binding.cardFormatPdf.setOnClickListener {
            isPdfSelected = true
            updateFormatSelectionUI()
        }

        binding.cardFormatCsv.setOnClickListener {
            isPdfSelected = false
            updateFormatSelectionUI()
        }
    }

    private fun updateFormatSelectionUI() {
        if (isPdfSelected) {
            binding.cardFormatPdf.strokeColor = ContextCompat.getColor(this, R.color.teal_primary)
            binding.cardFormatPdf.strokeWidth = 4
            binding.cardFormatCsv.strokeColor = ContextCompat.getColor(this, R.color.stroke_subtle)
            binding.cardFormatCsv.strokeWidth = 2
            binding.btnExecuteExport.text = getString(R.string.download_pdf_file)
        } else {
            binding.cardFormatCsv.strokeColor = ContextCompat.getColor(this, R.color.teal_primary)
            binding.cardFormatCsv.strokeWidth = 4
            binding.cardFormatPdf.strokeColor = ContextCompat.getColor(this, R.color.stroke_subtle)
            binding.cardFormatPdf.strokeWidth = 2
            binding.btnExecuteExport.text = getString(R.string.download_csv_file)
        }
    }

    private fun setupListeners() {
        binding.btnExecuteExport.setOnClickListener {
            executeExport()
        }
    }

    private fun executeExport() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            try {
                val transactions = app.repository.getAllTransactionsWithDetails().first()
                val currency = app.preferencesManager.currencySymbolFlow.first()

                val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
                val timestamp = System.currentTimeMillis()

                if (isPdfSelected) {
                    val file = File(dir, "bengal_ledger_export_$timestamp.pdf")
                    val fos = FileOutputStream(file)
                    PdfExporter.generatePdfReport(
                        transactions = transactions,
                        periodTitle = getString(R.string.range_all_records),
                        outputStream = fos,
                        currencySymbol = currency
                    )
                    fos.close()
                    Toast.makeText(this@ExportActivity, "${getString(R.string.export_success)}: ${file.name}", Toast.LENGTH_LONG).show()
                } else {
                    val file = File(dir, "bengal_ledger_export_$timestamp.csv")
                    val fos = FileOutputStream(file)
                    CsvExporter.exportTransactionsToCsv(
                        transactions = transactions,
                        outputStream = fos,
                        currencySymbol = currency
                    )
                    fos.close()
                    Toast.makeText(this@ExportActivity, "${getString(R.string.export_success)}: ${file.name}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ExportActivity, getString(R.string.backup_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
