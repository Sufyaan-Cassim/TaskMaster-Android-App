package za.co.rosebankcollege.st10304152.taskmaster.data

import java.io.Serializable

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val priority: String = "medium",
    val taskId: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
) : Serializable

enum class NotificationType {
    TASK_DUE,
    TASK_OVERDUE,
    TASK_COMPLETED,
    TASK_CREATED,
    REMINDER,
    INFO
}
