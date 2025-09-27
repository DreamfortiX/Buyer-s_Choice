package com.example.reviews.ui

import android.R.attr.repeatCount
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Build
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.example.reviews.MainActivity
import com.example.reviews.R
import com.example.reviews.databinding.ActivitySplashBinding
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val typingHandler = Handler(Looper.getMainLooper())
    private var typingRunnable: Runnable? = null
    private val loadingMessages = arrayOf(
        "Initializing AI Engine",
        "Analyzing Review Patterns",
        "Loading Sentiment Models",
        "Preparing UI"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
        // Start startup checks in parallel with animations
        startStartupFlow()
        startTypingAnimation()
    }

    private fun setupAnimations() {
        // Initial state
        binding.logo.scaleX = 0.8f
        binding.logo.scaleY = 0.8f
        binding.logo.alpha = 0f
        binding.glowEffect.alpha = 0.3f
        // Ensure loader containers are initially hidden; we'll toggle based on network
        binding.loadingIndicator.visibility = View.GONE
        binding.loadingAnimation.visibility = View.GONE
        binding.dotsLoading.visibility = View.GONE
        binding.statusText.apply {
            alpha = 0.8f
            visibility = View.VISIBLE
        }

        // Continuous animations (start immediately; don't block the intro set)
        val glowPulse = ObjectAnimator.ofFloat(binding.glowEffect, View.ALPHA, 0.3f, 0.7f, 0.3f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        glowPulse.start()

        val floatAnim = ObjectAnimator.ofFloat(binding.logo, View.TRANSLATION_Y, -20f, 20f, -20f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        floatAnim.start()

        // One-shot intro animations
        val animatorSet = AnimatorSet()

        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
            binding.logo,
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f, 1.1f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f, 1.1f)
        ).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            binding.logo,
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1.1f, 1.0f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.1f, 1.0f)
        ).apply {
            duration = 400
            interpolator = OvershootInterpolator()
        }

        val fadeIn = ObjectAnimator.ofFloat(binding.logo, View.ALPHA, 0f, 1f).apply {
            duration = 800
        }

        val rotation = ObjectAnimator.ofFloat(binding.logo, View.ROTATION, 0f, 5f, 0f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }
        animatorSet.playTogether(scaleUp, fadeIn, rotation)
        animatorSet.play(scaleDown).after(scaleUp)

        animatorSet.doOnEnd { startParticleAnimations() }

        animatorSet.start()
    }

    private fun startParticleAnimations() {
        val particle1X = ObjectAnimator.ofFloat(binding.particle1, View.TRANSLATION_X, -100f, 100f, -100f).apply {
            duration = 4000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val particle1Y = ObjectAnimator.ofFloat(binding.particle1, View.TRANSLATION_Y, -100f, 100f, -100f).apply {
            duration = 4000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val particle2X = ObjectAnimator.ofFloat(binding.particle2, View.TRANSLATION_X, 100f, -100f, 100f).apply {
            duration = 5000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val particle2Y = ObjectAnimator.ofFloat(binding.particle2, View.TRANSLATION_Y, 100f, -100f, 100f).apply {
            duration = 5000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val orb1X = ObjectAnimator.ofFloat(binding.backgroundOrb1, View.TRANSLATION_X, -200f, 200f).apply {
            duration = 8000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val orb2Y = ObjectAnimator.ofFloat(binding.backgroundOrb2, View.TRANSLATION_Y, -200f, 200f).apply {
            duration = 8000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        particle1X.start(); particle1Y.start();
        particle2X.start(); particle2Y.start();
        orb1X.start(); orb2Y.start()

        // Orbit particles around the logo after layout is ready
        binding.root.post { startOrbitParticles() }
    }

    private fun startOrbitParticles() {
        if (isFinishing || isDestroyed) return
        // Elliptical orbits with varied radii and phase offsets
        orbitParticle(binding.particle3, radiusX = 90f, radiusY = 60f, durationMs = 5200L, phase = Random.nextFloat() * 360f)
        orbitParticle(binding.particle4, radiusX = 110f, radiusY = 80f, durationMs = 6000L, phase = Random.nextFloat() * 360f)
        orbitParticle(binding.particle5, radiusX = 70f, radiusY = 100f, durationMs = 4800L, phase = Random.nextFloat() * 360f)
    }

    private fun orbitParticle(view: View, radiusX: Float, radiusY: Float, durationMs: Long, phase: Float) {
        if (isFinishing || isDestroyed) return
        val startAngle = Random.nextFloat() * 360f
        val animator = ValueAnimator.ofFloat(startAngle, startAngle + 360f).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener { va ->
                val cx = binding.logo.x + binding.logo.width / 2f
                val cy = binding.logo.y + binding.logo.height / 2f
                val angle = (va.animatedValue as Float + phase) * Math.PI.toFloat() / 180f
                val x = cx + radiusX * cos(angle) - view.width / 2f
                val y = cy + radiusY * sin(angle) - view.height / 2f
                view.x = x
                view.y = y
            }
        }
        animator.start()
    }

    private fun startStartupFlow() {
        // Update UI status
        binding.statusText.text = getString(R.string.loading)

        // Minimal splash display time to avoid instant jump
        val minDisplayMs = 900L
        val startTime = System.currentTimeMillis()

        if (!isNetworkAvailable()) {
            // Offline: show dots pulse
            showDotsLoader()
            binding.statusText.text = getString(R.string.no_internet_continuing_offline)
            // Continue offline after a short delay
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = (minDisplayMs - elapsed).coerceAtLeast(400L)
            Handler(Looper.getMainLooper()).postDelayed({ navigateToMain() }, remaining)
            return
        }

        // Online: show Lottie
        showLottieLoader()
        // Simulate initial data loading if needed
        binding.statusText.text = getString(R.string.loading_app_data)
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMain()
        }, 3000)
    }

    private fun navigateToMain() {
        if (isFinishing || isDestroyed) return
        // Stop typing animation
        typingRunnable?.let { typingHandler.removeCallbacks(it) }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun showLottieLoader() {
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.dotsLoading.visibility = View.GONE
    }

    private fun showDotsLoader() {
        binding.loadingAnimation.visibility = View.GONE
        binding.dotsLoading.visibility = View.VISIBLE
        // Start pulse animations for dots
        startDotPulse(binding.dot1, 0)
        startDotPulse(binding.dot2, 150)
        startDotPulse(binding.dot3, 300)
    }

    private fun startDotPulse(dot: View, delay: Long) {
        val sx = ObjectAnimator.ofFloat(dot, View.SCALE_X, 1f, 1.5f, 1f)
        val sy = ObjectAnimator.ofFloat(dot, View.SCALE_Y, 1f, 1.5f, 1f)
        val a = ObjectAnimator.ofFloat(dot, View.ALPHA, 0.5f, 1f, 0.5f)

        val d = 600L
        // Configure each animator to loop infinitely
        listOf(sx, sy, a).forEach { anim ->
            anim.duration = d
            anim.startDelay = delay
            anim.repeatCount = ValueAnimator.INFINITE
            anim.repeatMode = ValueAnimator.REVERSE
        }

        AnimatorSet().apply {
            playTogether(sx, sy, a)
            start()
        }
    }

    private fun startTypingAnimation() {
        // Rotate through loadingMessages while splash is visible
        var index = 0
        typingRunnable = object : Runnable {
            override fun run() {
                if (isFinishing || isDestroyed) return
                binding.statusText.text = loadingMessages[index % loadingMessages.size]
                index++
                typingHandler.postDelayed(this, 900)
            }
        }
        typingHandler.post(typingRunnable!!)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val ni = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            ni != null && ni.isConnected
        }
    }
}
