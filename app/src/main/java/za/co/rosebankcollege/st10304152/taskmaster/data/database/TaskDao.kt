package za.co.rosebankcollege.st10304152.taskmaster.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Task operations
 * Provides methods to interact with the local Room database
 */
@Dao
interface TaskDao {

    /**
     * Get all tasks for a specific user (excluding delete-pending tasks)
     */
    @Query("""
    SELECT * FROM tasks 
    WHERE user_id = :userId 
    AND sync_status NOT IN ('delete_pending', 'delete_failed') 
    ORDER BY created_at DESC
""")
    fun getAllTasksForUser(userId: String): Flow<List<TaskEntity>>

    /**
     * Get all tasks for a specific user (synchronous, excluding delete-pending tasks)
     */
    @Query("""
    SELECT * FROM tasks 
    WHERE user_id = :userId 
    AND sync_status NOT IN ('delete_pending', 'delete_failed') 
    ORDER BY created_at DESC
""")
    suspend fun getAllTasksForUserSync(userId: String): List<TaskEntity>

    /**
     * Get a specific task by ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    /**
     * Get tasks that need to be synced
     */
    @Query("SELECT * FROM tasks WHERE is_synced = 0 AND user_id = :userId")
    suspend fun getUnsyncedTasks(userId: String): List<TaskEntity>
    
    /**
     * Get tasks by completion status
     */
    @Query("SELECT * FROM tasks WHERE is_completed = :isCompleted AND user_id = :userId ORDER BY created_at DESC")
    fun getTasksByCompletionStatus(userId: String, isCompleted: Boolean): Flow<List<TaskEntity>>
    
    /**
     * Get tasks due today
     */
    @Query("SELECT * FROM tasks WHERE due_date = :today AND user_id = :userId ORDER BY due_time ASC")
    fun getTasksDueToday(userId: String, today: String): Flow<List<TaskEntity>>
    
    /**
     * Insert a new task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    /**
     * Insert multiple tasks
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    /**
     * Update an existing task
     */
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    /**
     * Delete a task
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    /**
     * Delete task by ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
    
    /**
     * Mark task as synced
     */
    @Query("UPDATE tasks SET is_synced = 1, sync_status = 'synced' WHERE id = :taskId")
    suspend fun markTaskAsSynced(taskId: String)
    
    /**
     * Mark task sync as failed
     */
    @Query("UPDATE tasks SET sync_status = 'failed' WHERE id = :taskId")
    suspend fun markTaskSyncFailed(taskId: String)
    
    /**
     * Update sync status
     */
    @Query("UPDATE tasks SET sync_status = :status WHERE id = :taskId")
    suspend fun updateSyncStatus(taskId: String, status: String)
    
    /**
     * Clear all tasks for a user (for logout)
     */
    @Query("DELETE FROM tasks WHERE user_id = :userId")
    suspend fun clearAllTasksForUser(userId: String)
    
    /**
     * Get task count by completion status
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = :isCompleted AND user_id = :userId")
    suspend fun getTaskCountByStatus(userId: String, isCompleted: Boolean): Int

    /**
     * Get total task count for user
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId")
    suspend fun getTotalTaskCount(userId: String): Int
}
