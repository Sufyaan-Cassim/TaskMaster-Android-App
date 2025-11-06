package za.co.rosebankcollege.st10304152.taskmaster.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import za.co.rosebankcollege.st10304152.taskmaster.R
import za.co.rosebankcollege.st10304152.taskmaster.data.Notification
import za.co.rosebankcollege.st10304152.taskmaster.data.NotificationType
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit = {}
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.notification_card)
        val icon: ImageView = itemView.findViewById(R.id.notification_icon)
        val title: TextView = itemView.findViewById(R.id.notification_title)
        val message: TextView = itemView.findViewById(R.id.notification_message)
        val time: TextView = itemView.findViewById(R.id.notification_time)
        val unreadIndicator: View = itemView.findViewById(R.id.unread_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        val context = holder.itemView.context
        
        // Localize notification title and message based on type
        val (localizedTitle, localizedMessage) = getLocalizedNotificationText(notification, context)
        
        holder.title.text = localizedTitle
        holder.message.text = localizedMessage
        holder.time.text = formatTime(notification.timestamp, context)
        
        // Set icon based on notification type
        val iconRes = when (notification.type) {
            NotificationType.TASK_DUE -> R.drawable.ic_calendar
            NotificationType.TASK_OVERDUE -> R.drawable.ic_warning
            NotificationType.TASK_COMPLETED -> R.drawable.ic_check
            NotificationType.TASK_CREATED -> R.drawable.ic_add
            NotificationType.REMINDER -> R.drawable.ic_notifications
            NotificationType.INFO -> R.drawable.ic_info
        }
        holder.icon.setImageResource(iconRes)
        
        // Set priority color
        val priorityColor = when (notification.priority.lowercase()) {
            "high" -> R.color.priority_high
            "medium" -> R.color.priority_medium
            "low" -> R.color.priority_low
            else -> R.color.priority_medium
        }
        holder.icon.setColorFilter(holder.itemView.context.resources.getColor(priorityColor, null))
        
        // Show/hide unread indicator
        holder.unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE
        
        // Set card alpha for read notifications
        holder.card.alpha = if (notification.isRead) 0.7f else 1.0f
        
        // Handle click
        holder.card.setOnClickListener {
            onNotificationClick(notification)
        }
    }

    override fun getItemCount() = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    private fun formatTime(timestamp: Long, context: android.content.Context): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> context.getString(R.string.just_now)
            diff < 60 * 60 * 1000 -> context.getString(R.string.n_minutes_ago, diff / (60 * 1000))
            diff < 24 * 60 * 60 * 1000 -> context.getString(R.string.n_hours_ago, diff / (60 * 60 * 1000))
            else -> {
                val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
        }
    }
    
    /**
     * Extract task title from notification message and re-localize
     */
    private fun getLocalizedNotificationText(
        notification: Notification,
        context: android.content.Context
    ): Pair<String, String> {
        // Try to extract task title from message (between quotes or single quotes)
        val taskTitle = extractTaskTitleFromMessage(notification.message) ?: ""
        
        return when (notification.type) {
            NotificationType.TASK_DUE -> {
                // Check if it's "due today" pattern
                val title = if (notification.title.contains("Today", ignoreCase = true) ||
                    notification.title.contains("Vandag", ignoreCase = true) ||
                    notification.title.contains("Namhlanje", ignoreCase = true)) {
                    context.getString(R.string.task_due_today_title)
                } else {
                    context.getString(R.string.task_due_soon)
                }
                val message = if (taskTitle.isNotEmpty()) {
                    context.getString(R.string.task_due_today_message, taskTitle)
                } else {
                    // Fallback: try to extract from original message or use as-is
                    notification.message
                }
                Pair(title, message)
            }
            NotificationType.TASK_COMPLETED -> {
                val title = context.getString(R.string.task_completed_title)
                val message = if (taskTitle.isNotEmpty()) {
                    context.getString(R.string.task_completed_message, taskTitle)
                } else {
                    notification.message
                }
                Pair(title, message)
            }
            NotificationType.TASK_CREATED -> {
                val title = context.getString(R.string.task_created_title)
                val message = if (taskTitle.isNotEmpty()) {
                    context.getString(R.string.task_created_message, taskTitle)
                } else {
                    notification.message
                }
                Pair(title, message)
            }
            NotificationType.TASK_OVERDUE -> {
                val title = context.getString(R.string.task_overdue_title)
                val message = if (taskTitle.isNotEmpty()) {
                    context.getString(R.string.task_overdue_message, taskTitle)
                } else {
                    notification.message
                }
                Pair(title, message)
            }
            NotificationType.INFO -> {
                // For INFO type, check if it's high priority task
                if (notification.priority.lowercase() == "high" && taskTitle.isNotEmpty()) {
                    Pair(
                        context.getString(R.string.high_priority_task_title),
                        context.getString(R.string.high_priority_task_message, taskTitle)
                    )
                } else {
                    Pair(notification.title, notification.message)
                }
            }
            NotificationType.REMINDER -> {
                Pair(notification.title, notification.message)
            }
        }
    }
    
    /**
     * Extract task title from notification message
     * Looks for text between single quotes, double quotes, or after common patterns
     */
    private fun extractTaskTitleFromMessage(message: String): String? {
        // Try to extract text between single quotes ('...')
        val singleQuotePattern = Regex("'([^']+)'")
        singleQuotePattern.find(message)?.let {
            return it.groupValues[1]
        }
        
        // Try to extract text between double quotes ("...")
        val doubleQuotePattern = Regex("\"([^\"]+)\"")
        doubleQuotePattern.find(message)?.let {
            return it.groupValues[1]
        }
        
        // Try common patterns for different languages
        // English: "Task 'X' is due today" or "'X' is due today"
        // Afrikaans: "Taak 'X' is vandag af" or "'X' is vandag af"
        // isiZulu: "Umsebenzi 'X' ufaneleke namhlanje" or "'X' ufaneleke namhlanje"
        
        // Pattern: anything between quotes that appears before common words
        val pattern = Regex("'([^']+)'")
        val match = pattern.find(message)
        return match?.groupValues?.get(1)
    }
}
