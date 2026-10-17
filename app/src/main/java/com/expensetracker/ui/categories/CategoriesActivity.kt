package com.expensetracker.ui.categories

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class CategoriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@CategoriesActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.categories_title)
            setTitleTextColor(ContextCompat.getColor(this@CategoriesActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@CategoriesActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@CategoriesActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val addBtn = MaterialButton(this).apply {
            text = getString(R.string.add_category)
            setBackgroundColor(ContextCompat.getColor(this@CategoriesActivity, R.color.teal_primary))
            setTextColor(ContextCompat.getColor(this@CategoriesActivity, R.color.text_inverse))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 24, 32, 16)
            }
            setOnClickListener {
                startActivity(Intent(this@CategoriesActivity, AddEditCategoryActivity::class.java))
            }
        }
        root.addView(addBtn)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 32)
        }
        scrollView.addView(listContainer)
        root.addView(scrollView)

        setContentView(root)

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            app.repository.getAllActiveCategories().collect { categories ->
                listContainer.removeAllViews()
                categories.forEach { cat ->
                    val card = LinearLayout(this@CategoriesActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(32, 28, 32, 28)
                        setBackgroundResource(R.drawable.bg_card_rounded)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { bottomMargin = 16 }
                    }

                    val nameTv = TextView(this@CategoriesActivity).apply {
                        text = "${cat.name} (${cat.type})"
                        setTextColor(ContextCompat.getColor(this@CategoriesActivity, R.color.text_primary))
                        textSize = 15f
                        paint.isFakeBoldText = true
                    }
                    card.addView(nameTv)
                    listContainer.addView(card)
                }
            }
        }
    }
}
