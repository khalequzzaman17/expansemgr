package com.sysninja.expensetracker.ui.backup

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
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
                Toast.makeText(this@BackupRestoreActivity, "ব্যাকআপ সফল হয়েছে: ${file.name}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@BackupRestoreActivity, "ব্যাকআপ তৈরিতে সমস্যা: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRestoreChooser() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
        val backupFiles = dir.listFiles { _, name -> name.endsWith(".json") && name.startsWith("bengal_ledger_backup") }
            ?.sortedByDescending { it.lastModified() }

        if (backupFiles.isNullOrEmpty()) {
            Toast.makeText(this, "কোন ব্যাকআপ ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
            return
        }

        val fileNames = backupFiles.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("রিস্টোর করার ফাইল নির্বাচন করুন")
            .setItems(fileNames) { _, which ->
                confirmRestore(backupFiles[which])
            }
            .setNegativeButton("বাতিল", null)
            .show()
    }

    private fun confirmRestore(file: File) {
        AlertDialog.Builder(this)
            .setTitle("রিস্টোর নিশ্চিতকরণ")
            .setMessage("নির্বাচিত ফাইল (${file.name}) থেকে ডাটা রিস্টোর করতে চান?")
            .setPositiveButton("রিস্টোর করুন") { _, _ ->
                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    try {
                        val fis = FileInputStream(file)
                        BackupManager.restoreBackup(app.database, fis)
                        fis.close()
                        Toast.makeText(this@BackupRestoreActivity, "ডাটা সফলভাবে রিস্টোর হয়েছে!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@BackupRestoreActivity, "রিস্টোরে ত্রুটি: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("বাতিল", null)
            .show()
    }
}
