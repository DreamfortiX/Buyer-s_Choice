package com.example.reviews.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reviews.R
import com.example.reviews.data.network.ProductSentiment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Intent

class ChartsActivity : AppCompatActivity() {
    private var topPick: ProductSentiment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)

        val tvTopPick = findViewById<TextView>(R.id.tvTopPick)
        val topPickCard = findViewById<View>(R.id.topPickCard)
        val barChart = findViewById<BarChart>(R.id.barChart)
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val json = intent.getStringExtra("results_json")
        val results: List<ProductSentiment> = try {
            if (json.isNullOrBlank()) emptyList() else Gson().fromJson(json, object : TypeToken<List<ProductSentiment>>() {}.type)
        } catch (_: Exception) {
            emptyList()
        }

        val data = if (results.isNotEmpty()) results else defaultSampleData()

        renderTopPick(tvTopPick, topPickCard, data)
        topPickCard.setOnClickListener {
            topPick?.let { winner ->
                val intent = Intent(this, SummaryActivity::class.java).apply {
                    putExtra("product_id", winner.productId)
                    putExtra("product_name", winner.productName)
                }
                startActivity(intent)
            }
        }
        renderCharts(barChart, pieChart, data)
    }

    private fun renderTopPick(tv: TextView, card: View, results: List<ProductSentiment>) {
        if (results.isEmpty()) {
            card.visibility = View.GONE
            return
        }
        val winner = results.maxByOrNull { it.positive }
        if (winner != null) {
            topPick = winner
            tv.text = "Top Pick: ${winner.productName} (${winner.positive}% Positive)"
            card.visibility = View.VISIBLE
        } else {
            card.visibility = View.GONE
        }
    }

    private fun defaultSampleData(): List<ProductSentiment> = listOf(
        ProductSentiment("p1", "Echo Dot (5th Gen)", positive = 72, neutral = 18, negative = 10),
        ProductSentiment("p2", "Kindle Paperwhite", positive = 81, neutral = 12, negative = 7),
        ProductSentiment("p3", "Fire TV Stick 4K", positive = 64, neutral = 22, negative = 14)
    )

    private fun renderCharts(barChart: BarChart, pieChart: PieChart, results: List<ProductSentiment>) {
        val labels = results.map { it.productName }
        val entriesPos = ArrayList<BarEntry>()
        val entriesNeu = ArrayList<BarEntry>()
        val entriesNeg = ArrayList<BarEntry>()
        results.forEachIndexed { index, r ->
            entriesPos.add(BarEntry(index.toFloat(), r.positive.toFloat()))
            entriesNeu.add(BarEntry(index.toFloat(), r.neutral.toFloat()))
            entriesNeg.add(BarEntry(index.toFloat(), r.negative.toFloat()))
        }

        val colorPos = ContextCompat.getColor(this, R.color.positive_sentiment)
        val colorNeu = ContextCompat.getColor(this, R.color.neutral_sentiment)
        val colorNeg = ContextCompat.getColor(this, R.color.negative_sentiment)

        val setPos = BarDataSet(entriesPos, "+ Positive").apply { color = colorPos }
        val setNeu = BarDataSet(entriesNeu, "~ Neutral").apply { color = colorNeu }
        val setNeg = BarDataSet(entriesNeg, "- Negative").apply { color = colorNeg }

        val barData = BarData(setPos, setNeu, setNeg)
        val barWidth = 0.25f
        val barSpace = 0.02f
        val groupSpace = 0.26f
        barData.barWidth = barWidth
        barChart.data = barData
        // Brand styling for BarChart
        styleBarChart(barChart)
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setCenterAxisLabels(true)
            setDrawGridLines(false)
            labelRotationAngle = 0f
        }
        barChart.axisLeft.axisMinimum = 0f

        val groupCount = results.size
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = 0f + groupCount * (setPos.entryCount * (barWidth + barSpace) + groupSpace)
        barChart.groupBars(0f, groupSpace, barSpace)
        barChart.animateY(800)
        barChart.invalidate()

        val totalPos = results.sumOf { it.positive }
        val totalNeu = results.sumOf { it.neutral }
        val totalNeg = results.sumOf { it.negative }
        val pieEntries = ArrayList<PieEntry>().apply {
            if (totalPos > 0) add(PieEntry(totalPos.toFloat(), "+ Positive"))
            if (totalNeu > 0) add(PieEntry(totalNeu.toFloat(), "~ Neutral"))
            if (totalNeg > 0) add(PieEntry(totalNeg.toFloat(), "- Negative"))
        }
        val pieDataSet = PieDataSet(pieEntries, "Overall").apply {
            colors = listOf(colorPos, colorNeu, colorNeg)
            sliceSpace = 2f
            valueTextColor = ContextCompat.getColor(this@ChartsActivity, android.R.color.white)
            valueTextSize = 12f
        }
        pieChart.data = PieData(pieDataSet)
        // Brand styling for PieChart
        stylePieChart(pieChart)
        pieChart.setUsePercentValues(true)
        pieChart.centerText = getString(R.string.app_name)
        pieChart.setCenterTextColor(ContextCompat.getColor(this, android.R.color.white))
        pieChart.setCenterTextSize(14f)
        pieChart.animateY(800)
        pieChart.invalidate()
    }

    private fun styleBarChart(chart: BarChart) {
        val white = ContextCompat.getColor(this, android.R.color.white)
        val grid = ContextCompat.getColor(this, R.color.header_card_background)
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setExtraOffsets(8f, 8f, 8f, 8f)
        chart.axisRight.isEnabled = false
        chart.axisLeft.apply {
            textColor = white
            axisLineColor = grid
            gridColor = grid
            granularity = 10f
        }
        chart.xAxis.apply {
            textColor = white
            axisLineColor = grid
        }
        chart.legend.apply {
            isEnabled = true
            textColor = white
            textSize = 12f
            verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
        }
    }

    private fun stylePieChart(chart: PieChart) {
        val white = ContextCompat.getColor(this, android.R.color.white)
        chart.description.isEnabled = false
        chart.setDrawEntryLabels(false)
        chart.legend.apply {
            isEnabled = true
            textColor = white
            textSize = 12f
            verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
        }
        chart.holeRadius = 58f
        chart.transparentCircleRadius = 61f
    }
}
