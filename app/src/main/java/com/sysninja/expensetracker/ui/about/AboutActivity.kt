package com.sysninja.expensetracker.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sysninja.expensetracker.BuildConfig
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupAppDetails()
        setupClickListeners()
    }

    private fun setupAppDetails() {
        binding.tvAppVersion.text = getString(
            R.string.app_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
    }

    private fun setupClickListeners() {
        // Quick share on topbar
        binding.btnQuickShare.setOnClickListener {
            shareApp()
        }

        // Bottom big share button
        binding.btnBottomShare.setOnClickListener {
            shareApp()
        }

        // Developer action cards
        binding.btnWebsiteCard.setOnClickListener {
            openUrl(getString(R.string.website_url))
        }

        binding.btnEmailCard.setOnClickListener {
            sendEmail(getString(R.string.email_raw))
        }

        binding.btnPhoneCard.setOnClickListener {
            callPhone(getString(R.string.phone_raw))
        }

        binding.btnShareAppCard.setOnClickListener {
            shareApp()
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (_: Exception) {
            // Browser not installed or URL malformed
        }
    }

    private fun sendEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Expense Tracker Feedback & Inquiry")
            }
            startActivity(intent)
        } catch (_: Exception) {
            // Email app not available
        }
    }

    private fun callPhone(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        } catch (_: Exception) {
            // Dialer not available
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        } catch (_: Exception) {
            // Share chooser error
        }
    }
}
