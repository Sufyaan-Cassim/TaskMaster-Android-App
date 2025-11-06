package za.co.rosebankcollege.st10304152.taskmaster.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import za.co.rosebankcollege.st10304152.taskmaster.data.database.TaskDao
import za.co.rosebankcollege.st10304152.taskmaster.data.database.TaskEntity
import za.co.rosebankcollege.st10304152.taskmaster.data.database.TaskMasterDatabase
import za.co.rosebankcollege.st10304152.taskmaster.data.database.toEntity
import za.co.rosebankcollege.st10304152.taskmaster.data.database.toTask
import za.co.rosebankcollege.st10304152.taskmaster.data.network.NetworkStateManager

/**
 * Enhanced TaskRepository with offline/online support
 * Handles both local Room database and Firebase Firestore
 */
class TaskRepositoryOffline(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    // Use applicationContext to avoid holding references to fragment/activity contexts
    private val appContext = context.applicationContext
    private val database = TaskMasterDatabase.getDatabase(appContext)
    private val taskDao = database.taskDao()
    private val networkStateManager = NetworkStateManager(appContext)
    private val notificationGenerator = NotificationGenerator(appContext)
    
    companion object {
        private const val COLLECTION_TASKS = "tasks"
    }
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Add a new task (works offline and online)
     */
    suspend fun addTask(task: Task): Result<Task> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val taskWithId = task.copy(
                id = if (task.id.isEmpty()) firestore.collection(COLLECTION_TASKS).document().id else task.id,
                userId = userId
            )
            
            // Always save to local database first
            val taskEntity = taskWithId.toEntity()
            taskDao.insertTask(taskEntity)
            
            // Try to sync with Firebase if online
            if (networkStateManager.isCurrentlyConnected()) {
                try {
                    firestore.collection(COLLECTION_TASKS)
                        .document(taskWithId.id)
                        .set(taskWithId)
                        .await()
                    
                    // Mark as synced
                    taskDao.markTaskAsSynced(taskWithId.id)
                } catch (e: Exception) {
                    // Mark sync as failed, will retry later
                    taskDao.markTaskSyncFailed(taskWithId.id)
                }
            }
            
            // Generate notification for task creation (non-blocking, don't fail task creation if it fails)
            try {
                notificationGenerator.generateTaskCreatedNotification(taskWithId)
            } catch (e: Exception) {
                // Notification generation failed - log but don't fail task creation
                // Task is already saved, notification is optional
            }
            
            Result.success(taskWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all tasks (from local database with sync status)
     */
    fun getTasks(): Flow<List<Task>> {
        val userId = getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return taskDao.getAllTasksForUser(userId).let { flow ->
            kotlinx.coroutines.flow.flow {
                flow.collect { entities ->
                    // Filter out deleted tasks (syncStatus = "delete_pending")
                    val activeTasks = entities.filter { it.syncStatus != "delete_pending" }
                    emit(activeTasks.map { it.toTask() })
                }
            }
        }
    }
    
    /**
     * Get tasks synchronously (for immediate use)
     */
    suspend fun getTasksSync(): Result<List<Task>> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val entities = taskDao.getAllTasksForUserSync(userId)
            // Filter out deleted tasks (syncStatus = "delete_pending")
            val activeTasks = entities.filter { it.syncStatus != "delete_pending" }
            val tasks = activeTasks.map { it.toTask() }
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing task
     */
    suspend fun updateTask(task: Task): Result<Task> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            if (task.id.isEmpty()) {
                return Result.failure(Exception("Task ID is required for update"))
            }

            val taskWithUserId = task.copy(userId = userId)

            // Get existing task safely
            val existingEntity = try {
                taskDao.getTaskById(task.id)
            } catch (e: Exception) {
                return Result.failure(e)
            }

            val taskEntity = if (existingEntity != null) {
                // Update existing entity while preserving sync metadata
                existingEntity.copy(
                    title = taskWithUserId.title,
                    description = taskWithUserId.description,
                    isCompleted = taskWithUserId.isCompleted,
                    dueDate = taskWithUserId.dueDate,
                    priority = taskWithUserId.priority,
                    dueTime = taskWithUserId.dueTime,
                    reminderEnabled = taskWithUserId.reminderEnabled,
                    reminderTime = taskWithUserId.reminderTime,
                    lastModified = System.currentTimeMillis(),
                    isSynced = false, // Mark as unsynced since we changed it
                    syncStatus = "pending"
                )
            } else {
                // New task
                taskWithUserId.toEntity()
            }

            // Update local database safely
            try {
                taskDao.updateTask(taskEntity)
            } catch (e: Exception) {
                return Result.failure(e)
            }

            // Try to sync with Firebase if online
            if (networkStateManager.isCurrentlyConnected()) {
                try {
                    firestore.collection(COLLECTION_TASKS)
                        .document(task.id)
                        .set(taskWithUserId)
                        .await()

                    try {
                        taskDao.markTaskAsSynced(task.id)
                    } catch (e: Exception) {
                        // Ignore sync status update errors
                    }
                } catch (e: Exception) {
                    try {
                        taskDao.markTaskSyncFailed(task.id)
                    } catch (dbError: Exception) {
                        // Ignore sync status update errors
                    }
                }
            }

            Result.success(taskWithUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a task
     */
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            if (taskId.isBlank()) {
                return Result.failure(Exception("Task ID is required"))
            }

            // Get existing task safely
            val taskEntity = try {
                taskDao.getTaskById(taskId)
            } catch (e: Exception) {
                return Result.failure(Exception("Database error: ${e.message}"))
            }

            if (taskEntity == null) {
                // Task not found - might already be deleted, return success
                return Result.success(Unit)
            }

            // Check if task was ever synced to Firebase
            val wasEverSynced = taskEntity.isSynced && taskEntity.syncStatus == "synced"

            // If task was never synced, just delete locally immediately
            if (!wasEverSynced) {
                try {
                    taskDao.deleteTaskById(taskId)
                    return Result.success(Unit)
                } catch (e: Exception) {
                    return Result.failure(Exception("Failed to delete task from local database: ${e.message}"))
                }
            }

            // Task was synced - need to delete from Firebase too
            // Mark as delete_pending first (for offline scenarios)
            try {
                val deletedEntity = taskEntity.copy(
                    lastModified = System.currentTimeMillis(),
                    isSynced = false,
                    syncStatus = "delete_pending"
                )
                taskDao.updateTask(deletedEntity)
            } catch (e: Exception) {
                return Result.failure(Exception("Failed to mark task for deletion: ${e.message}"))
            }

            // Try to delete from Firebase if online
            if (networkStateManager.isCurrentlyConnected()) {
                try {
                    // Firebase delete is idempotent - deleting non-existent doc doesn't error
                    firestore.collection(COLLECTION_TASKS)
                        .document(taskId)
                        .delete()
                        .await()

                    // Successfully deleted from Firebase - now delete from local DB
                    try {
                        taskDao.deleteTaskById(taskId)
                        return Result.success(Unit)
                    } catch (e: Exception) {
                        // Local deletion failed but Firebase succeeded
                        // Task is effectively deleted, but we'll leave it in DB as delete_pending
                        // syncOfflineChanges will clean it up later
                        return Result.success(Unit)
                    }
                } catch (e: Exception) {
                    // Firebase deletion failed - mark as delete_failed for retry
                    try {
                        taskDao.updateSyncStatus(taskId, "delete_failed")
                        // Return success - task is marked for deletion, will retry on sync
                        return Result.success(Unit)
                    } catch (dbError: Exception) {
                        // Can't update status - this is a serious error
                        return Result.failure(Exception("Failed to mark deletion as failed: ${dbError.message}"))
                    }
                }
            } else {
                // Offline - task is marked as delete_pending, will be synced when online
                return Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error during deletion: ${e.message}"))
        }
    }

    /**
     * Toggle task completion status
     */
    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {

                return Result.failure(Exception("User not authenticated"))
            }
            
            if (taskId.isBlank()) {

                return Result.failure(Exception("Task ID is required"))
            }
            
            // Get existing task safely
            val taskEntity = try {
                taskDao.getTaskById(taskId)
            } catch (e: Exception) {

                return Result.failure(e)
            }
            
            if (taskEntity == null) {

                return Result.failure(Exception("Task not found"))
            }
            
            // Update the existing entity with new completion status
            val updatedEntity = taskEntity.copy(
                isCompleted = isCompleted,
                lastModified = System.currentTimeMillis(),
                isSynced = false, // Mark as unsynced since we changed it
                syncStatus = "pending"
            )
            
            // Update local database safely
            try {
                taskDao.updateTask(updatedEntity)

            } catch (e: Exception) {

                return Result.failure(e)
            }
            
            // Try to sync with Firebase if online
            if (networkStateManager.isCurrentlyConnected()) {
                try {
                    firestore.collection(COLLECTION_TASKS)
                        .document(taskId)
                        .update("isCompleted", isCompleted)
                        .await()
                    
                    // Mark as synced
                    try {
                        taskDao.markTaskAsSynced(taskId)

                    } catch (e: Exception) {

                    }
                } catch (e: Exception) {
                    // Mark sync as failed, will retry later
                    try {
                        taskDao.markTaskSyncFailed(taskId)

                    } catch (dbError: Exception) {

                    }
                }
            }
            
            // Generate notification for task completion (non-blocking, don't fail task update if it fails)
            if (isCompleted) {
                try {
                    val task = updatedEntity.toTask()
                    notificationGenerator.generateTaskCompletedNotification(task)
                } catch (e: Exception) {
                    // Notification generation failed - log but don't fail task update
                    // Task is already updated, notification is optional
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {

            Result.failure(e)
        }
    }
    
    /**
     * Sync offline changes with Firebase
     */
    suspend fun syncOfflineChanges(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            if (!networkStateManager.isCurrentlyConnected()) {
                return Result.failure(Exception("No internet connection"))
            }

            val unsyncedTasks = try {
                taskDao.getUnsyncedTasks(userId)
            } catch (e: Exception) {
                return Result.failure(e)
            }

            for (taskEntity in unsyncedTasks) {
                if (taskEntity.id.isBlank()) continue

                try {
                    // 🔹 Handle delete_pending and delete_failed tasks separately
                    if (taskEntity.syncStatus == "delete_pending" || taskEntity.syncStatus == "delete_failed") {
                        try {
                            firestore.collection(COLLECTION_TASKS)
                                .document(taskEntity.id)
                                .delete()
                                .await()

                            // Remove from local DB after successful remote delete
                            // (Firebase delete is idempotent - deleting non-existent doc doesn't error)
                            taskDao.deleteTaskById(taskEntity.id)
                        } catch (e: Exception) {
                            // Only mark as failed if it's still in delete_pending state
                            // If it was already delete_failed, leave it as is (will retry next sync)
                            if (taskEntity.syncStatus == "delete_pending") {
                                taskDao.updateSyncStatus(taskEntity.id, "delete_failed")
                            }
                        }
                        continue // Move to next task
                    }

                    // 🔹 Otherwise, normal sync (create/update)
                    taskDao.updateSyncStatus(taskEntity.id, "syncing")

                    val task = taskEntity.toTask()
                    firestore.collection(COLLECTION_TASKS)
                        .document(task.id)
                        .set(task)
                        .await()

                    taskDao.markTaskAsSynced(task.id)
                } catch (e: Exception) {
                    taskDao.markTaskSyncFailed(taskEntity.id)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Download tasks from Firebase and save to local database
     */
    suspend fun downloadTasksFromFirebase(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            if (!networkStateManager.isCurrentlyConnected()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val snapshot = firestore.collection(COLLECTION_TASKS)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val tasks = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Task::class.java)
                } catch (e: Exception) {
                    null
                }
            }
            
            // Convert to entities and insert/update in local database
            val entities = tasks.map { task ->
                task.toEntity().copy(isSynced = true, syncStatus = "synced")
            }
            
            // Only insert/update tasks that are not marked for deletion locally
            for (entity in entities) {
                val existingEntity = taskDao.getTaskById(entity.id)
                if (existingEntity == null || existingEntity.syncStatus != "delete_pending") {
                    // Only update if not marked for deletion
                    taskDao.insertTask(entity)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get network state
     */
    fun getNetworkState(): Flow<Boolean> {
        return networkStateManager.getNetworkState()
    }
    
    /**
     * Check if currently online
     */
    fun isOnline(): Boolean {
        return networkStateManager.isCurrentlyConnected()
    }
    
    /**
     * Process all tasks and generate appropriate notifications
     */
    suspend fun processTasksForNotifications(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val tasks = taskDao.getAllTasksForUserSync(userId).map { it.toTask() }
            notificationGenerator.processTasksForNotifications(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear all local data (for logout)
     */
    suspend fun clearLocalData() {
        val userId = getCurrentUserId()
        if (userId != null) {
            taskDao.clearAllTasksForUser(userId)
        }
    }
}
