package com.sysninja.expensetracker.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.ui.dp
import com.sysninja.expensetracker.ui.stitchSectionLabel
import com.sysninja.expensetracker.ui.stitchToolbar
import com.sysninja.expensetracker.ui.views.BarTrendChartView
import com.sysninja.expensetracker.ui.views.DonutChartView

class ReportsActivity : AppCompatActivity() {
    private val teal by lazy { ContextCompat.getColor(this, R.color.teal_primary) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.bg_navy_dark))
        }
        root.addView(stitchToolbar("Expense Tracker", "হিসাব খাতা • Reports", ::finish))

        val scroll = android.widget.ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(28))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        content.addView(stitchSectionLabel("আর্থিক রিপোর্ট / Analytics", "আপনার খরচ, আয় এবং সঞ্চয়ের বিশ্লেষণ"))
        content.addView(periodCard())
        content.addView(metricRow())
        content.addView(chartCard("মাসিক আয় বনাম খরচ", "Monthly Cash Flow", barChart()))
        content.addView(chartCard("খরচের খাতভিত্তিক বিশ্লেষণ", "Spending Breakdown", donutChart()))
        content.addView(insightCard())
        content.addView(stitchSectionLabel("সাম্প্রতিক খরচের ধরন", "Recent spending patterns"))
        content.addView(simpleListCard())

        setContentView(root)
    }

    private fun periodCard(): MaterialCardView = card().apply {
        val row = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        row.addView(TextView(context).apply {
            text = "এপ্রিল ২০২৫  •  April 2025"
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            textSize = 14f
        }, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(TextView(context).apply {
            text = "মাসিক"
            setTextColor(teal)
            textSize = 12f
        })
        addView(row)
    }

    private fun metricRow(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(0, dp(12), 0, 0)
        listOf("মোট আয়\n৳ ৮৫,০০০", "মোট ব্যয়\n৳ ৪২,১৫০", "সঞ্চয় হার\n৫০.৪%").forEachIndexed { index, label ->
            val card = card().apply {
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy_elevated))
                val tv = TextView(context).apply {
                    text = label
                    setTextColor(if (index == 1) ContextCompat.getColor(context, R.color.expense_red) else teal)
                    textSize = 13f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }
                addView(tv)
            }
            addView(card, LinearLayout.LayoutParams(0, dp(72), 1f).apply { if (index > 0) marginStart = dp(8) })
        }
    }

    private fun barChart(): BarTrendChartView = BarTrendChartView(this).apply {
        setData(listOf(
            BarTrendChartView.BarGroup("Jan", 62.0, 35.0),
            BarTrendChartView.BarGroup("Feb", 70.0, 42.0),
            BarTrendChartView.BarGroup("Mar", 78.0, 48.0),
            BarTrendChartView.BarGroup("Apr", 85.0, 42.0)
        ))
    }

    private fun donutChart(): DonutChartView = DonutChartView(this).apply {
        setData(listOf(
            DonutChartView.Slice("Groceries", 35.0, teal),
            DonutChartView.Slice("Rent", 28.0, ContextCompat.getColor(context, R.color.savings_purple)),
            DonutChartView.Slice("Dining", 18.0, ContextCompat.getColor(context, R.color.expense_red)),
            DonutChartView.Slice("Transport", 11.0, ContextCompat.getColor(context, R.color.warning_amber)),
            DonutChartView.Slice("Others", 8.0, ContextCompat.getColor(context, R.color.text_muted))
        ), "৳ ৪২.১k", "মোট খরচ")
    }

    private fun chartCard(title: String, subtitle: String, chart: android.view.View): MaterialCardView = card().apply {
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        body.addView(TextView(context).apply {
            text = "$title\n$subtitle"
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        body.addView(chart, LinearLayout.LayoutParams(-1, dp(190)).apply { topMargin = dp(12) })
        addView(body)
    }

    private fun insightCard(): MaterialCardView = card().apply {
        addView(TextView(context).apply {
            text = "💡  Smart Tip\nএই মাসে আপনার ফুড ডেলিভারি খরচ ১৫% কমেছে। একইভাবে চললে আরও ৳ ৩,৫০০ সঞ্চয় হতে পারে।"
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            textSize = 13f
        })
    }

    private fun simpleListCard(): MaterialCardView = card().apply {
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        listOf("Shwapno Supermarket     -৳ ৩,৪৫০", "Tech Solutions Ltd       +৳ ৮৫,০০০", "Pathao Ride                 -৳ ২৮০").forEach { line ->
            body.addView(TextView(context).apply {
                text = line
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                textSize = 13f
                setPadding(0, dp(8), 0, dp(8))
            })
        }
        addView(body)
    }

    private fun card(): MaterialCardView = MaterialCardView(this).apply {
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy_elevated))
        strokeColor = ContextCompat.getColor(context, R.color.stroke_subtle)
        strokeWidth = dp(1)
        radius = dp(18).toFloat()
        setContentPadding(dp(14), dp(14), dp(14), dp(14))
    }
}
