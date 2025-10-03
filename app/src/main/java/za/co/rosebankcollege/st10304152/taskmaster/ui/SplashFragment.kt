package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import za.co.rosebankcollege.st10304152.taskmaster.R

class SplashFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fade-in for content container
        view.findViewById<View>(R.id.content_container)?.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(800)
                .start()
        }

        // Breathing animation for loading indicator
        val loadingIndicator = view.findViewById<CircularProgressIndicator>(R.id.loading_indicator)
        val pulseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        loadingIndicator.startAnimation(pulseAnim)

        // Optional: breathing fade for loading text
        val loadingText = view.findViewById<TextView>(R.id.loading_text)
        val fadeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade)
        loadingText.startAnimation(fadeAnim)

        // Check if user is already logged in
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        
        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (currentUser != null) {
                // User is already logged in, go to home
                findNavController().navigate(R.id.action_splash_to_home)
            } else {
                // User not logged in, go to login
                findNavController().navigate(R.id.action_splash_to_login)
            }
        }, 2500) // 2.5 seconds
    }
}
