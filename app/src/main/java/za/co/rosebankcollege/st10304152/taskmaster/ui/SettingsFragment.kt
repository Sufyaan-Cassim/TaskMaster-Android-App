package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import za.co.rosebankcollege.st10304152.taskmaster.R
import za.co.rosebankcollege.st10304152.taskmaster.TaskMasterApplication
import java.io.File
import java.util.Locale

class SettingsFragment : Fragment() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        sharedPreferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        
        setupVersionInfo()
        setupClickListeners()
        setupToolbar()
        setupUserProfile()
        setupLanguageSpinner()
        setupSwitches()
        
        // Refresh language spinner after view is created
        refreshLanguageSpinner()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh language spinner when fragment becomes visible
        refreshLanguageSpinner()
    }

    private fun setupVersionInfo() {
        try {
            val packageInfo: PackageInfo = requireContext().packageManager.getPackageInfo(
                requireContext().packageName, 0
            )
            
            // Set app version
            view?.findViewById<TextView>(R.id.app_version)?.text = packageInfo.versionName
            
            // Set build number with timestamp
            val buildDate = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
                .format(java.util.Date(packageInfo.lastUpdateTime))
            view?.findViewById<TextView>(R.id.build_number)?.text = "v${packageInfo.versionCode} • $buildDate"
            
        } catch (e: PackageManager.NameNotFoundException) {
            // Fallback values if package info can't be retrieved
            view?.findViewById<TextView>(R.id.app_version)?.text = "1.0.1"
            view?.findViewById<TextView>(R.id.build_number)?.text = "v1001 • 2024.01.15"
        }
    }

    private fun setupClickListeners() {
        // Profile edit button
        view?.findViewById<View>(R.id.edit_profile_button)?.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_editProfile)
        }

        // Change password
        view?.findViewById<View>(R.id.change_password_layout)?.setOnClickListener {
            val dialog = ChangePasswordDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "ChangePasswordDialog")
        }

        // Privacy policy
        view?.findViewById<View>(R.id.privacy_policy_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_privacy_policy)
        }

        // Terms of service
        view?.findViewById<View>(R.id.terms_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_terms_of_service)
        }

        // Logout button
        view?.findViewById<View>(R.id.logout_button)?.setOnClickListener {
            logout()
        }

        // Language spinner functionality is handled in setupLanguageSpinner()
    }

    private fun setupToolbar() {
        // Setup toolbar navigation icon (back button)
        view?.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)?.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Update the email display with the actual user's email
            view?.findViewById<TextView>(R.id.user_email)?.text = currentUser.email
            // You can also update the name if you have it stored
            val displayName = currentUser.displayName ?: "User"
            view?.findViewById<TextView>(R.id.user_name)?.text = displayName
            
            // Load profile image from local storage
            loadLocalProfileImage()
        }
    }
    
    private fun loadLocalProfileImage() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val profileImageFile = getProfileImageFile(currentUser.uid)
            val profileImageView = view?.findViewById<ImageView>(R.id.profile_image)
            
            if (profileImageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(profileImageFile.absolutePath)
                // Create a circular bitmap
                val circularBitmap = createCircularBitmap(bitmap)
                profileImageView?.setImageBitmap(circularBitmap)
            } else {
                profileImageView?.setImageResource(R.drawable.logo)
            }
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
    
    private fun getProfileImageFile(userId: String): File {
        val context = requireContext()
        val profileImagesDir = File(context.filesDir, "profile_images")
        if (!profileImagesDir.exists()) {
            profileImagesDir.mkdirs()
        }
        return File(profileImagesDir, "${userId}_profile.jpg")
    }
    
    private fun setupLanguageSpinner() {
        val languageSpinner = view?.findViewById<AutoCompleteTextView>(R.id.language_spinner)
        
        // Set threshold to 0 so dropdown appears immediately
        languageSpinner?.threshold = 0
        
        // Set up the spinner with all languages
        updateLanguageSpinnerOptions()
        
        languageSpinner?.setOnItemClickListener { _, _, position, _ ->
            val languageCodes = arrayOf("en", "af", "zu")
            val selectedLanguage = languageCodes[position]
            changeLanguage(selectedLanguage)
        }
        
        // Also handle text changes for better UX
        languageSpinner?.setOnClickListener {
            languageSpinner.showDropDown()
        }
    }
    
    private fun updateLanguageSpinnerOptions() {
        val languageSpinner = view?.findViewById<AutoCompleteTextView>(R.id.language_spinner)
        val languages = arrayOf(
            getString(R.string.english),
            getString(R.string.afrikaans), 
            getString(R.string.zulu)
        )
        val languageCodes = arrayOf("en", "af", "zu")
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)
        languageSpinner?.setAdapter(adapter)
        
        // Set current language
        val currentLanguage = sharedPreferences.getString("app_language", "en") ?: "en"
        val currentLanguageIndex = languageCodes.indexOf(currentLanguage)
        if (currentLanguageIndex >= 0) {
            languageSpinner?.setText(languages[currentLanguageIndex], false)
        }
    }
    
    private fun refreshLanguageSpinner() {
        updateLanguageSpinnerOptions()
    }
    
    private fun setupSwitches() {
        // Notifications switch
        val notificationsSwitch = view?.findViewById<MaterialSwitch>(R.id.notifications_switch)
        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        notificationsSwitch?.isChecked = notificationsEnabled
        
        notificationsSwitch?.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()
            Toast.makeText(context, if (isChecked) "Notifications enabled" else "Notifications disabled", Toast.LENGTH_SHORT).show()
        }
        
        // Theme switch
        val themeSwitch = view?.findViewById<MaterialSwitch>(R.id.theme_switch)
        val isDarkTheme = sharedPreferences.getBoolean("dark_theme", false)
        themeSwitch?.isChecked = isDarkTheme
        
        themeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("dark_theme", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            Toast.makeText(context, if (isChecked) "Dark theme enabled" else "Light theme enabled", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun changeLanguage(languageCode: String) {
        // Save language preference
        sharedPreferences.edit().putString("app_language", languageCode).apply()
        
        // Update language in application
        val app = requireActivity().application as TaskMasterApplication
        app.updateLanguage(requireContext(), languageCode)
        
        // Show loading message
        Toast.makeText(context, getString(R.string.changing_language), Toast.LENGTH_SHORT).show()
        
        // Recreate activity to apply language changes
        requireActivity().recreate()
    }
    
    private fun logout() {
        auth.signOut()
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        // Navigate to splash screen, which will redirect to login since user is logged out
        findNavController().navigate(R.id.action_settings_to_splash)
    }
}


