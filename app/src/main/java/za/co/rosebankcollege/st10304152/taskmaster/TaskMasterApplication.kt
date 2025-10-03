package za.co.rosebankcollege.st10304152.taskmaster

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class TaskMasterApplication : Application() {
    
    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val LANGUAGE_KEY = "app_language"
    }
    
    override fun onCreate() {
        super.onCreate()
        setLanguageFromPreferences()
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
