package za.co.rosebankcollege.st10304152.taskmaster.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import za.co.rosebankcollege.st10304152.taskmaster.MainActivity
import za.co.rosebankcollege.st10304152.taskmaster.R

/**
 * Manager for Android system notifications (push notifications)
 */
class PushNotificationManager(private val context: Context) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    companion object {
        private const val CHANNEL_ID_TASKS = "task_notifications"
        private const val CHANNEL_ID_REMINDERS = "task_reminders"
        private const val CHANNEL_ID_GENERAL = "general_notifications"
        
        private const val NOTIFICATION_ID_TASK_CREATED = 1001
        private const val NOTIFICATION_ID_TASK_COMPLETED = 1002
        private const val NOTIFICATION_ID_TASK_DUE = 1003
        private const val NOTIFICATION_ID_TASK_OVERDUE = 1004
        private const val NOTIFICATION_ID_REMINDER = 1005
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create notification channels for different types of notifications
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Task notifications channel
            val taskChannel = NotificationChannel(
                CHANNEL_ID_TASKS,
                "Task Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for task creation and completion"
                enableVibration(true)
                enableLights(true)
            }
            
            // Reminder notifications channel
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task due dates and reminders"
                enableVibration(true)
                enableLights(true)
            }
            
            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General app notifications"
                enableVibration(false)
                enableLights(false)
            }
            
            notificationManager.createNotificationChannels(listOf(taskChannel, reminderChannel, generalChannel))
        }
    }
    
    /**
     * Show notification for task created
     */
    fun showTaskCreatedNotification(taskTitle: String, taskId: String) {
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to_task", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TASK_CREATED,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
            .setSmallIcon(R.drawable.ic_add)
            .setContentTitle("New Task Created")
            .setContentText("Task '$taskTitle' has been added to your list")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID_TASK_CREATED, notification)
        } catch (e: SecurityException) {
            // Permission denied, ignore
        }
    }
    
    /**
     * Show notification for task completed
     */
    fun showTaskCompletedNotification(taskTitle: String, taskId: String) {
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to_task", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TASK_COMPLETED,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle("Task Completed")
            .setContentText("Great job! You've completed '$taskTitle'")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID_TASK_COMPLETED, notification)
        } catch (e: SecurityException) {
            // Permission denied, ignore
        }
    }
    
    /**
     * Show notification for task due soon
     */
    fun showTaskDueSoonNotification(taskTitle: String, taskId: String, dueDate: String) {
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to_task", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TASK_DUE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle("Task Due Soon")
            .setContentText("Task '$taskTitle' is due $dueDate")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID_TASK_DUE, notification)
        } catch (e: SecurityException) {
            // Permission denied, ignore
        }
    }
    
    /**
     * Show notification for task overdue
     */
    fun showTaskOverdueNotification(taskTitle: String, taskId: String) {
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to_task", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TASK_OVERDUE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Task Overdue")
            .setContentText("Task '$taskTitle' is overdue! Please complete it soon.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID_TASK_OVERDUE, notification)
        } catch (e: SecurityException) {
            // Permission denied, ignore
        }
    }
    
    /**
     * Show custom reminder notification
     */
    fun showReminderNotification(taskTitle: String, taskId: String, message: String) {
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to_task", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Task Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
        } catch (e: SecurityException) {
            // Permission denied, ignore
        }
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Cancel specific notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
