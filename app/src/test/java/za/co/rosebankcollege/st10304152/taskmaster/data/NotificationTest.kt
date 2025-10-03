package za.co.rosebankcollege.st10304152.taskmaster.data

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Notification data class
 * Tests notification creation, types, and data integrity
 */
class NotificationTest {

    @Test
    fun `test notification creation with all parameters`() {
        // Arrange
        val id = "test-notification-id"
        val title = "Test Notification"
        val message = "This is a test notification message"
        val type = NotificationType.TASK_DUE
        val priority = "high"
        val timestamp = System.currentTimeMillis()
        val taskId = "test-task-id"
        val isRead = false

        // Act
        val notification = Notification(
            id = id,
            title = title,
            message = message,
            type = type,
            priority = priority,
            timestamp = timestamp,
            taskId = taskId,
            isRead = isRead
        )

        // Assert
        assertEquals(id, notification.id)
        assertEquals(title, notification.title)
        assertEquals(message, notification.message)
        assertEquals(type, notification.type)
        assertEquals(priority, notification.priority)
        assertEquals(timestamp, notification.timestamp)
        assertEquals(taskId, notification.taskId)
        assertEquals(isRead, notification.isRead)
    }

    @Test
    fun `test notification creation with default values`() {
        // Act
        val notification = Notification(
            id = "default-test",
            title = "Default Test",
            message = "Default message",
            type = NotificationType.INFO,
            priority = "medium"
        )

        // Assert
        assertEquals("default-test", notification.id)
        assertEquals("Default Test", notification.title)
        assertEquals("Default message", notification.message)
        assertEquals(NotificationType.INFO, notification.type)
        assertEquals("medium", notification.priority)
        assertTrue(notification.timestamp > 0)
        assertNull(notification.taskId)
        assertEquals(false, notification.isRead)
    }

    @Test
    fun `test notification type enum values`() {
        // Act & Assert
        assertEquals("TASK_DUE", NotificationType.TASK_DUE.name)
        assertEquals("TASK_OVERDUE", NotificationType.TASK_OVERDUE.name)
        assertEquals("TASK_COMPLETED", NotificationType.TASK_COMPLETED.name)
        assertEquals("TASK_CREATED", NotificationType.TASK_CREATED.name)
        assertEquals("REMINDER", NotificationType.REMINDER.name)
        assertEquals("INFO", NotificationType.INFO.name)
    }

    @Test
    fun `test notification priority validation`() {
        // Arrange & Act
        val highPriorityNotification = Notification(
            id = "high-priority",
            title = "High Priority",
            message = "High priority message",
            type = NotificationType.TASK_OVERDUE,
            priority = "high"
        )

        val mediumPriorityNotification = Notification(
            id = "medium-priority",
            title = "Medium Priority",
            message = "Medium priority message",
            type = NotificationType.TASK_DUE,
            priority = "medium"
        )

        val lowPriorityNotification = Notification(
            id = "low-priority",
            title = "Low Priority",
            message = "Low priority message",
            type = NotificationType.INFO,
            priority = "low"
        )

        // Assert
        assertEquals("high", highPriorityNotification.priority)
        assertEquals("medium", mediumPriorityNotification.priority)
        assertEquals("low", lowPriorityNotification.priority)
    }

    @Test
    fun `test notification read status toggle`() {
        // Arrange
        val notification = Notification(
            id = "toggle-test",
            title = "Toggle Test",
            message = "Testing read status toggle",
            type = NotificationType.INFO,
            priority = "medium"
        )

        // Act
        val readNotification = notification.copy(isRead = true)

        // Assert
        assertEquals(false, notification.isRead)
        assertEquals(true, readNotification.isRead)
        assertEquals(notification.id, readNotification.id)
        assertEquals(notification.title, readNotification.title)
        assertEquals(notification.message, readNotification.message)
        assertEquals(notification.type, readNotification.type)
        assertEquals(notification.priority, readNotification.priority)
    }

    @Test
    fun `test notification equality`() {
        // Arrange
        val notification1 = Notification(
            id = "same-id",
            title = "Same Title",
            message = "Same Message",
            type = NotificationType.TASK_COMPLETED,
            priority = "high",
            timestamp = 1000L,
            taskId = "same-task-id",
            isRead = false
        )

        val notification2 = Notification(
            id = "same-id",
            title = "Same Title",
            message = "Same Message",
            type = NotificationType.TASK_COMPLETED,
            priority = "high",
            timestamp = 1000L,
            taskId = "same-task-id",
            isRead = false
        )

        // Act & Assert
        assertEquals(notification1, notification2)
        assertEquals(notification1.hashCode(), notification2.hashCode())
    }

    @Test
    fun `test notification with task association`() {
        // Arrange
        val taskId = "associated-task-id"
        val notification = Notification(
            id = "task-associated",
            title = "Task Due",
            message = "Your task is due soon",
            type = NotificationType.TASK_DUE,
            priority = "high",
            taskId = taskId
        )

        // Assert
        assertEquals(taskId, notification.taskId)
        assertEquals(NotificationType.TASK_DUE, notification.type)
    }

    @Test
    fun `test notification without task association`() {
        // Arrange
        val notification = Notification(
            id = "general-notification",
            title = "General Info",
            message = "This is a general information notification",
            type = NotificationType.INFO,
            priority = "low"
        )

        // Assert
        assertNull(notification.taskId)
        assertEquals(NotificationType.INFO, notification.type)
    }
}
