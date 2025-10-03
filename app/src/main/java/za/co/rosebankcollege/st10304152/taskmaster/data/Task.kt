package za.co.rosebankcollege.st10304152.taskmaster.data

import java.io.Serializable

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val priority: String = "Medium",
    val dueTime: String = "9:00 AM",
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "15 minutes before",
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = ""
) : Serializable {
    // No-argument constructor for Firestore deserialization
    constructor() : this(
        id = "",
        title = "",
        description = "",
        isCompleted = false,
        dueDate = "",
        priority = "Medium",
        dueTime = "9:00 AM",
        reminderEnabled = false,
        reminderTime = "15 minutes before",
        createdAt = System.currentTimeMillis(),
        userId = ""
    )
}
