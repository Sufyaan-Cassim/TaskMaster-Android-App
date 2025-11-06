package za.co.rosebankcollege.st10304152.taskmaster

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Handler
import android.os.Looper
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import za.co.rosebankcollege.st10304152.taskmaster.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
            // Check if we should start directly with login (coming from onboarding)
            val isFromOnboarding = intent.getBooleanExtra("navigate_to_login", false)
            
            if (isFromOnboarding) {
                // Start directly with login fragment, bypassing splash
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                        if (navHostFragment != null) {
                            val navController = navHostFragment.navController
                            // Navigate directly to login, bypassing splash
                            navController.navigate(R.id.loginFragment)
                        }
                    } catch (e: Exception) {
                        // Navigation failed, let SplashFragment handle it
                    }
                }, 0) // No delay - immediate navigation
            }
    }
}