package za.co.rosebankcollege.st10304152.taskmaster.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

/**
 * Room database for TaskMaster app
 * Handles local storage of tasks for offline functionality
 */
@Database(
    entities = [TaskEntity::class, NotificationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TaskMasterDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun notificationDao(): NotificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: TaskMasterDatabase? = null
        
        private const val DATABASE_NAME = "taskmaster_database"
        
        // Migration from version 1 to 2 (adding notifications table)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        type TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        taskId TEXT,
                        isRead INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        userId TEXT NOT NULL,
                        isSynced INTEGER NOT NULL DEFAULT 0,
                        syncStatus TEXT NOT NULL DEFAULT 'pending',
                        lastModified INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }
        
        /**
         * Get database instance (Singleton pattern)
         */
        fun getDatabase(context: Context): TaskMasterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskMasterDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Clear database instance (for testing or logout)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
