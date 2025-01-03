package com.example.mixmaster

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var navController: NavController? = null
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle system insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Toolbar
        toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        // Initialize Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
        navController = navHostFragment?.navController

        if (navController == null) {
            Log.e("MainActivity", "NavController is null. Check FragmentContainerView setup.")
            return
        }

        NavigationUI.setupActionBarWithNavController(this, navController!!)

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_bar)
        NavigationUI.setupWithNavController(bottomNavigationView, navController!!)

        // Add destination change listener to manage visibility
        navController!!.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.welcomeFragment, R.id.loginFragment, R.id.registerFragment -> {
                    hideToolbarAndBottomNavigation()
                }
                else -> {
                    showToolbarAndBottomNavigation()
                }
            }
        }
    }

    private fun showToolbarAndBottomNavigation() {
        toolbar.visibility = android.view.View.VISIBLE
        bottomNavigationView.visibility = android.view.View.VISIBLE
    }

    private fun hideToolbarAndBottomNavigation() {
        toolbar.visibility = android.view.View.GONE
        bottomNavigationView.visibility = android.view.View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> navController?.popBackStack()
            else -> navController?.let { NavigationUI.onNavDestinationSelected(item, it) }
        }
        return true
    }
}