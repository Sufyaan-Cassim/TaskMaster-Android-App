package za.co.rosebankcollege.st10304152.taskmaster.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val COLLECTION_TASKS = "tasks"
    }
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    suspend fun addTask(task: Task): Result<Task> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val taskWithId = task.copy(
                id = firestore.collection(COLLECTION_TASKS).document().id,
                userId = userId
            )
            
            firestore.collection(COLLECTION_TASKS)
                .document(taskWithId.id)
                .set(taskWithId)
                .await()
            Result.success(taskWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
            suspend fun getTasks(): Result<List<Task>> {
                return try {
                    val userId = getCurrentUserId()
                    if (userId == null) {
                        return Result.failure(Exception("User not authenticated"))
                    }

                    val snapshot = firestore.collection(COLLECTION_TASKS)
                        .get()
                        .await()

                    val tasks = snapshot.documents.mapNotNull { document ->
                        try {
                            val task = document.toObject(Task::class.java)
                            if (task?.userId == userId) {
                                task
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }

                    Result.success(tasks)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    
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
            
            firestore.collection(COLLECTION_TASKS)
                .document(task.id)
                .set(taskWithUserId)
                .await()
            
            Result.success(taskWithUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            firestore.collection(COLLECTION_TASKS)
                .document(taskId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            firestore.collection(COLLECTION_TASKS)
                .document(taskId)
                .update("isCompleted", isCompleted)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
