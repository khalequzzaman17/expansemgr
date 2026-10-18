package com.expensetracker.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class BarTrendChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class BarGroup(
        val label: String,
        val incomeValue: Double,
        val expenseValue: Double
    )

    private val groups = mutableListOf<BarGroup>()

    private val incomePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        style = Paint.Style.FILL
    }

    private val expensePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF4444")
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E293B")
        strokeWidth = 1f
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8")
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    private val cornerRadius = 6f
    private val rect = RectF()

    fun setData(data: List<BarGroup>) {
        groups.clear()
        groups.addAll(data)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (groups.isEmpty()) {
            canvas.drawText("No Chart Data", width / 2f, height / 2f, labelPaint)
            return
        }

        val maxVal = groups.maxOfOrNull { max(it.incomeValue, it.expenseValue) } ?: 0.0
        val effectiveMax = if (maxVal <= 0.0) 100.0 else maxVal * 1.15

        val bottomMargin = 40f
        val topMargin = 20f
        val chartHeight = height - bottomMargin - topMargin
        val count = groups.size
        val groupWidth = width.toFloat() / count
        val barWidth = (groupWidth * 0.32f).coerceAtMost(32f)
        val barSpacing = 4f

        // Draw horizontal grid lines
        for (i in 0..3) {
            val y = topMargin + (chartHeight * (i / 3f))
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }

        for (i in 0 until count) {
            val group = groups[i]
            val centerX = (i * groupWidth) + (groupWidth / 2f)

            // Income bar
            val incomeH = ((group.incomeValue / effectiveMax) * chartHeight).toFloat()
            val incomeLeft = centerX - barWidth - (barSpacing / 2f)
            val incomeTop = topMargin + (chartHeight - incomeH)
            val incomeBottom = topMargin + chartHeight
            rect.set(incomeLeft, incomeTop, incomeLeft + barWidth, incomeBottom)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, incomePaint)

            // Expense bar
            val expenseH = ((group.expenseValue / effectiveMax) * chartHeight).toFloat()
            val expenseLeft = centerX + (barSpacing / 2f)
            val expenseTop = topMargin + (chartHeight - expenseH)
            val expenseBottom = topMargin + chartHeight
            rect.set(expenseLeft, expenseTop, expenseLeft + barWidth, expenseBottom)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, expensePaint)

            // Label
            canvas.drawText(group.label, centerX, height - 10f, labelPaint)
        }
    }
}
