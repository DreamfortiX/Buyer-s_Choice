package com.example.reviews

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optional: Android 12+ splashscreen (no-op on older versions)
        installSplashScreen()
        setContentView(R.layout.activity_main)

        // Set up top app bar (MaterialToolbar) with Settings action
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Menu is provided via XML app:menu in activity_main.xml
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                    // TODO: startActivity(Intent(this, SettingsActivity::class.java)) when available
                    true
                }
                else -> false
            }
        }

        // Welcome card animations
        findViewById<CardView>(R.id.welcomeCard)?.let { card ->
            // Scale-in on start
            card.scaleX = 0.8f
            card.scaleY = 0.8f
            card.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(OvershootInterpolator())
                .start()

            // Click bounce animation
            card.setOnClickListener { v ->
                v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(150)
                    .withEndAction {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
            }
        }

        // Home feature cards
        findViewById<CardView>(R.id.featureCard1)?.setOnClickListener {
            startActivity(Intent(this, com.example.reviews.ui.AnalyzeActivity::class.java))
        }

        findViewById<CardView>(R.id.featureCard2)?.setOnClickListener {
            startActivity(Intent(this, com.example.reviews.ui.ComparisonActivity::class.java))
        }

        findViewById<CardView>(R.id.featureCard3)?.setOnClickListener {
            startActivity(Intent(this, com.example.reviews.ui.SummaryActivity::class.java))
        }

        // New: History
        findViewById<CardView>(R.id.featureCard4)?.setOnClickListener {
            startActivity(Intent(this, com.example.reviews.ui.HistoryActivity::class.java))
        }

        // Demo: Navigate to ResultActivity with sample data (long-press Analyze)
        findViewById<CardView>(R.id.featureCard1)?.setOnLongClickListener {
            val intent = Intent(this, com.example.reviews.ui.ResultActivity::class.java).apply {
                putExtra("sentiment", "positive")
                putExtra("confidence", 0.87f)
            }
            startActivity(intent)
            true
        }

        // Run intro animations to reveal cards (they are alpha=0 in XML)
        animateHome()
    }

    private fun animateHome() {
        val interpolator = AccelerateDecelerateInterpolator()
        // Helper to convert dp to px
        fun dp(value: Int): Float = value * resources.displayMetrics.density

        val welcome = findViewById<View>(R.id.welcomeCard)
        val c1 = findViewById<View>(R.id.featureCard1)
        val c2 = findViewById<View>(R.id.featureCard2)
        val c3 = findViewById<View>(R.id.featureCard3)
        val orb = findViewById<View>(R.id.backgroundOrb)

        // If views exist, animate them in sequence
        welcome?.let {
            it.alpha = 0f
            it.translationY = dp(24)
            it.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(interpolator)
                .start()
        }
        c1?.let {
            it.alpha = 0f
            it.translationY = dp(24)
            it.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(100)
                .setDuration(400)
                .setInterpolator(interpolator)
                .start()
        }
        c2?.let {
            it.alpha = 0f
            it.translationY = dp(24)
            it.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(200)
                .setDuration(400)
                .setInterpolator(interpolator)
                .start()
        }
        c3?.let {
            it.alpha = 0f
            it.translationY = dp(24)
            it.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(300)
                .setDuration(400)
                .setInterpolator(interpolator)
                .start()
        }

        // Subtle background orb pulse
        orb?.apply {
            scaleX = 0.95f
            scaleY = 0.95f
            animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(2000)
                .setInterpolator(interpolator)
                .withEndAction {
                    // Reverse pulse forever
                    animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(2000)
                        .setInterpolator(interpolator)
                        .withEndAction { animateHomeOrbLoop(this) }
                        .start()
                }
                .start()
        }
    }

    private fun animateHomeOrbLoop(view: View) {
        val interpolator = AccelerateDecelerateInterpolator()
        view.animate()
            .scaleX(if (view.scaleX < 1f) 1.05f else 0.95f)
            .scaleY(if (view.scaleY < 1f) 1.05f else 0.95f)
            .setDuration(2000)
            .setInterpolator(interpolator)
            .withEndAction { animateHomeOrbLoop(view) }
            .start()
    }
}