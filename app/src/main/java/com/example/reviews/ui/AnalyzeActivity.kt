package com.example.reviews.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.reviews.R
import com.example.reviews.data.SentimentResult
import com.example.reviews.data.analyzeSentiment
import com.example.reviews.databinding.ActivityAnalyzeBinding
import com.example.reviews.utils.TextWatcherHelper

class AnalyzeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalyzeBinding
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
        setupViews()
        setupClickListeners()
    }

    private fun setupAnimations() {
        val headerAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.headerCard,
            android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 30f, 0f)
        ).apply {
            duration = 600
            interpolator = OvershootInterpolator()
        }

        val inputAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.inputCard,
            android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 40f, 0f)
        ).apply {
            duration = 500
            startDelay = 200
            interpolator = AccelerateDecelerateInterpolator()
        }

        val tipsAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.tipsCard,
            android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 30f, 0f)
        ).apply {
            duration = 400
            startDelay = 400
            interpolator = AccelerateDecelerateInterpolator()
        }

        AnimatorSet().apply {
            playTogether(headerAnim, inputAnim, tipsAnim)
            start()
        }
    }

    private fun setupViews() {
        // Placeholder visibility
        TextWatcherHelper.setupPlaceholderVisibility(binding.reviewEditText, binding.placeholderText)

        // Character count and hide error on typing
        binding.reviewEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCharacterCount(s?.length ?: 0)
                hideErrorMessage()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Focus background change
        binding.reviewEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            updateTextFieldBackground(hasFocus)
        }
    }

    private fun setupClickListeners() {
        binding.analyzeButton.setOnClickListener {
            val reviewText = binding.reviewEditText.text.toString().trim()
            when {
                reviewText.isEmpty() -> showErrorMessage("Please enter a review to analyze")
                reviewText.length < 10 -> showErrorMessage("Review should be at least 10 characters long")
                else -> analyzeReview(reviewText)
            }
        }
    }

    private fun updateCharacterCount(count: Int) {
        binding.characterCount.text = "$count/1000 characters"
    }

    private fun updateTextFieldBackground(hasFocus: Boolean) {
        val res = if (hasFocus) R.drawable.text_field_background_focused else R.drawable.text_field_background
        binding.textFieldContainer.setBackgroundResource(res)
    }

    private fun showErrorMessage(message: String) {
        binding.errorCard.visibility = View.VISIBLE
        binding.errorText.text = message
        ObjectAnimator.ofPropertyValuesHolder(
            binding.errorCard,
            android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f, 1f)
        ).apply { duration = 300 }.start()
    }

    private fun hideErrorMessage() {
        if (binding.errorCard.visibility == View.VISIBLE) {
            ObjectAnimator.ofPropertyValuesHolder(
                binding.errorCard,
                android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.9f),
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.9f)
            ).apply { duration = 200 }.start()
            binding.errorCard.postDelayed({ binding.errorCard.visibility = View.GONE }, 200)
        }
    }

    private fun analyzeReview(reviewText: String) {
        if (isLoading) return
        isLoading = true
        updateButtonState()

        analyzeSentiment(reviewText) { result: SentimentResult ->
            runOnUiThread {
                isLoading = false
                updateButtonState()
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra("sentiment", result.sentiment)
                    putExtra("confidence", result.confidence)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    private fun updateButtonState() {
        if (isLoading) {
            binding.analyzeButton.isEnabled = false
            binding.buttonProgress.visibility = View.VISIBLE
            binding.buttonText.visibility = View.GONE
            binding.analyzeButton.setBackgroundResource(R.drawable.button_background_loading)
        } else {
            binding.analyzeButton.isEnabled = true
            binding.buttonProgress.visibility = View.GONE
            binding.buttonText.visibility = View.VISIBLE
            binding.analyzeButton.setBackgroundResource(R.drawable.button_background)
        }
    }
}
