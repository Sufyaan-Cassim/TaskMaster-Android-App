package za.co.rosebankcollege.st10304152.taskmaster.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllNotificationsForUser(userId: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllNotificationsForUserSync(userId: String): List<NotificationEntity>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotificationsForUser(userId: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    suspend fun getUnreadNotificationsForUserSync(userId: String): List<NotificationEntity>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getNotificationsSince(userId: String, since: Long): List<NotificationEntity>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    suspend fun getNotificationsByType(userId: String, type: String): List<NotificationEntity>
    
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
    
    @Update
    suspend fun updateNotification(notification: NotificationEntity)
    
    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String, isRead: Boolean)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
    
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: String)
    
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllNotificationsForUser(userId: String)
    
    @Query("DELETE FROM notifications WHERE userId = :userId AND timestamp < :cutoffTime")
    suspend fun deleteOldNotifications(userId: String, cutoffTime: Long)
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedNotifications(userId: String): List<NotificationEntity>
    
    @Query("UPDATE notifications SET isSynced = 1, syncStatus = 'synced' WHERE id = :notificationId")
    suspend fun markAsSynced(notificationId: String)
    
    @Query("UPDATE notifications SET syncStatus = 'sync_failed' WHERE id = :notificationId")
    suspend fun markSyncFailed(notificationId: String)
    
    @Query("UPDATE notifications SET syncStatus = :status WHERE id = :notificationId")
    suspend fun updateSyncStatus(notificationId: String, status: String)
}
