package com.example.reviews.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView

object TextWatcherHelper {
    fun setupPlaceholderVisibility(editText: EditText, placeholder: TextView) {
        // Initialize visibility at start
        placeholder.visibility = if (editText.text.isNullOrEmpty()) View.VISIBLE else View.GONE

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                placeholder.visibility = if (s.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
