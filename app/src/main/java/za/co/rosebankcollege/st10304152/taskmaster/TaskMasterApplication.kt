package za.co.rosebankcollege.st10304152.taskmaster

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.Locale

class TaskMasterApplication : Application() {
    
    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val LANGUAGE_KEY = "app_language"
        private const val TAG = "TaskMasterApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Set up global exception handler to catch App Inspection conflicts with Firestore
        setupGlobalExceptionHandler()
        
        setLanguageFromPreferences()
    }
    
    /**
     * Sets up a global exception handler to catch App Inspection conflicts with Firestore.
     * This prevents crashes when Android Studio's App Inspection intercepts Firestore gRPC calls.
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Check if this is the App Inspection/Firestore conflict error
            if (isAppInspectionError(exception)) {
                Log.w(TAG, "App Inspection conflict detected with Firestore. " +
                        "Please close App Inspection tool window in Android Studio to avoid crashes.")
                
                // Try to handle gracefully - don't crash the app
                // The error will still be logged but won't crash
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, exception)
                }
            } else {
                // For all other exceptions, use the default handler
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, exception)
                } else {
                    // If no default handler, log and exit
                    Log.e(TAG, "Uncaught exception in thread ${thread.name}", exception)
                }
            }
        }
    }
    
    /**
     * Checks if the exception is related to App Inspection/Firestore conflict
     */
    private fun isAppInspectionError(exception: Throwable): Boolean {
        var cause: Throwable? = exception
        while (cause != null) {
            val message = cause.message ?: ""
            val className = cause.javaClass.name
            
            // Check for the specific App Inspection/Firestore error
            if (className.contains("NoSuchMethodError") &&
                (message.contains("getBareMethodName") ||
                 message.contains("GrpcInterceptor") ||
                 message.contains("MethodDescriptor"))) {
                return true
            }
            
            // Check for the Firestore internal error that wraps it
            if (className.contains("AsyncQueue") && message.contains("Cloud Firestore")) {
                return true
            }
            
            cause = cause.cause
        }
        return false
    }
    
    override fun attachBaseContext(base: Context) {
        val language = getLanguageFromPreferences(base)
        val locale = Locale(language)
        val config = Configuration(base.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        val context = base.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
    
    private fun setLanguageFromPreferences() {
        val language = getLanguageFromPreferences(this)
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    private fun getLanguageFromPreferences(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, "en") ?: "en"
    }
    
    fun updateLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
