package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import android.view.View
import za.co.rosebankcollege.st10304152.taskmaster.MainActivity
import za.co.rosebankcollege.st10304152.taskmaster.R

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var pageIndicators: View
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View
    private lateinit var indicator4: View
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: MaterialButton
    private lateinit var btnGetStarted: MaterialButton
    
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        
        fun isOnboardingCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        }
        
        fun markOnboardingCompleted(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        initViews()
        setupViewPager()
        setupListeners()
    }
    
    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        pageIndicators = findViewById(R.id.pageIndicators)
        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)
        indicator3 = findViewById(R.id.indicator3)
        indicator4 = findViewById(R.id.indicator4)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        btnGetStarted = findViewById(R.id.btnGetStarted)
    }
    
    private fun setupViewPager() {
        val adapter = OnboardingPagerAdapter(this)
        viewPager.adapter = adapter
        
        // Custom page change listener
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePageIndicators(position)
                updateNavigationButtons(position)
            }
        })
        
        // Initialize navigation buttons and indicators
        updatePageIndicators(0)
        updateNavigationButtons(0)
    }
    
    private fun updatePageIndicators(position: Int) {
        // Reset all indicators
        indicator1.setBackgroundResource(R.drawable.page_indicator_unselected)
        indicator2.setBackgroundResource(R.drawable.page_indicator_unselected)
        indicator3.setBackgroundResource(R.drawable.page_indicator_unselected)
        indicator4.setBackgroundResource(R.drawable.page_indicator_unselected)
        
        // Set selected indicator
        when (position) {
            0 -> indicator1.setBackgroundResource(R.drawable.page_indicator_selected)
            1 -> indicator2.setBackgroundResource(R.drawable.page_indicator_selected)
            2 -> indicator3.setBackgroundResource(R.drawable.page_indicator_selected)
            3 -> indicator4.setBackgroundResource(R.drawable.page_indicator_selected)
        }
    }
    
    private fun setupListeners() {
        btnNext.setOnClickListener {
            android.util.Log.d("OnboardingActivity", "Next button clicked - current item: ${viewPager.currentItem}")
            if (viewPager.currentItem < 3) {
                viewPager.currentItem = viewPager.currentItem + 1
                android.util.Log.d("OnboardingActivity", "Moved to next page: ${viewPager.currentItem}")
            }
        }
        
        btnSkip.setOnClickListener {
            android.util.Log.d("OnboardingActivity", "Skip button clicked")
            markOnboardingCompleted(this)
            navigateToLogin()
        }
        
        btnGetStarted.setOnClickListener {
            android.util.Log.d("OnboardingActivity", "Get Started button clicked")
            markOnboardingCompleted(this)
            navigateToLogin()
        }
    }
    
    private fun updateNavigationButtons(position: Int) {
        when (position) {
            0, 1, 2 -> {
                btnNext.visibility = android.view.View.VISIBLE
                btnSkip.visibility = android.view.View.VISIBLE
                btnGetStarted.visibility = android.view.View.GONE
            }
            3 -> {
                btnNext.visibility = android.view.View.GONE
                btnSkip.visibility = android.view.View.GONE
                btnGetStarted.visibility = android.view.View.VISIBLE
            }
        }
    }
    
    private fun navigateToLogin() {
        try {
            // Navigate to MainActivity which will show the login fragment
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("navigate_to_login", true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            // Fallback: just finish this activity
            finish()
        }
    }
    
    private inner class OnboardingPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 4
        
        override fun createFragment(position: Int): Fragment {
            return OnboardingFragment.newInstance(position)
        }
    }
}
