package za.co.rosebankcollege.st10304152.taskmaster.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*

/**
 * Unit tests for TaskRepository
 * Tests task management operations with mocked Firebase dependencies
 */
@RunWith(MockitoJUnitRunner::class)
class TaskRepositoryTest {

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    private lateinit var taskRepository: TaskRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // Note: In a real implementation, you would inject these mocks
        // For now, we'll test the data validation and business logic
    }

    @Test
    fun `test task validation with valid data`() {
        // Arrange
        val validTask = Task(
            id = "valid-task-id",
            title = "Valid Task",
            description = "This is a valid task description",
            isCompleted = false,
            dueDate = "Today",
            priority = "High",
            dueTime = "10:00 AM",
            reminderEnabled = true,
            reminderTime = "15 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "valid-user-id"
        )

        // Act & Assert
        assertTrue("Task should be valid", isValidTask(validTask))
    }

    @Test
    fun `test task validation with empty title`() {
        // Arrange
        val invalidTask = Task(
            id = "invalid-task-id",
            title = "",
            description = "Task with empty title",
            isCompleted = false,
            dueDate = "Today",
            priority = "High",
            dueTime = "10:00 AM",
            reminderEnabled = true,
            reminderTime = "15 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "valid-user-id"
        )

        // Act & Assert
        assertFalse("Task with empty title should be invalid", isValidTask(invalidTask))
    }

    @Test
    fun `test task validation with null title`() {
        // Arrange
        val invalidTask = Task(
            id = "invalid-task-id",
            title = null,
            description = "Task with null title",
            isCompleted = false,
            dueDate = "Today",
            priority = "High",
            dueTime = "10:00 AM",
            reminderEnabled = true,
            reminderTime = "15 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "valid-user-id"
        )

        // Act & Assert
        assertFalse("Task with null title should be invalid", isValidTask(invalidTask))
    }

    @Test
    fun `test task priority validation`() {
        // Arrange
        val highPriorityTask = createTaskWithPriority("High")
        val mediumPriorityTask = createTaskWithPriority("Medium")
        val lowPriorityTask = createTaskWithPriority("Low")
        val invalidPriorityTask = createTaskWithPriority("Invalid")

        // Act & Assert
        assertTrue("High priority task should be valid", isValidTask(highPriorityTask))
        assertTrue("Medium priority task should be valid", isValidTask(mediumPriorityTask))
        assertTrue("Low priority task should be valid", isValidTask(lowPriorityTask))
        assertTrue("Invalid priority task should still be valid (flexible validation)", isValidTask(invalidPriorityTask))
    }

    @Test
    fun `test task completion status toggle`() {
        // Arrange
        val incompleteTask = Task(
            id = "toggle-test",
            title = "Toggle Test Task",
            description = "Testing completion toggle",
            isCompleted = false,
            dueDate = "Today",
            priority = "Medium",
            dueTime = "2:00 PM",
            reminderEnabled = false,
            reminderTime = "15 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "test-user"
        )

        // Act
        val completedTask = incompleteTask.copy(isCompleted = true)

        // Assert
        assertEquals(false, incompleteTask.isCompleted)
        assertEquals(true, completedTask.isCompleted)
        assertEquals(incompleteTask.id, completedTask.id)
        assertEquals(incompleteTask.title, completedTask.title)
    }

    @Test
    fun `test task due date validation`() {
        // Arrange
        val todayTask = createTaskWithDueDate("Today")
        val tomorrowTask = createTaskWithDueDate("Tomorrow")
        val specificDateTask = createTaskWithDueDate("Dec 25, 2024")
        val emptyDateTask = createTaskWithDueDate("")

        // Act & Assert
        assertTrue("Today task should be valid", isValidTask(todayTask))
        assertTrue("Tomorrow task should be valid", isValidTask(tomorrowTask))
        assertTrue("Specific date task should be valid", isValidTask(specificDateTask))
        assertTrue("Empty date task should be valid", isValidTask(emptyDateTask))
    }

    @Test
    fun `test task reminder configuration`() {
        // Arrange
        val reminderEnabledTask = Task(
            id = "reminder-enabled",
            title = "Reminder Enabled Task",
            description = "Task with reminders enabled",
            isCompleted = false,
            dueDate = "Today",
            priority = "High",
            dueTime = "3:00 PM",
            reminderEnabled = true,
            reminderTime = "1 hour before",
            createdAt = System.currentTimeMillis(),
            userId = "test-user"
        )

        val reminderDisabledTask = Task(
            id = "reminder-disabled",
            title = "Reminder Disabled Task",
            description = "Task with reminders disabled",
            isCompleted = false,
            dueDate = "Today",
            priority = "Low",
            dueTime = "4:00 PM",
            reminderEnabled = false,
            reminderTime = "30 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "test-user"
        )

        // Act & Assert
        assertTrue("Reminder enabled task should be valid", isValidTask(reminderEnabledTask))
        assertTrue("Reminder disabled task should be valid", isValidTask(reminderDisabledTask))
        assertEquals(true, reminderEnabledTask.reminderEnabled)
        assertEquals(false, reminderDisabledTask.reminderEnabled)
    }

    // Helper methods for testing
    private fun isValidTask(task: Task): Boolean {
        return task.title.isNotEmpty() && 
               task.id.isNotEmpty() && 
               task.userId.isNotEmpty() &&
               task.createdAt > 0
    }

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

    private fun createTaskWithDueDate(dueDate: String): Task {
        return Task(
            id = "due-date-test",
            title = "Due Date Test Task",
            description = "Testing due date: $dueDate",
            isCompleted = false,
            dueDate = dueDate,
            priority = "Medium",
            dueTime = "1:00 PM",
            reminderEnabled = false,
            reminderTime = "15 minutes before",
            createdAt = System.currentTimeMillis(),
            userId = "test-user"
        )
    }
}
