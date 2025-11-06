package za.co.rosebankcollege.st10304152.taskmaster.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import za.co.rosebankcollege.st10304152.taskmaster.data.Notification
import za.co.rosebankcollege.st10304152.taskmaster.data.NotificationType

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val message: String,
    val type: String, // NotificationType as string
    val priority: String,
    val taskId: String?,
    val isRead: Boolean,
    val timestamp: Long,
    val userId: String,
    val isSynced: Boolean = false,
    val syncStatus: String = "pending",
    val lastModified: Long = System.currentTimeMillis()
)

// Extension functions to convert between Entity and Data class
fun NotificationEntity.toNotification(): Notification {
    return Notification(
        id = this.id,
        title = this.title,
        message = this.message,
        type = try {
            NotificationType.valueOf(this.type)
        } catch (e: IllegalArgumentException) {
            NotificationType.INFO
        },
        priority = this.priority,
        taskId = this.taskId,
        isRead = this.isRead,
        timestamp = this.timestamp,
        userId = this.userId
    )
}

fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = this.id,
        title = this.title,
        message = this.message,
        type = this.type.name,
        priority = this.priority,
        taskId = this.taskId,
        isRead = this.isRead,
        timestamp = this.timestamp,
        userId = this.userId,
        isSynced = false,
        syncStatus = "pending",
        lastModified = System.currentTimeMillis()
    )
}
