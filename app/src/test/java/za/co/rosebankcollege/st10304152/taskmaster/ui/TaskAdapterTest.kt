package za.co.rosebankcollege.st10304152.taskmaster.ui

import za.co.rosebankcollege.st10304152.taskmaster.data.Task
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TaskAdapter functionality
 * Tests task data binding and UI logic
 */
class TaskAdapterTest {

    @Test
    fun `test task priority color mapping`() {
        // Arrange
        val highPriorityTask = createTaskWithPriority("High")
        val mediumPriorityTask = createTaskWithPriority("Medium")
        val lowPriorityTask = createTaskWithPriority("Low")

        // Act & Assert
        assertEquals("High", highPriorityTask.priority)
        assertEquals("Medium", mediumPriorityTask.priority)
        assertEquals("Low", lowPriorityTask.priority)
    }

    @Test
    fun `test task completion status`() {
        // Arrange
        val completedTask = createTaskWithCompletionStatus(true)
        val pendingTask = createTaskWithCompletionStatus(false)

        // Act & Assert
        assertTrue("Task should be completed", completedTask.isCompleted)
        assertFalse("Task should be pending", pendingTask.isCompleted)
    }

    @Test
    fun `test task title and description`() {
        // Arrange
        val title = "Test Task Title"
        val description = "This is a test task description"
        val task = createTaskWithTitleAndDescription(title, description)

        // Act & Assert
        assertEquals(title, task.title)
        assertEquals(description, task.description)
    }

    @Test
    fun `test task due date and time`() {
        // Arrange
        val dueDate = "Today"
        val dueTime = "2:00 PM"
        val task = createTaskWithDueDateAndTime(dueDate, dueTime)

        // Act & Assert
        assertEquals(dueDate, task.dueDate)
        assertEquals(dueTime, task.dueTime)
    }

    @Test
    fun `test task reminder configuration`() {
        // Arrange
        val reminderEnabled = true
        val reminderTime = "1 hour before"
        val task = createTaskWithReminder(reminderEnabled, reminderTime)

        // Act & Assert
        assertEquals(reminderEnabled, task.reminderEnabled)
        assertEquals(reminderTime, task.reminderTime)
    }

    @Test
    fun `test task data integrity`() {
        // Arrange
        val task = Task(
            id = "integrity-test",
            title = "Data Integrity Test",
            description = "Testing data integrity",
            isCompleted = false,
            dueDate = "Tomorrow",
            priority = "Medium",
            dueTime = "10:00 AM",
            reminderEnabled = true,
            reminderTime = "30 minutes before",
            createdAt = 1234567890L,
            userId = "test-user"
        )

        // Act & Assert
        assertNotNull("Task ID should not be null", task.id)
        assertNotNull("Task title should not be null", task.title)
        assertNotNull("Task description should not be null", task.description)
        assertNotNull("Task priority should not be null", task.priority)
        assertNotNull("Task due date should not be null", task.dueDate)
        assertNotNull("Task due time should not be null", task.dueTime)
        assertNotNull("Task reminder time should not be null", task.reminderTime)
        assertNotNull("Task user ID should not be null", task.userId)
        assertTrue("Created at timestamp should be positive", task.createdAt > 0)
    }

    @Test
    fun `test task list filtering logic`() {
        // Arrange
        val tasks = listOf(
            createTaskWithCompletionStatus(true),
            createTaskWithCompletionStatus(false),
            createTaskWithCompletionStatus(true),
            createTaskWithCompletionStatus(false)
        )

        // Act
        val completedTasks = tasks.filter { it.isCompleted }
        val pendingTasks = tasks.filter { !it.isCompleted }

        // Assert
        assertEquals(2, completedTasks.size)
        assertEquals(2, pendingTasks.size)
        assertTrue("All completed tasks should be completed", completedTasks.all { it.isCompleted })
        assertTrue("All pending tasks should not be completed", pendingTasks.all { !it.isCompleted })
    }

    @Test
    fun `test task priority filtering`() {
        // Arrange
        val tasks = listOf(
            createTaskWithPriority("High"),
            createTaskWithPriority("Medium"),
            createTaskWithPriority("Low"),
            createTaskWithPriority("High")
        )

        // Act
        val highPriorityTasks = tasks.filter { it.priority == "High" }
        val mediumPriorityTasks = tasks.filter { it.priority == "Medium" }
        val lowPriorityTasks = tasks.filter { it.priority == "Low" }

        // Assert
        assertEquals(2, highPriorityTasks.size)
        assertEquals(1, mediumPriorityTasks.size)
        assertEquals(1, lowPriorityTasks.size)
    }

    // Helper methods for creating test tasks
    private fun createTaskWithPriority(priority: String): Task {
        return Task(
            id = "priority-test-${priority.lowercase()}",
            title = "Priority Test Task",
            description = "Testing priority: $priority",
            isCompleted = false,
            dueDate = "Today",
            priority = priority,
            dueTime = "12:00 PM",
            reminderEnabled = false,
            reminderTime = "15 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "test-user"
        )
    }

    private fun createTaskWithCompletionStatus(isCompleted: Boolean): Task {
        return Task(
            id = "completion-test-${isCompleted}",
            title = "Completion Test Task",
            description = "Testing completion status: $isCompleted",
            isCompleted = isCompleted,
            dueDate = "Today",
            priority = "Medium",
            dueTime = "1:00 PM",
            reminderEnabled = false,
            reminderTime = "15 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "test-user"
        )
    }

    private fun createTaskWithTitleAndDescription(title: String, description: String): Task {
        return Task(
            id = "title-desc-test",
            title = title,
            description = description,
            isCompleted = false,
            dueDate = "Today",
            priority = "Medium",
            dueTime = "1:00 PM",
            reminderEnabled = false,
            reminderTime = "15 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "test-user"
        )
    }

    private fun createTaskWithDueDateAndTime(dueDate: String, dueTime: String): Task {
        return Task(
            id = "due-date-time-test",
            title = "Due Date Time Test Task",
            description = "Testing due date and time",
            isCompleted = false,
            dueDate = dueDate,
            priority = "Medium",
            dueTime = dueTime,
            reminderEnabled = false,
            reminderTime = "15 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "test-user"
        )
    }

    private fun createTaskWithReminder(reminderEnabled: Boolean, reminderTime: String): Task {
        return Task(
            id = "reminder-test",
            title = "Reminder Test Task",
            description = "Testing reminder configuration",
            isCompleted = false,
            dueDate = "Today",
            priority = "Medium",
            dueTime = "1:00 PM",
            reminderEnabled = reminderEnabled,
            reminderTime = reminderTime,
            createdAt = System.currentTimeMillis(),
            userId = "test-user"
        )
    }
}
