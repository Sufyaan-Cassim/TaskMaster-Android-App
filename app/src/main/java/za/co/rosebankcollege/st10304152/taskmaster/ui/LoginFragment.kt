package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import za.co.rosebankcollege.st10304152.taskmaster.R

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        
        // Configure Google Sign-In
        configureGoogleSignIn()
        
        // Setup Google Sign-In launcher
        setupGoogleSignInLauncher()

        val emailEt = view.findViewById<EditText>(R.id.etLoginEmail)
        val passEt = view.findViewById<EditText>(R.id.etLoginPassword)

        // Login
        view.findViewById<View>(R.id.login_button)?.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_login_to_home)
                    } else {
                        Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Google Sign-In Button
        view.findViewById<View>(R.id.google_button)?.setOnClickListener {
            signInWithGoogle()
        }

        // Navigate to Register
        view.findViewById<View>(R.id.register_link)?.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Forgot password - navigate to forgot password fragment
        view.findViewById<View>(R.id.forgot_password)?.setOnClickListener {
            val email = emailEt.text.toString().trim()
            // Pass email to forgot password fragment if available
            val bundle = Bundle().apply {
                if (email.isNotEmpty()) {
                    putString("email", email)
                }
            }
            findNavController().navigate(R.id.action_login_to_forgot_password, bundle)
        }
    }

    /**
     * Configure Google Sign-In options
     */
    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    /**
     * Setup Google Sign-In launcher for handling the result
     */
    private fun setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Show progress dialog AFTER user selects account
                showProgressDialog("Signing in with Google...")
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
                Toast.makeText(requireContext(), "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Initiate Google Sign-In process
     */
    private fun signInWithGoogle() {
        // Sign out first to force account selection
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    /**
     * Authenticate with Firebase using Google account
     */
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.d("GoogleSignIn", "firebaseAuthWithGoogle:${acct?.id}")
        
        updateProgressDialog("Authenticating with Firebase...")

        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(requireContext(), "Welcome ${user?.displayName}! 🎉", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_login_to_home)
                } else {
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Show progress dialog with custom message
     */
    private fun showProgressDialog(message: String) {
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage(message)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }
    }

    /**
     * Update progress dialog message
     */
    private fun updateProgressDialog(message: String) {
        progressDialog?.setMessage(message)
    }

    /**
     * Hide progress dialog
     */
    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgressDialog()
    }
}
