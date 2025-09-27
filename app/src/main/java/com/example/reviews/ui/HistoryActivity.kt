package com.example.reviews.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reviews.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reviews.data.db.AppDatabase
import kotlinx.coroutines.launch
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.recyclerview.widget.ItemTouchHelper
import android.content.Intent
import kotlinx.coroutines.flow.collectLatest
import com.google.android.material.snackbar.Snackbar

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Use the MaterialToolbar as the ActionBar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.historyToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.history_title)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Inflate top app bar menu with Settings and Logout
        toolbar.inflateMenu(R.menu.menu_history)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                    // TODO: startActivity(Intent(this, SettingsActivity::class.java)) when implemented
                    true
                }
                R.id.action_logout -> {
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    // TODO: implement real logout flow
                    true
                }
                R.id.action_clear_all -> {
                    val dao = AppDatabase.get(applicationContext).reviewDao()
                    lifecycleScope.launch {
                        dao.clear()
                        Toast.makeText(this@HistoryActivity, "Cleared", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        // Recycler + Paging setup
        val recycler = findViewById<RecyclerView>(R.id.historyRecycler)
        val empty = findViewById<android.widget.TextView>(R.id.emptyText)
        val adapter = HistoryPagingAdapter { entity ->
            val intent = Intent(this, ReviewDetailActivity::class.java).apply {
                putExtra("id", entity.id)
                putExtra("sentiment", entity.sentiment)
                putExtra("confidence", entity.confidence)
                putExtra("text", entity.text)
                putExtra("createdAt", entity.createdAt)
            }
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val dao = AppDatabase.get(applicationContext).reviewDao()
        val pagerFlow = Pager(PagingConfig(pageSize = 20, prefetchDistance = 10)) {
            dao.pagingAll()
        }.flow.cachedIn(lifecycleScope)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagerFlow.collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }

        // Show empty state when adapter has no items
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest {
                    empty.visibility = if (adapter.itemCount == 0) android.view.View.VISIBLE else android.view.View.GONE
                }
            }
        }

        // Swipe to delete
        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val entity = adapter.getEntity(position)
                if (entity != null) {
                    // Delete immediately
                    lifecycleScope.launch { dao.deleteById(entity.id) }

                    // Show Undo snackbar
                    Snackbar.make(recycler, getString(R.string.deleted), Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) {
                            // Re-insert with a new ID (auto-generate). Preserve original createdAt.
                            lifecycleScope.launch {
                                com.example.reviews.data.db.AppDatabase.get(applicationContext)
                                    .reviewDao()
                                    .insert(
                                        com.example.reviews.data.db.ReviewEntity(
                                            id = 0,
                                            text = entity.text,
                                            sentiment = entity.sentiment,
                                            confidence = entity.confidence,
                                            createdAt = entity.createdAt
                                        )
                                    )
                            }
                        }
                        .show()
                }
            }
        })
        touchHelper.attachToRecyclerView(recycler)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

