package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import za.co.rosebankcollege.st10304152.taskmaster.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditProfileFragment : Fragment() {
    
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                selectedImageUri = uri
                updateProfileImage(uri)
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        
        setupToolbar()
        setupProfileImage()
        setupUsernameField()
        setupSaveButton()
    }
    
    private fun setupToolbar() {
        view?.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)?.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupProfileImage() {
        val profileImage = view?.findViewById<ImageView>(R.id.profile_image)
        
        // Load current profile image from local storage
        loadLocalProfileImage(profileImage)
        
        // Set click listener for image selection
        profileImage?.setOnClickListener {
            openImagePicker()
        }
        
        // Add change image text
        view?.findViewById<View>(R.id.change_image_text)?.setOnClickListener {
            openImagePicker()
        }
    }
    
    private fun loadLocalProfileImage(imageView: ImageView?) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val profileImageFile = getProfileImageFile(currentUser.uid)
            if (profileImageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(profileImageFile.absolutePath)
                // Create a circular bitmap
                val circularBitmap = createCircularBitmap(bitmap)
                imageView?.setImageBitmap(circularBitmap)
            } else {
                imageView?.setImageResource(R.drawable.ic_person)
            }
        } else {
            imageView?.setImageResource(R.drawable.ic_person)
        }
    }
    
    private fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        
        val paint = android.graphics.Paint()
        val rect = android.graphics.Rect(0, 0, size, size)
        val rectF = android.graphics.RectF(rect)
        
        paint.isAntiAlias = true
        canvas.drawOval(rectF, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        
        return output
    }
    
    private fun setupUsernameField() {
        val usernameEditText = view?.findViewById<EditText>(R.id.username_edit_text)
        val emailEditText = view?.findViewById<EditText>(R.id.email_edit_text)
        val currentUser = auth.currentUser
        
        // Set current username if available
        currentUser?.displayName?.let { displayName ->
            usernameEditText?.setText(displayName)
        }
        
        // Set current email (read-only)
        currentUser?.email?.let { email ->
            emailEditText?.setText(email)
        }
    }
    
    private fun setupSaveButton() {
        view?.findViewById<View>(R.id.save_profile_button)?.setOnClickListener {
            saveProfile()
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    
    private fun updateProfileImage(uri: Uri) {
        val profileImage = view?.findViewById<ImageView>(R.id.profile_image)
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_person)
            .circleCrop()
            .into(profileImage!!)
    }
    
    private fun saveProfile() {
        val usernameEditText = view?.findViewById<EditText>(R.id.username_edit_text)
        val newUsername = usernameEditText?.text.toString().trim()
        
        if (newUsername.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }
        
        val currentUser = auth.currentUser
        if (currentUser != null) {
            showProgress(true)
            
            if (selectedImageUri != null) {
                // Save image locally first, then update profile
                saveImageLocallyAndUpdateProfile(currentUser, newUsername)
            } else {
                // Just update username without image
                updateUserProfile(currentUser, newUsername)
            }
        }
    }
    
    private fun saveImageLocallyAndUpdateProfile(user: com.google.firebase.auth.FirebaseUser, username: String) {
        try {
            val profileImageFile = getProfileImageFile(user.uid)
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri!!)
            
            // Save bitmap to file
            val outputStream = FileOutputStream(profileImageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // Update user profile
            updateUserProfile(user, username)
            
        } catch (e: IOException) {
            showProgress(false)
            Toast.makeText(requireContext(), "Failed to save image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun updateUserProfile(user: com.google.firebase.auth.FirebaseUser, username: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()
        
        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                showProgress(false)
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun getProfileImageFile(userId: String): File {
        val context = requireContext()
        val profileImagesDir = File(context.filesDir, "profile_images")
        if (!profileImagesDir.exists()) {
            profileImagesDir.mkdirs()
        }
        return File(profileImagesDir, "${userId}_profile.jpg")
    }
    
    private fun showProgress(show: Boolean) {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progress_bar)
        val saveButton = view?.findViewById<View>(R.id.save_profile_button)
        
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE
        saveButton?.isEnabled = !show
    }
}
