package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import za.co.rosebankcollege.st10304152.taskmaster.R

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

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

        // Navigate to Register
        view.findViewById<View>(R.id.register_link)?.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Forgot password
        view.findViewById<View>(R.id.forgot_password)?.setOnClickListener {
            val email = emailEt.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Enter email to reset password", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { resetTask ->
                    if (resetTask.isSuccessful) {
                        Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error: ${resetTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
