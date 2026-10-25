package com.sysninja.expensetracker.ui.backup

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.ActivityBackupRestoreBinding
import com.sysninja.expensetracker.domain.BackupManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupRestoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupRestoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnCreateBackup.setOnClickListener {
            createBackup()
        }

        binding.btnRestoreBackup.setOnClickListener {
            showRestoreChooser()
        }
    }

    private fun createBackup() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            try {
                val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
                val timestamp = System.currentTimeMillis()
                val file = File(dir, "bengal_ledger_backup_$timestamp.json")
                val fos = FileOutputStream(file)
                BackupManager.createBackup(app.database, fos)
                fos.close()
                Toast.makeText(this@BackupRestoreActivity, "${getString(R.string.backup_success)}: ${file.name}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@BackupRestoreActivity, getString(R.string.backup_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRestoreChooser() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
        val backupFiles = dir.listFiles { _, name -> name.endsWith(".json") && name.startsWith("bengal_ledger_backup") }
            ?.sortedByDescending { it.lastModified() }

        if (backupFiles.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.no_transactions_found), Toast.LENGTH_SHORT).show()
            return
        }

        val fileNames = backupFiles.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_restore_file))
            .setItems(fileNames) { _, which ->
                confirmRestore(backupFiles[which])
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun confirmRestore(file: File) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_restore_title))
            .setMessage(getString(R.string.confirm_restore_msg, file.name))
            .setPositiveButton(getString(R.string.restore_backup)) { _, _ ->
                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    try {
                        val fis = FileInputStream(file)
                        BackupManager.restoreBackup(app.database, fis)
                        fis.close()
                        Toast.makeText(this@BackupRestoreActivity, getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@BackupRestoreActivity, getString(R.string.restore_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
