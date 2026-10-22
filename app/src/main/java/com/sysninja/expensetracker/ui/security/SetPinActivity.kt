package com.sysninja.expensetracker.ui.security

import android.os.Bundle
import android.text.InputType
import android.content.res.ColorStateList
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.domain.SecurityManager
import com.sysninja.expensetracker.ui.dp
import com.sysninja.expensetracker.ui.stitchToolbar
import kotlinx.coroutines.launch

class SetPinActivity : AppCompatActivity() {
    private val primary by lazy { ContextCompat.getColor(this, R.color.teal_primary) }
    private val primaryLight by lazy { ContextCompat.getColor(this, R.color.teal_light) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.bg_navy_dark))
        }
        root.addView(stitchToolbar("Security PIN", "হিসাব খাতা • নিরাপত্তা", ::finish))

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(16), dp(18), dp(16), dp(28))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val icon = TextView(this).apply {
            text = "▣"
            gravity = Gravity.CENTER
            textSize = 30f
            setTextColor(primaryLight)
            setBackgroundResource(R.drawable.bg_chip_selected)
        }
        content.addView(icon, LinearLayout.LayoutParams(dp(72), dp(72)))
        content.addView(TextView(this).apply {
            text = "নিরাপত্তা পিন • Set PIN"
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            textSize = 21f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(12), 0, dp(4))
        })
        content.addView(TextView(this).apply {
            text = "আপনার হিসাব খাতা সুরক্ষিত রাখতে ৪-সংখ্যার পিন দিন"
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            textSize = 13f
            gravity = Gravity.CENTER
        })

        val pinEt = pinField("নতুন PIN  •  Enter PIN")
        val confirmEt = pinField("Confirm PIN  •  PIN নিশ্চিত করুন")
        content.addView(pinEt, LinearLayout.LayoutParams(-1, dp(58)).apply { topMargin = dp(22) })
        content.addView(confirmEt, LinearLayout.LayoutParams(-1, dp(58)).apply { topMargin = dp(10) })

        val saveBtn = MaterialButton(this).apply {
            text = "✓  PIN সংরক্ষণ করুন  •  Save PIN"
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(context, R.color.text_inverse))
            textSize = 14f
            setOnClickListener { savePin(pinEt, confirmEt) }
        }
        content.addView(saveBtn, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(18) })

        val biometric = MaterialCardView(this).apply {
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy_elevated))
            strokeColor = ContextCompat.getColor(context, R.color.stroke_subtle)
            strokeWidth = dp(1)
            radius = dp(16).toFloat()
            val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)) }
            row.addView(TextView(context).apply { text = "⌁"; textSize = 24f; setTextColor(primaryLight) }, LinearLayout.LayoutParams(dp(36), dp(36)))
            row.addView(TextView(context).apply { text = "বায়োমেট্রিক আনলক\nTouch fingerprint sensor"; setTextColor(ContextCompat.getColor(context, R.color.text_primary)); textSize = 12f; setPadding(dp(10), 0, 0, 0) }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(TextView(context).apply { text = "সক্রিয়"; setTextColor(primaryLight); textSize = 11f })
            addView(row)
        }
        content.addView(biometric, LinearLayout.LayoutParams(-1, dp(68)).apply { topMargin = dp(16) })

        val disableBtn = MaterialButton(this).apply {
            text = "Disable PIN Lock"
            setBackgroundResource(R.drawable.bg_card_rounded)
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.surface_navy_elevated))
            setTextColor(ContextCompat.getColor(context, R.color.expense_red))
            setOnClickListener { disablePin() }
        }
        content.addView(disableBtn, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(14) })

        setContentView(root)
    }

    private fun pinField(hintText: String): EditText = EditText(this).apply {
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        gravity = Gravity.CENTER
        textSize = 17f
        letterSpacing = 0.12f
        hint = hintText
        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        setHintTextColor(ContextCompat.getColor(context, R.color.text_muted))
        maxLines = 1
        setBackgroundResource(R.drawable.bg_edit_text)
        setPadding(dp(16), 0, dp(16), 0)
    }

    private fun savePin(pinEt: EditText, confirmEt: EditText) {
        val pin1 = pinEt.text.toString().trim()
        val pin2 = confirmEt.text.toString().trim()
        if (pin1.length < 4) {
            Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
            return
        }
        if (pin1 != pin2) {
            Toast.makeText(this, getString(R.string.pin_mismatch), Toast.LENGTH_SHORT).show()
            return
        }
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val salt = SecurityManager.generateSalt()
            val hash = SecurityManager.hashPin(pin1, salt)
            app.preferencesManager.setPin(hash, salt)
            SecurityManager.isSessionUnlocked = true
            Toast.makeText(this@SetPinActivity, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun disablePin() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            app.preferencesManager.disablePin()
            Toast.makeText(this@SetPinActivity, "PIN lock disabled", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
