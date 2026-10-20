package com.example.acadex

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.acadex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_reminders -> RemindersFragment()
                R.id.nav_calendar -> CalendarFragment()
                R.id.nav_map -> MapFragment()
                R.id.nav_planner -> PlannerFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
            true
        }
        if (savedInstanceState == null) binding.bottomNav.selectedItemId = R.id.nav_home

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNav.selectedItemId != R.id.nav_home) goHome() else finish()
            }
        })
    }

    fun navigateTo(menuId: Int) { binding.bottomNav.selectedItemId = menuId }
    fun goHome() = navigateTo(R.id.nav_home)
}
