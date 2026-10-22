package com.sysninja.expensetracker.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sysninja.expensetracker.BuildConfig
import com.sysninja.expensetracker.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@AboutActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.about_title)
            setTitleTextColor(ContextCompat.getColor(this@AboutActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@AboutActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@AboutActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val titleTv = TextView(this).apply {
            text = getString(R.string.app_name)
            setTextColor(ContextCompat.getColor(this@AboutActivity, R.color.text_primary))
            textSize = 22f
            paint.isFakeBoldText = true
            gravity = Gravity.CENTER
        }
        content.addView(titleTv)

        val versionTv = TextView(this).apply {
            text = getString(R.string.app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
            setTextColor(ContextCompat.getColor(this@AboutActivity, R.color.teal_light))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 32)
        }
        content.addView(versionTv)

        val devTv = TextView(this).apply {
            text = getString(R.string.developer_name)
            setTextColor(ContextCompat.getColor(this@AboutActivity, R.color.text_primary))
            textSize = 16f
            paint.isFakeBoldText = true
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 8)
        }
        content.addView(devTv)

        val phoneTv = TextView(this).apply {
            text = getString(R.string.developer_phone)
            setTextColor(ContextCompat.getColor(this@AboutActivity, R.color.text_secondary))
            textSize = 14f
            setPadding(0, 8, 0, 8)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${getString(R.string.phone_raw)}"))
                startActivity(intent)
            }
        }
        content.addView(phoneTv)

        val emailTv = TextView(this).apply {
            text = getString(R.string.developer_email)
            setTextColor(ContextCompat.getColor(this@AboutActivity, R.color.text_secondary))
            textSize = 14f
            setPadding(0, 8, 0, 8)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${getString(R.string.email_raw)}"))
                startActivity(intent)
            }
        }
        content.addView(emailTv)

        val websiteTv = TextView(this).apply {
            text = getString(R.string.developer_website)
            setTextColor(ContextCompat.getColor(this@AboutActivity, R.color.teal_primary))
            textSize = 14f
            setPadding(0, 8, 0, 24)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url)))
                startActivity(intent)
            }
        }
        content.addView(websiteTv)

        val shareBtn = MaterialButton(this).apply {
            text = getString(R.string.share_app)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@AboutActivity, R.color.text_inverse))
            setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
            }
        }
        content.addView(shareBtn)

        root.addView(content)
        setContentView(root)
    }
}
