package za.co.rosebankcollege.st10304152.taskmaster.data

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Task data class
 * Tests task creation, validation, and data integrity
 */
class TaskTest {

    @Test
    fun `test task creation with all parameters`() {
        // Arrange
        val title = "Test Task"
        val description = "Test Description"
        val isCompleted = false
        val dueDate = "Today"
        val priority = "High"
        val dueTime = "10:00 AM"
        val reminderEnabled = true
        val reminderTime = "15 minutes before"
        val createdAt = System.currentTimeMillis()
        val userId = "test-user-id"

        // Act
        val task = Task(
            id = "test-id",
            title = title,
            description = description,
            isCompleted = isCompleted,
            dueDate = dueDate,
            priority = priority,
            dueTime = dueTime,
            reminderEnabled = reminderEnabled,
            reminderTime = reminderTime,
            createdAt = createdAt,
            userId = userId
        )

        // Assert
        assertEquals("test-id", task.id)
        assertEquals(title, task.title)
        assertEquals(description, task.description)
        assertEquals(isCompleted, task.isCompleted)
        assertEquals(dueDate, task.dueDate)
        assertEquals(priority, task.priority)
        assertEquals(dueTime, task.dueTime)
        assertEquals(reminderEnabled, task.reminderEnabled)
        assertEquals(reminderTime, task.reminderTime)
        assertEquals(createdAt, task.createdAt)
        assertEquals(userId, task.userId)
    }

    @Test
    fun `test task creation with default values`() {
        // Act
        val task = Task()

        // Assert
        assertNotNull(task.id)
        assertEquals("", task.id) // id is empty by default
        assertEquals("", task.title)
        assertEquals("", task.description)
        assertEquals(false, task.isCompleted)
        assertEquals("", task.dueDate)
        assertEquals("Medium", task.priority)
        assertEquals("9:00 AM", task.dueTime) // Fixed: default is "9:00 AM", not ""
        assertEquals(false, task.reminderEnabled)
        assertEquals("15 minutes before", task.reminderTime)
        assertTrue(task.createdAt > 0)
        assertEquals("", task.userId)
    }

    @Test
    fun `test task copy with modified values`() {
        // Arrange
        val originalTask = Task(
            id = "original-id",
            title = "Original Title",
            description = "Original Description",
            isCompleted = false,
            dueDate = "Today",
            priority = "Low",
            dueTime = "9:00 AM",
            reminderEnabled = false,
            reminderTime = "30 minutes before",
            createdAt = 1000L,
            userId = "original-user"
        )

        // Act
        val modifiedTask = originalTask.copy(
            title = "Modified Title",
            isCompleted = true,
            priority = "High"
        )

        // Assert
        assertEquals("original-id", modifiedTask.id)
        assertEquals("Modified Title", modifiedTask.title)
        assertEquals("Original Description", modifiedTask.description)
        assertEquals(true, modifiedTask.isCompleted)
        assertEquals("Today", modifiedTask.dueDate)
        assertEquals("High", modifiedTask.priority)
        assertEquals("9:00 AM", modifiedTask.dueTime)
        assertEquals(false, modifiedTask.reminderEnabled)
        assertEquals("30 minutes before", modifiedTask.reminderTime)
        assertEquals(1000L, modifiedTask.createdAt)
        assertEquals("original-user", modifiedTask.userId)
    }

    @Test
    fun `test task serialization compatibility`() {
        // Arrange
        val task = Task(
            id = "serialization-test",
            title = "Serialization Test",
            description = "Testing serialization",
            isCompleted = true,
            dueDate = "Tomorrow",
            priority = "Medium",
            dueTime = "2:00 PM",
            reminderEnabled = true,
            reminderTime = "1 hour before",
            createdAt = 1234567890L,
            userId = "serialization-user"
        )

        // Act & Assert - Test that task implements Serializable
        assertTrue("Task should implement Serializable", task is Serializable)
    }

    @Test
    fun `test task equality`() {
        // Arrange
        val task1 = Task(
            id = "same-id",
            title = "Same Title",
            description = "Same Description",
            isCompleted = false,
            dueDate = "Today",
            priority = "High",
            dueTime = "10:00 AM",
            reminderEnabled = true,
            reminderTime = "15 minutes before",
            createdAt = 1000L,
            userId = "same-user"
        )

        val task2 = Task(
            id = "same-id",
            title = "Same Title",
            description = "Same Description",
            isCompleted = false,
            dueDate = "Today",
            priority = "High",
            dueTime = "10:00 AM",
            reminderEnabled = true,
            reminderTime = "15 minutes before",
            createdAt = 1000L,
            userId = "same-user"
        )

        // Act & Assert
        assertEquals(task1, task2)
        assertEquals(task1.hashCode(), task2.hashCode())
    }

    @Test
    fun `test task priority validation`() {
        // Arrange & Act
        val highPriorityTask = Task(priority = "High")
        val mediumPriorityTask = Task(priority = "Medium")
        val lowPriorityTask = Task(priority = "Low")
        val invalidPriorityTask = Task(priority = "Invalid")

        // Assert
        assertEquals("High", highPriorityTask.priority)
        assertEquals("Medium", mediumPriorityTask.priority)
        assertEquals("Low", lowPriorityTask.priority)
        assertEquals("Invalid", invalidPriorityTask.priority) // Should accept any string
    }
}
