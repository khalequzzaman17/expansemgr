package com.sysninja.expensetracker.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.sysninja.expensetracker.R

/** Shared native primitives matching the Bengal Ledger/Stitch visual system. */
fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

fun Context.stitchToolbar(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null): View {
    val wrap = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy))
    }
    val toolbar = MaterialToolbar(this).apply {
        this.title = title
        setTitleTextColor(ContextCompat.getColor(context, R.color.text_primary))
        setBackgroundColor(Color.TRANSPARENT)
        minimumHeight = dp(64)
        if (onBack != null) {
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(context, R.color.text_primary))
            setNavigationOnClickListener { onBack() }
        }
    }
    wrap.addView(toolbar, LinearLayout.LayoutParams(-1, dp(64)))
    if (subtitle != null) {
        wrap.addView(TextView(this).apply {
            text = subtitle
            setTextColor(ContextCompat.getColor(context, R.color.text_muted))
            textSize = 12f
            setPadding(dp(16), 0, dp(16), dp(12))
        })
    }
    return wrap
}

fun Context.stitchSectionLabel(title: String, subtitle: String? = null): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(dp(4), dp(8), dp(4), dp(8))
    addView(TextView(context).apply {
        text = title
        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        textSize = 16f
        setTypeface(typeface, Typeface.BOLD)
    })
    if (subtitle != null) addView(TextView(context).apply {
        text = subtitle
        setTextColor(ContextCompat.getColor(context, R.color.text_muted))
        textSize = 11f
        setPadding(0, dp(2), 0, 0)
    })
}

fun Context.stitchCard(content: View, padding: Int = 16): MaterialCardView = MaterialCardView(this).apply {
    setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy_elevated))
    strokeColor = ContextCompat.getColor(context, R.color.stroke_subtle)
    strokeWidth = dp(1)
    radius = dp(20).toFloat()
    val inside = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
        addView(content)
    }
    addView(inside)
}
