package com.sysninja.expensetracker.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Slice(
        val label: String,
        val value: Double,
        val color: Int
    )

    private val slices = mutableListOf<Slice>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F8FAFC")
        textAlign = Paint.Align.CENTER
        textSize = 36f
        isFakeBoldText = true
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8")
        textAlign = Paint.Align.CENTER
        textSize = 24f
    }

    private val oval = RectF()
    private var centerLabel: String = ""
    private var centerSubLabel: String = "Total"

    fun setData(data: List<Slice>, totalLabel: String, subLabel: String = "Total") {
        slices.clear()
        slices.addAll(data)
        centerLabel = totalLabel
        centerSubLabel = subLabel
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val total = slices.sumOf { it.value }
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - 20f
        val strokeW = radius * 0.28f

        paint.strokeWidth = strokeW
        val halfStroke = strokeW / 2f
        oval.set(cx - radius + halfStroke, cy - radius + halfStroke, cx + radius - halfStroke, cy + radius - halfStroke)

        if (total <= 0.0 || slices.isEmpty()) {
            paint.color = Color.parseColor("#1E293B")
            canvas.drawArc(oval, 0f, 360f, false, paint)
            canvas.drawText("No Data", cx, cy + 10f, subTextPaint)
            return
        }

        var startAngle = -90f
        for (slice in slices) {
            val sweepAngle = ((slice.value / total) * 360f).toFloat()
            if (sweepAngle > 0.5f) {
                paint.color = slice.color
                canvas.drawArc(oval, startAngle, sweepAngle, false, paint)
                startAngle += sweepAngle
            }
        }

        // Draw center text
        if (centerLabel.isNotBlank()) {
            canvas.drawText(centerSubLabel, cx, cy - 12f, subTextPaint)
            canvas.drawText(centerLabel, cx, cy + 24f, textPaint)
        }
    }
}
