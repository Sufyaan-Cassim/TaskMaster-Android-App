package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.auth.FirebaseAuth
import za.co.rosebankcollege.st10304152.taskmaster.R

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

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

        // Toggle checkbox when terms text is clicked
        termsText.setOnClickListener {
            termsCb.isChecked = !termsCb.isChecked
        }

        // Go back to login
        view.findViewById<View>(R.id.login_link)?.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }
}
