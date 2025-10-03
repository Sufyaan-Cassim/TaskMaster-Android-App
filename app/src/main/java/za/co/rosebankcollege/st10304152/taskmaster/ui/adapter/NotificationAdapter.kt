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
        
        holder.title.text = notification.title
        holder.message.text = notification.message
        holder.time.text = formatTime(notification.timestamp)
        
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

    private fun formatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            else -> {
                val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
        }
    }
}
