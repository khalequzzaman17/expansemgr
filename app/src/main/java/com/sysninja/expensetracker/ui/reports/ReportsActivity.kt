package com.sysninja.expensetracker.ui.reports

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.model.TransactionWithDetails
import com.sysninja.expensetracker.databinding.ActivityReportsBinding
import com.sysninja.expensetracker.domain.MoneyUtils
import com.sysninja.expensetracker.ui.export.ExportActivity
import com.sysninja.expensetracker.ui.views.BarTrendChartView
import com.sysninja.expensetracker.ui.views.DonutChartView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private val calendar = Calendar.getInstance()
    private var currencySymbol: String = "৳"
    private var allTransactions = listOf<TransactionWithDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupMonthNavigator()
        setupListeners()
        observeData()
    }

    private fun setupMonthNavigator() {
        updateMonthDisplay()

        binding.btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            updateReportData()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            updateReportData()
        }
    }

    private fun updateMonthDisplay() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvReportMonth.text = sdf.format(calendar.time)
    }

    private fun setupListeners() {
        binding.btnExportReports.setOnClickListener {
            startActivity(Intent(this, ExportActivity::class.java))
        }
    }

    private fun observeData() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            currencySymbol = app.preferencesManager.currencySymbolFlow.first()
            app.repository.getAllTransactionsWithDetails().collect { txList ->
                allTransactions = txList
                updateReportData()
            }
        }
    }

    private fun updateReportData() {
        val cal = Calendar.getInstance().apply { time = calendar.time }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val endOfMonth = cal.timeInMillis

        val monthTx = allTransactions.filter { it.transaction.timestamp in startOfMonth until endOfMonth }

        var totalIncome = 0L
        var totalExpense = 0L
        val categoryExpenseMap = mutableMapOf<String, Long>()

        monthTx.forEach { item ->
            val tx = item.transaction
            if (tx.type.equals("INCOME", ignoreCase = true)) {
                totalIncome += tx.amountCents
            } else if (tx.type.equals("EXPENSE", ignoreCase = true)) {
                totalExpense += tx.amountCents
                val catName = item.category?.name ?: getString(R.string.cat_other_expense)
                categoryExpenseMap[catName] = (categoryExpenseMap[catName] ?: 0L) + tx.amountCents
            }
        }

        binding.tvTotalIncome.text = MoneyUtils.formatCents(totalIncome, currencySymbol)
        binding.tvTotalExpense.text = MoneyUtils.formatCents(totalExpense, currencySymbol)

        val savingsRate = if (totalIncome > 0) {
            val netSavings = (totalIncome - totalExpense).coerceAtLeast(0)
            ((netSavings.toDouble() / totalIncome.toDouble()) * 100).toInt()
        } else {
            0
        }
        binding.tvSavingsRate.text = "$savingsRate%"

        // Bar Chart (last 4 months)
        val barData = mutableListOf<BarTrendChartView.BarGroup>()
        val barCal = Calendar.getInstance().apply { time = calendar.time }
        barCal.add(Calendar.MONTH, -3)

        val monthLabelFormat = SimpleDateFormat("MMM", Locale.getDefault())
        for (i in 0..3) {
            barCal.set(Calendar.DAY_OF_MONTH, 1)
            barCal.set(Calendar.HOUR_OF_DAY, 0)
            barCal.set(Calendar.MINUTE, 0)
            barCal.set(Calendar.SECOND, 0)
            val mStart = barCal.timeInMillis
            barCal.add(Calendar.MONTH, 1)
            val mEnd = barCal.timeInMillis

            val periodTx = allTransactions.filter { it.transaction.timestamp in mStart until mEnd }
            val inc = periodTx.filter { it.transaction.type.equals("INCOME", ignoreCase = true) }.sumOf { it.transaction.amountCents } / 100.0
            val exp = periodTx.filter { it.transaction.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.transaction.amountCents } / 100.0

            barCal.add(Calendar.MONTH, -1)
            val label = monthLabelFormat.format(barCal.time)
            barData.add(BarTrendChartView.BarGroup(label, inc, exp))
            barCal.add(Calendar.MONTH, 1)
        }
        binding.chartBarTrend.setData(barData)

        // Donut Chart
        val palette = listOf(
            Color.parseColor("#46EFCF"),
            Color.parseColor("#4EDEA3"),
            Color.parseColor("#DFCFFF"),
            Color.parseColor("#FBBF24"),
            Color.parseColor("#F43F5E"),
            Color.parseColor("#38BDF8"),
            Color.parseColor("#A78BFA")
        )

        val slices = mutableListOf<DonutChartView.Slice>()
        binding.llCategoryLegend.removeAllViews()

        var colorIdx = 0
        val sortedCategories = categoryExpenseMap.entries.sortedByDescending { it.value }

        sortedCategories.forEach { (catName, amtCents) ->
            val color = palette[colorIdx % palette.size]
            colorIdx++

            val amtMajor = amtCents / 100.0
            slices.add(DonutChartView.Slice(catName, amtMajor, color))

            // Add legend item
            val legendRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(4), 0, dp(4))
            }

            val dot = android.view.View(this).apply {
                val d = android.graphics.drawable.GradientDrawable()
                d.shape = android.graphics.drawable.GradientDrawable.OVAL
                d.setColor(color)
                background = d
            }
            legendRow.addView(dot, LinearLayout.LayoutParams(dp(10), dp(10)))

            val nameTv = TextView(this).apply {
                text = catName
                setTextColor(ContextCompat.getColor(this@ReportsActivity, R.color.text_primary))
                textSize = 12f
                setPadding(dp(8), 0, 0, 0)
            }
            legendRow.addView(nameTv, LinearLayout.LayoutParams(0, -2, 1f))

            val amtTv = TextView(this).apply {
                text = MoneyUtils.formatCents(amtCents, currencySymbol)
                setTextColor(ContextCompat.getColor(this@ReportsActivity, R.color.text_muted))
                textSize = 12f
            }
            legendRow.addView(amtTv)

            binding.llCategoryLegend.addView(legendRow)
        }

        binding.chartDonut.setData(
            slices,
            MoneyUtils.formatCents(totalExpense, currencySymbol),
            getString(R.string.total_expense)
        )
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
