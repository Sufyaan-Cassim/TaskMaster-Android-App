package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
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
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import za.co.rosebankcollege.st10304152.taskmaster.R

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        
        // Configure Google Sign-In
        configureGoogleSignIn()
        
        // Setup Google Sign-In launcher
        setupGoogleSignInLauncher()

        val emailEt = view.findViewById<EditText>(R.id.etRegisterEmail)
        val passEt = view.findViewById<EditText>(R.id.etRegisterPassword)
        val confirmEt = view.findViewById<EditText>(R.id.etRegisterConfirmPassword)
        val termsCb = view.findViewById<MaterialCheckBox>(R.id.terms_checkbox)
        val termsText = view.findViewById<TextView>(R.id.terms_text)

        view.findViewById<View>(R.id.register_button)?.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passEt.text.toString().trim()
            val confirm = confirmEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!termsCb.isChecked) {
                Toast.makeText(requireContext(), "You must agree to the terms", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register user with Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Account created successfully! Please login.", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp() // Go back to login screen
                    } else {
                        Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Google Sign-In Button
        view.findViewById<View>(R.id.google_button)?.setOnClickListener {
            signInWithGoogle()
        }

        // Toggle checkbox when terms text is clicked
        termsText.setOnClickListener {
            termsCb.isChecked = !termsCb.isChecked
        }

        // Go back to login
        view.findViewById<View>(R.id.login_link)?.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
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
                showProgressDialog("Creating account with Google...")
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
        
        updateProgressDialog("Setting up your account...")

        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(requireContext(), "Account created! Welcome ${user?.displayName}! 🎉", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_register_to_home)
                } else {
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    Toast.makeText(requireContext(), "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
