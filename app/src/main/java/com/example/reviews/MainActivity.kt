package com.example.reviews

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.reviews.databinding.ActivityMainBinding
import com.example.reviews.ui.fragments.HomeFragment
import com.example.reviews.ui.fragments.SearchFragment
import android.view.WindowManager
import android.os.Build
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Set up fullscreen/edge-to-edge
        setupEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // Load home fragment by default
        loadFragment(homeFragment)
    }

    private fun setupEdgeToEdge() {
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (currentFragment != homeFragment) {
                        loadFragment(homeFragment)
                    }
                    true
                }
                R.id.nav_search -> {
                    if (currentFragment != searchFragment) {
                        loadFragment(searchFragment)
                    }
                    true
                }
                R.id.nav_recommendations -> {
                    // Handle recommendations click
                    // For example, navigate to recommendations fragment
                    // Or show a toast
                    true
                }
                R.id.nav_profile -> {
                    // Handle profile click
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        currentFragment = fragment
    }
}