package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import za.co.rosebankcollege.st10304152.taskmaster.R

class ChangePasswordDialogFragment : DialogFragment() {

    private lateinit var currentPasswordEditText: TextInputEditText
    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var changePasswordButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.FullWidthDialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Make dialog full width
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPasswordEditText = view.findViewById(R.id.current_password)
        newPasswordEditText = view.findViewById(R.id.new_password)
        confirmPasswordEditText = view.findViewById(R.id.confirm_password)
        changePasswordButton = view.findViewById(R.id.change_password_button)
        cancelButton = view.findViewById(R.id.cancel_button)

        setupTextWatchers()
        setupButtons()
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateInputs()
            }
        }

        currentPasswordEditText.addTextChangedListener(textWatcher)
        newPasswordEditText.addTextChangedListener(textWatcher)
        confirmPasswordEditText.addTextChangedListener(textWatcher)
    }

    private fun setupButtons() {
        cancelButton.setOnClickListener {
            dismiss()
        }

        changePasswordButton.setOnClickListener {
            changePassword()
        }
    }

    private fun validateInputs() {
        val currentPassword = currentPasswordEditText.text?.toString()?.trim()
        val newPassword = newPasswordEditText.text?.toString()?.trim()
        val confirmPassword = confirmPasswordEditText.text?.toString()?.trim()

        val isValid = !currentPassword.isNullOrEmpty() &&
                !newPassword.isNullOrEmpty() &&
                !confirmPassword.isNullOrEmpty() &&
                newPassword == confirmPassword &&
                newPassword.length >= 6

        changePasswordButton.isEnabled = isValid
    }

    private fun changePassword() {
        val currentPassword = currentPasswordEditText.text?.toString()?.trim()
        val newPassword = newPasswordEditText.text?.toString()?.trim()

        if (currentPassword.isNullOrEmpty() || newPassword.isNullOrEmpty()) {
            Toast.makeText(context, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(context, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, getString(R.string.user_not_authenticated), Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        changePasswordButton.isEnabled = false
        changePasswordButton.text = getString(R.string.changing)

        // Re-authenticate user with current password
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Update password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            changePasswordButton.isEnabled = true
                            changePasswordButton.text = getString(R.string.change_password)

                            if (updateTask.isSuccessful) {
                                Toast.makeText(context, getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show()
                                dismiss()
                            } else {
                                val error = updateTask.exception?.message ?: getString(R.string.password_change_failed)
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    changePasswordButton.isEnabled = true
                    changePasswordButton.text = getString(R.string.change_password)
                    val error = reauthTask.exception?.message ?: getString(R.string.authentication_failed)
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        fun newInstance(): ChangePasswordDialogFragment {
            return ChangePasswordDialogFragment()
        }
    }
}
