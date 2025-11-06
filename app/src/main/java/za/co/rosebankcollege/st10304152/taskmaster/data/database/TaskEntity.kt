package za.co.rosebankcollege.st10304152.taskmaster.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.io.Serializable

/**
 * Room database entity for Task
 * This represents the local database version of a Task
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    
    @ColumnInfo(name = "due_date")
    val dueDate: String,
    
    @ColumnInfo(name = "priority")
    val priority: String,
    
    @ColumnInfo(name = "due_time")
    val dueTime: String,
    
    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean,
    
    @ColumnInfo(name = "reminder_time")
    val reminderTime: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending" // pending, syncing, synced, failed
) : Serializable

/**
 * Extension function to convert Task to TaskEntity
 */
fun za.co.rosebankcollege.st10304152.taskmaster.data.Task.toEntity(
    lastModified: Long = System.currentTimeMillis(),
    isSynced: Boolean = false,
    syncStatus: String = "pending"
): TaskEntity {
    return TaskEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        isCompleted = this.isCompleted,
        dueDate = this.dueDate,
        priority = this.priority,
        dueTime = this.dueTime,
        reminderEnabled = this.reminderEnabled,
        reminderTime = this.reminderTime,
        createdAt = this.createdAt,
        userId = this.userId,
        lastModified = lastModified,
        isSynced = isSynced,
        syncStatus = syncStatus
    )
}

/**
 * Extension function to convert TaskEntity to Task
 */
fun TaskEntity.toTask(): za.co.rosebankcollege.st10304152.taskmaster.data.Task {
    return za.co.rosebankcollege.st10304152.taskmaster.data.Task(
        id = this.id,
        title = this.title,
        description = this.description,
        isCompleted = this.isCompleted,
        dueDate = this.dueDate,
        priority = this.priority,
        dueTime = this.dueTime,
        reminderEnabled = this.reminderEnabled,
        reminderTime = this.reminderTime,
        createdAt = this.createdAt,
        userId = this.userId
    )
}
