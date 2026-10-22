package com.sysninja.expensetracker.ui.backup

import com.sysninja.expensetracker.ui.dp

import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.domain.BackupManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupRestoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@BackupRestoreActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.backup_restore_title)
            setTitleTextColor(ContextCompat.getColor(this@BackupRestoreActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@BackupRestoreActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@BackupRestoreActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        val backupBtn = MaterialButton(this).apply {
            text = getString(R.string.create_backup)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@BackupRestoreActivity, R.color.text_inverse))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
            setOnClickListener { createBackup() }
        }
        content.addView(backupBtn)

        val restoreBtn = MaterialButton(this).apply {
            text = getString(R.string.restore_backup)
            setBackgroundColor(ContextCompat.getColor(this@BackupRestoreActivity, R.color.surface_navy_elevated))
            setTextColor(ContextCompat.getColor(this@BackupRestoreActivity, R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { restoreBackup() }
        }
        content.addView(restoreBtn)

        root.addView(content)
        setContentView(root)
    }

    private fun createBackup() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            try {
                val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
                val backupFile = File(dir, "backup_expense_tracker.json")
                FileOutputStream(backupFile).use { os ->
                    BackupManager.createBackup(app.database, os)
                }
                Toast.makeText(this@BackupRestoreActivity, getString(R.string.backup_success), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@BackupRestoreActivity, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun restoreBackup() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            try {
                val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
                val backupFile = File(dir, "backup_expense_tracker.json")
                if (!backupFile.exists()) {
                    Toast.makeText(this@BackupRestoreActivity, "No local backup found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                FileInputStream(backupFile).use { `is` ->
                    BackupManager.restoreBackup(app.database, `is`)
                }
                Toast.makeText(this@BackupRestoreActivity, getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@BackupRestoreActivity, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
