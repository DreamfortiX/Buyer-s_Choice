package com.example.reviews.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.reviews.R
import com.example.reviews.data.SummaryData
import com.example.reviews.data.generateSummary
import com.example.reviews.data.productList

class SummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val spinner = findViewById<Spinner>(R.id.spinner_products)
        val btnGenerate = findViewById<Button>(R.id.btn_generate)
        val progress = findViewById<ProgressBar>(R.id.progress_summary)
        val tvResult = findViewById<TextView>(R.id.tv_summary_result)

        val names = productList.map { it.name }
        val ids = productList.map { it.id }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)

        btnGenerate.setOnClickListener {
            val idx = spinner.selectedItemPosition
            if (idx in ids.indices) {
                btnGenerate.isEnabled = false
                progress.visibility = View.VISIBLE
                tvResult.text = ""
                generateSummary(ids[idx]) { data: SummaryData ->
                    btnGenerate.isEnabled = true
                    progress.visibility = View.GONE
                    tvResult.text = formatSummary(data)
                }
            }
        }
    }

    private fun formatSummary(data: SummaryData): String {
        val sb = StringBuilder()
        sb.append("Overall: ${data.overallSentiment}\n\n")
        sb.append("Pros:\n")
        data.pros.forEach { sb.append("• $it\n") }
        sb.append("\nCons:\n")
        data.cons.forEach { sb.append("• $it\n") }
        sb.append("\nFrequent words: ${data.frequentWords.joinToString(", ")}")
        return sb.toString()
    }
}
