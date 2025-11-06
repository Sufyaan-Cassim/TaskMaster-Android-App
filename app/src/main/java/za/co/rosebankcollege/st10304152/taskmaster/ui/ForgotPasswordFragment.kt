package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import za.co.rosebankcollege.st10304152.taskmaster.R

class ForgotPasswordFragment : Fragment() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var sendResetButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var messageContainer: LinearLayout
    private lateinit var successMessage: LinearLayout
    private lateinit var errorMessage: TextView
    private lateinit var backToLoginLink: TextView
    
    companion object {
        private const val TAG = "ForgotPasswordFragment"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        
        // Initialize views
        emailEditText = view.findViewById(R.id.etForgotPasswordEmail)
        sendResetButton = view.findViewById(R.id.send_reset_button)
        progressBar = view.findViewById(R.id.progress_bar)
        messageContainer = view.findViewById(R.id.message_container)
        successMessage = view.findViewById(R.id.success_message)
        errorMessage = view.findViewById(R.id.error_message)
        backToLoginLink = view.findViewById(R.id.back_to_login_link)
        
        // Try to pre-fill email from login fragment if available
        arguments?.getString("email")?.let {
            emailEditText.setText(it)
        }
        
        // Setup click listeners
        sendResetButton.setOnClickListener {
            sendPasswordResetEmail()
        }
        
        backToLoginLink.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun sendPasswordResetEmail() {
        val email = emailEditText.text.toString().trim()
        
        // Validate email
        if (!isValidEmail(email)) {
            showError(getString(R.string.invalid_email_format))
            return
        }
        
        // Hide previous messages and show loading
        hideAllMessages()
        showLoading(true)
        sendResetButton.isEnabled = false
        
        // Send password reset email
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                sendResetButton.isEnabled = true
                
                if (task.isSuccessful) {
                    Log.d(TAG, "Password reset email sent successfully to: $email")
                    // Show success message
                    showSuccess()
                    // Clear email field
                    emailEditText.text?.clear()
                } else {
                    // Log the error for debugging
                    val exception = task.exception
                    Log.e(TAG, "Failed to send password reset email", exception)
                    val errorMsg = exception?.message ?: getString(R.string.failed_to_send_reset_email)
                    Log.d(TAG, "Error message: $errorMsg")
                    
                    // Show error message
                    showError(getErrorMessage(errorMsg))
                }
            }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun showSuccess() {
        messageContainer.visibility = View.VISIBLE
        successMessage.visibility = View.VISIBLE
        errorMessage.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        messageContainer.visibility = View.VISIBLE
        errorMessage.text = message
        errorMessage.visibility = View.VISIBLE
        successMessage.visibility = View.GONE
    }
    
    private fun hideAllMessages() {
        messageContainer.visibility = View.GONE
        successMessage.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    /**
     * Convert Firebase error messages to user-friendly localized messages
     */
    private fun getErrorMessage(firebaseError: String): String {
        return when {
            firebaseError.contains("user-not-found", ignoreCase = true) -> {
                // Firebase doesn't reveal if email exists, but we can show a generic message
                getString(R.string.reset_email_sent_description)
            }
            firebaseError.contains("invalid-email", ignoreCase = true) -> {
                getString(R.string.invalid_email_format)
            }
            firebaseError.contains("too-many-requests", ignoreCase = true) -> {
                getString(R.string.too_many_requests)
            }
            firebaseError.contains("network", ignoreCase = true) -> {
                getString(R.string.network_error)
            }
            else -> {
                getString(R.string.failed_to_send_reset_email)
            }
        }
    }
}

