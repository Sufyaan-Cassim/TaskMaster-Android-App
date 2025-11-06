package za.co.rosebankcollege.st10304152.taskmaster.data

import android.content.Context
import za.co.rosebankcollege.st10304152.taskmaster.data.NotificationType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Service for generating notifications based on task events
 */
class NotificationGenerator(private val context: Context) {
    
    private val notificationRepository = NotificationRepository(context)
    private val pushNotificationManager = PushNotificationManager(context)
    private val sharedPreferences = context.getSharedPreferences("taskmaster_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val REMINDER_HOURS_BEFORE = 24L // 1 day before (as requested)
        private const val OVERDUE_CHECK_HOURS = 1L // Check for overdue every hour
    }
    
    /**
     * Check if notifications are enabled in settings
     */
    private fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }
    
    /**
     * Check if reminders are enabled in settings
     */
    private fun areRemindersEnabled(): Boolean {
        return sharedPreferences.getBoolean("reminders_enabled", true)
    }
    
    /**
     * Generate notification when a task is created
     */
    suspend fun generateTaskCreatedNotification(task: Task): Result<Unit> {
        return try {
            // Check if notifications are enabled
            if (!areNotificationsEnabled()) {
                return Result.success(Unit)
            }
            
            val notification = Notification(
                title = "New Task Created",
                message = "Task '${task.title}' has been added to your list",
                type = NotificationType.TASK_CREATED,
                priority = "medium",
                taskId = task.id,
                isRead = false,
                timestamp = System.currentTimeMillis(),
                userId = task.userId
            )
            
            val result = notificationRepository.addNotification(notification)
            if (result.isSuccess) {
                // Also show push notification
                pushNotificationManager.showTaskCreatedNotification(task.title, task.id)
            }
            result.map { Result.success(Unit) }.getOrElse { Result.failure(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate notification when a task is completed
     */
    suspend fun generateTaskCompletedNotification(task: Task): Result<Unit> {
        return try {
            // Check if notifications are enabled
            if (!areNotificationsEnabled()) {
                return Result.success(Unit)
            }
            
            val notification = Notification(
                title = "Task Completed",
                message = "Great job! You've completed '${task.title}'",
                type = NotificationType.TASK_COMPLETED,
                priority = "medium",
                taskId = task.id,
                isRead = false,
                timestamp = System.currentTimeMillis(),
                userId = task.userId
            )
            
            val result = notificationRepository.addNotification(notification)
            if (result.isSuccess) {
                // Also show push notification
                pushNotificationManager.showTaskCompletedNotification(task.title, task.id)
            }
            result.map { Result.success(Unit) }.getOrElse { Result.failure(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate notification when a task is due soon
     */
    suspend fun generateTaskDueSoonNotification(task: Task): Result<Unit> {
        return try {
            val dueDate = task.dueDate
            if (dueDate.isEmpty()) return Result.success(Unit)
            
            // Check if reminders are enabled
            if (!areRemindersEnabled()) {
                return Result.success(Unit)
            }
            
            val notification = Notification(
                title = "Task Due Soon",
                message = "Task '${task.title}' is due ${formatDueDate(dueDate)}",
                type = NotificationType.TASK_DUE,
                priority = "high",
                taskId = task.id,
                isRead = false,
                timestamp = System.currentTimeMillis(),
                userId = task.userId
            )
            
            val result = notificationRepository.addNotification(notification)
            if (result.isSuccess) {
                // Also show push notification
                pushNotificationManager.showTaskDueSoonNotification(task.title, task.id, formatDueDate(dueDate))
            }
            result.map { Result.success(Unit) }.getOrElse { Result.failure(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate notification when a task is overdue
     */
    suspend fun generateTaskOverdueNotification(task: Task): Result<Unit> {
        return try {
            val notification = Notification(
                title = "Task Overdue",
                message = "Task '${task.title}' is overdue! Please complete it soon.",
                type = NotificationType.TASK_OVERDUE,
                priority = "high",
                taskId = task.id,
                isRead = false,
                timestamp = System.currentTimeMillis(),
                userId = task.userId
            )
            
            val result = notificationRepository.addNotification(notification)
            if (result.isSuccess) {
                // Also show push notification
                pushNotificationManager.showTaskCreatedNotification(task.title, task.id)
            }
            result.map { Result.success(Unit) }.getOrElse { Result.failure(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate custom reminder notification
     */
    suspend fun generateReminderNotification(task: Task, reminderMessage: String): Result<Unit> {
        return try {
            val notification = Notification(
                title = "Task Reminder",
                message = reminderMessage,
                type = NotificationType.REMINDER,
                priority = "medium",
                taskId = task.id,
                isRead = false,
                timestamp = System.currentTimeMillis(),
                userId = task.userId
            )
            
            val result = notificationRepository.addNotification(notification)
            if (result.isSuccess) {
                // Also show push notification
                pushNotificationManager.showTaskCreatedNotification(task.title, task.id)
            }
            result.map { Result.success(Unit) }.getOrElse { Result.failure(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a task needs a due soon notification
     */
    fun shouldNotifyDueSoon(task: Task): Boolean {
        val dueDate = task.dueDate
        if (dueDate.isEmpty()) return false
        
        // For now, we'll use a simple string-based check
        // In a real app, you'd parse the date string and compare properly
        return true // Simplified for now
    }
    
    /**
     * Check if a task is overdue
     */
    fun isTaskOverdue(task: Task): Boolean {
        val dueDate = task.dueDate
        if (dueDate.isEmpty()) return false
        
        // For now, we'll use a simple string-based check
        // In a real app, you'd parse the date string and compare properly
        return false // Simplified for now
    }
    
    /**
     * Get tasks that need due soon notifications
     */
    fun getTasksNeedingDueSoonNotification(tasks: List<Task>): List<Task> {
        return tasks.filter { task ->
            !task.isCompleted && shouldNotifyDueSoon(task)
        }
    }
    
    /**
     * Get tasks that are overdue
     */
    fun getOverdueTasks(tasks: List<Task>): List<Task> {
        return tasks.filter { task ->
            isTaskOverdue(task)
        }
    }
    
    /**
     * Process all tasks and generate appropriate notifications
     * NOTE: This method is disabled to prevent generating notifications for existing tasks
     */
    suspend fun processTasksForNotifications(tasks: List<Task>): Result<Unit> {
        return try {
            // Disabled to prevent generating notifications for existing tasks
            // Only generate notifications for new events (task created, completed, etc.)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Format due date for notification message
     */
    private fun formatDueDate(dueDate: String): String {
        // For now, just return the date string as-is
        // In a real app, you'd parse and format the date properly
        return "on $dueDate"
    }
}
