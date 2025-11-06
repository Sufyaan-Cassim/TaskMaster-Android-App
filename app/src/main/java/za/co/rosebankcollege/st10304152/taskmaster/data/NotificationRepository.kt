package za.co.rosebankcollege.st10304152.taskmaster.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import za.co.rosebankcollege.st10304152.taskmaster.data.database.NotificationDao
import za.co.rosebankcollege.st10304152.taskmaster.data.database.NotificationEntity
import za.co.rosebankcollege.st10304152.taskmaster.data.database.TaskMasterDatabase
import za.co.rosebankcollege.st10304152.taskmaster.data.database.toEntity
import za.co.rosebankcollege.st10304152.taskmaster.data.database.toNotification
import za.co.rosebankcollege.st10304152.taskmaster.data.network.NetworkStateManager
import java.util.*

/**
 * Repository for managing notifications with offline-first approach
 * Handles both local Room database and Firebase Firestore
 */
class NotificationRepository(context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    // Use applicationContext to avoid holding references to fragment/activity contexts
    private val appContext = context.applicationContext
    private val database = TaskMasterDatabase.getDatabase(appContext)
    private val notificationDao = database.notificationDao()
    private val networkStateManager = NetworkStateManager(appContext)
    
    companion object {
        private const val COLLECTION_NOTIFICATIONS = "notifications"
        private const val NOTIFICATION_RETENTION_DAYS = 30L // Keep notifications for 30 days
    }
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Get all notifications for current user
     */
    fun getNotifications(): Flow<List<Notification>> {
        val userId = getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return notificationDao.getAllNotificationsForUser(userId).map { entities ->
            entities.map { it.toNotification() }
        }
    }
    
    /**
     * Get unread notifications for current user
     */
    fun getUnreadNotifications(): Flow<List<Notification>> {
        val userId = getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return notificationDao.getUnreadNotificationsForUser(userId).map { entities ->
            entities.map { it.toNotification() }
        }
    }
    
    /**
     * Get notifications synchronously
     */
    suspend fun getNotificationsSync(): Result<List<Notification>> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val entities = notificationDao.getAllNotificationsForUserSync(userId)
            val notifications = entities.map { it.toNotification() }
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get unread count
     */
    suspend fun getUnreadCount(): Result<Int> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val count = notificationDao.getUnreadCount(userId)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add a new notification
     */
    suspend fun addNotification(notification: Notification): Result<Notification> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val notificationWithUserId = notification.copy(
                id = if (notification.id.isEmpty()) UUID.randomUUID().toString() else notification.id,
                userId = userId
            )
            
            // Save locally first
            val entity = notificationWithUserId.toEntity()
            notificationDao.insertNotification(entity)
            
            // Try to sync with Firebase if online
            if (networkStateManager.isCurrentlyConnected()) {
                try {
                    firestore.collection(COLLECTION_NOTIFICATIONS)
                        .document(notificationWithUserId.id)
                        .set(notificationWithUserId)
                        .await()
                    
                    // Mark as synced
                    notificationDao.markAsSynced(notificationWithUserId.id)
                } catch (e: Exception) {
                    // Mark sync as failed, will retry later
                    notificationDao.markSyncFailed(notificationWithUserId.id)
                }
            }
            
            Result.success(notificationWithUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark notification as read/unread
     */
    suspend fun markAsRead(notificationId: String, isRead: Boolean): Result<Unit> {
        return try {
            notificationDao.markAsRead(notificationId, isRead)
            
            // Try to sync with Firebase if online
            if (networkStateManager.isCurrentlyConnected()) {
                try {
                    firestore.collection(COLLECTION_NOTIFICATIONS)
                        .document(notificationId)
                        .update("isRead", isRead)
                        .await()
                } catch (e: Exception) {
                    // Ignore sync errors for read status
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            notificationDao.markAllAsRead(userId)
            
            // Try to sync with Firebase if online
            if (networkStateManager.isCurrentlyConnected()) {
                try {
                    firestore.collection(COLLECTION_NOTIFICATIONS)
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("isRead", false)
                        .get()
                        .await()
                        .forEach { document ->
                            document.reference.update("isRead", true).await()
                        }
                } catch (e: Exception) {
                    // Ignore sync errors
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete notification
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationDao.deleteNotification(notificationId)
            
            // Try to sync with Firebase if online
            if (networkStateManager.isCurrentlyConnected()) {
                try {
                    firestore.collection(COLLECTION_NOTIFICATIONS)
                        .document(notificationId)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    // Ignore sync errors
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear all notifications
     */
    suspend fun clearAllNotifications(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            notificationDao.deleteAllNotificationsForUser(userId)
            
            // Try to sync with Firebase if online
            if (networkStateManager.isCurrentlyConnected()) {
                try {
                    firestore.collection(COLLECTION_NOTIFICATIONS)
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                        .forEach { document ->
                            document.reference.delete().await()
                        }
                } catch (e: Exception) {
                    // Ignore sync errors
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean up old notifications (older than 30 days)
     */
    suspend fun cleanupOldNotifications(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val cutoffTime = System.currentTimeMillis() - (NOTIFICATION_RETENTION_DAYS * 24 * 60 * 60 * 1000)
            notificationDao.deleteOldNotifications(userId, cutoffTime)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync offline changes to Firebase
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
            
            val unsyncedNotifications = notificationDao.getUnsyncedNotifications(userId)
            
            for (notificationEntity in unsyncedNotifications) {
                try {
                    if (notificationEntity.id.isBlank()) {
                        continue
                    }
                    
                    // Update sync status
                    notificationDao.updateSyncStatus(notificationEntity.id, "syncing")
                    
                    val notification = notificationEntity.toNotification()
                    
                    // Sync to Firebase
                    firestore.collection(COLLECTION_NOTIFICATIONS)
                        .document(notification.id)
                        .set(notification)
                        .await()
                    
                    // Mark as synced
                    notificationDao.markAsSynced(notification.id)
                } catch (e: Exception) {
                    try {
                        notificationDao.markSyncFailed(notificationEntity.id)
                    } catch (dbError: Exception) {
                        // Ignore database errors
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download notifications from Firebase
     */
    suspend fun downloadNotificationsFromFirebase(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            if (!networkStateManager.isCurrentlyConnected()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val firebaseNotifications = firestore.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val entities = firebaseNotifications.map { document ->
                val notification = document.toObject(Notification::class.java)
                notification.copy(id = document.id).toEntity().copy(isSynced = true, syncStatus = "synced")
            }
            
            notificationDao.insertNotifications(entities)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if device is online
     */
    fun isOnline(): Boolean {
        return networkStateManager.isCurrentlyConnected()
    }
}
