package za.co.rosebankcollege.st10304152.taskmaster.utils

import android.util.Log

/**
 * Centralized logging utility for the TaskMaster app
 * Provides consistent logging across all components
 */
object Logger {
    
    private const val TAG_PREFIX = "TaskMaster"
    
    // Component-specific tags for better log filtering
    const val TAG_AUTH = "$TAG_PREFIX.Auth"
    const val TAG_TASK = "$TAG_PREFIX.Task"
    const val TAG_NOTIFICATION = "$TAG_PREFIX.Notification"
    const val TAG_UI = "$TAG_PREFIX.UI"
    const val TAG_FIREBASE = "$TAG_PREFIX.Firebase"
    const val TAG_REPOSITORY = "$TAG_PREFIX.Repository"
    const val TAG_ADAPTER = "$TAG_PREFIX.Adapter"
    const val TAG_FRAGMENT = "$TAG_PREFIX.Fragment"
    const val TAG_NAVIGATION = "$TAG_PREFIX.Navigation"
    const val TAG_SETTINGS = "$TAG_PREFIX.Settings"
    const val TAG_THEME = "$TAG_PREFIX.Theme"
    const val TAG_LANGUAGE = "$TAG_PREFIX.Language"
    
    /**
     * Log debug messages
     * Used for development debugging and flow tracking
     */
    fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }
    
    /**
     * Log info messages
     * Used for important application flow information
     */
    fun info(tag: String, message: String) {
        Log.i(tag, message)
    }
    
    /**
     * Log warning messages
     * Used for non-critical issues that should be noted
     */
    fun warning(tag: String, message: String) {
        Log.w(tag, message)
    }
    
    /**
     * Log error messages
     * Used for errors that don't crash the app but should be investigated
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    /**
     * Log method entry
     * Used to track method execution flow
     */
    fun methodEntry(tag: String, methodName: String, parameters: Map<String, Any?> = emptyMap()) {
        val paramString = if (parameters.isNotEmpty()) {
            parameters.entries.joinToString(", ") { "${it.key}=${it.value}" }
        } else {
            "no parameters"
        }
        debug(tag, "→ Entering $methodName($paramString)")
    }
    
    /**
     * Log method exit
     * Used to track method completion
     */
    fun methodExit(tag: String, methodName: String, result: Any? = null) {
        val resultString = if (result != null) {
            " with result: $result"
        } else {
            ""
        }
        debug(tag, "← Exiting $methodName$resultString")
    }
    
    /**
     * Log user actions
     * Used to track user interactions for analytics
     */
    fun userAction(tag: String, action: String, details: Map<String, Any?> = emptyMap()) {
        val detailsString = if (details.isNotEmpty()) {
            details.entries.joinToString(", ") { "${it.key}=${it.value}" }
        } else {
            ""
        }
        info(tag, "User Action: $action${if (detailsString.isNotEmpty()) " - $detailsString" else ""}")
    }
    
    /**
     * Log Firebase operations
     * Used to track Firebase API calls and responses
     */
    fun firebaseOperation(tag: String, operation: String, success: Boolean, details: String = "") {
        val status = if (success) "SUCCESS" else "FAILED"
        val detailsString = if (details.isNotEmpty()) " - $details" else ""
        if (success) {
            info(tag, "Firebase $operation: $status$detailsString")
        } else {
            error(tag, "Firebase $operation: $status$detailsString")
        }
    }
    
    /**
     * Log data operations
     * Used to track CRUD operations
     */
    fun dataOperation(tag: String, operation: String, entityType: String, entityId: String, success: Boolean) {
        val status = if (success) "SUCCESS" else "FAILED"
        info(tag, "Data $operation: $entityType (ID: $entityId) - $status")
    }
    
    /**
     * Log UI state changes
     * Used to track UI component state changes
     */
    fun uiStateChange(tag: String, component: String, oldState: String, newState: String) {
        debug(tag, "UI State Change: $component - $oldState → $newState")
    }
    
    /**
     * Log performance metrics
     * Used to track operation timing and performance
     */
    fun performance(tag: String, operation: String, duration: Long, unit: String = "ms") {
        info(tag, "Performance: $operation took ${duration}${unit}")
    }
    
    /**
     * Log configuration changes
     * Used to track app configuration updates
     */
    fun configurationChange(tag: String, setting: String, oldValue: Any?, newValue: Any?) {
        info(tag, "Configuration Change: $setting - $oldValue → $newValue")
    }
}
