package com.sysninja.expensetracker.ui.accounts

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sysninja.expensetracker.ui.addtransaction.AddEditTransactionActivity

class TransferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, AddEditTransactionActivity::class.java).apply {
            putExtra("EXTRA_TYPE", "TRANSFER")
        }
        startActivity(intent)
        finish()
    }
}
