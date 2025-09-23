package com.example.reviews.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.example.reviews.MainActivity
import com.example.reviews.R
import com.example.reviews.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
    }

    private fun setupAnimations() {
        // Initial state
        binding.logo.scaleX = 0.8f
        binding.logo.scaleY = 0.8f
        binding.logo.alpha = 0f
        binding.glowEffect.alpha = 0.3f

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

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 1800)
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
    }
}
