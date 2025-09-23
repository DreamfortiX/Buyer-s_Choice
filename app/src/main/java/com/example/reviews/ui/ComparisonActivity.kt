package com.example.reviews.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.reviews.R
import com.example.reviews.data.ProductComparison
import com.example.reviews.data.compareProducts
import com.example.reviews.data.productList

class ComparisonActivity : AppCompatActivity() {
    private val selectedIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comparison)

        val container = findViewById<LinearLayout>(R.id.container_products)
        val btnCompare = findViewById<Button>(R.id.btn_compare)
        val progress = findViewById<ProgressBar>(R.id.progress_compare)
        val tvResult = findViewById<TextView>(R.id.tv_compare_result)

        // Dynamically add checkboxes for products
        productList.forEach { product ->
            val cb = CheckBox(this).apply {
                text = product.name
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedIds.add(product.id) else selectedIds.remove(product.id)
                    updateButtonState(btnCompare)
                }
            }
            container.addView(cb)
        }

        btnCompare.setOnClickListener {
            if (selectedIds.size < 2) return@setOnClickListener
            btnCompare.isEnabled = false
            progress.visibility = View.VISIBLE
            tvResult.text = ""

            compareProducts(selectedIds.toList()) { list: List<ProductComparison> ->
                progress.visibility = View.GONE
                btnCompare.isEnabled = true
                tvResult.text = formatComparison(list)
            }
        }

        updateButtonState(btnCompare)
    }

    private fun updateButtonState(button: Button) {
        button.isEnabled = selectedIds.size >= 2
        button.text = "Compare Products (${selectedIds.size} selected)"
    }

    private fun formatComparison(list: List<ProductComparison>): String {
        val winner = list.maxByOrNull { it.positivePercentage }
        val sb = StringBuilder()
        winner?.let {
            sb.append("Recommended: ${it.productName} (${it.positivePercentage}% Positive)\n\n")
        }
        list.forEach { p ->
            sb.append("- ${p.productName}: +${p.positivePercentage}% / ~${p.neutralPercentage}% / -${p.negativePercentage}%\n")
        }
        return sb.toString()
    }
}
