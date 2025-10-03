package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.rosebankcollege.st10304152.taskmaster.R
import za.co.rosebankcollege.st10304152.taskmaster.data.Notification
import za.co.rosebankcollege.st10304152.taskmaster.data.NotificationType
import za.co.rosebankcollege.st10304152.taskmaster.data.Task
import za.co.rosebankcollege.st10304152.taskmaster.data.TaskRepository
import za.co.rosebankcollege.st10304152.taskmaster.ui.adapter.NotificationAdapter
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment : Fragment() {
    
    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()
    private val taskRepository = TaskRepository()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private var isCleared = false
    
    companion object {
        private const val PREFS_NAME = "notifications_prefs"
        private const val KEY_READ_NOTIFICATIONS = "read_notifications"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Set up the toolbar
        setupToolbar()
        
        setHasOptionsMenu(true)
        
        setupRecyclerView()
        setupFilterChips()
        setupClickListeners()
        loadNotifications()
    }
    
    private fun setupToolbar() {
        val toolbar = view?.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar?.let {
            // Set the toolbar as the activity's action bar
            (requireActivity() as? AppCompatActivity)?.setSupportActionBar(it)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notifications_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications_home -> {
                findNavController().popBackStack(R.id.homeFragment, false)
                true
            }
            R.id.action_notifications_settings -> {
                findNavController().navigate(R.id.settingsFragment)
                true
            }
            R.id.action_clear_all -> {
                clearAllNotifications()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupRecyclerView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.notifications_list)
        notificationAdapter = NotificationAdapter(notifications) { notification ->
            // Handle notification click - could navigate to task details
            // For now, just mark as read
            markNotificationAsRead(notification)
        }
        
        recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }
    
    private fun setupFilterChips() {
        val chipGroup = view?.findViewById<ChipGroup>(R.id.filter_chips)
        
        view?.findViewById<Chip>(R.id.chip_all)?.setOnClickListener {
            chipGroup?.check(R.id.chip_all)
            updateNotificationChipAppearance(R.id.chip_all)
            showAllNotifications()
        }
        
        view?.findViewById<Chip>(R.id.chip_unread)?.setOnClickListener {
            chipGroup?.check(R.id.chip_unread)
            updateNotificationChipAppearance(R.id.chip_unread)
            showUnreadNotifications()
        }
        
        view?.findViewById<Chip>(R.id.chip_important)?.setOnClickListener {
            chipGroup?.check(R.id.chip_important)
            updateNotificationChipAppearance(R.id.chip_important)
            showImportantNotifications()
        }
        
        // Set initial selection
        chipGroup?.check(R.id.chip_all)
        updateNotificationChipAppearance(R.id.chip_all)
    }
    
    private fun setupClickListeners() {
        view?.findViewById<View>(R.id.mark_all_read_button)?.setOnClickListener {
            markAllNotificationsAsRead()
        }
        
        // Add refresh functionality - double tap on empty state to refresh notifications
        view?.findViewById<LinearLayout>(R.id.empty_state)?.setOnClickListener {
            resetClearedState()
            loadNotifications()
        }
    }
    
    private fun loadNotifications() {
        // Check if notifications were cleared and should stay cleared
        val wasCleared = sharedPreferences.getBoolean("notifications_cleared", false)
        
        if (wasCleared || isCleared) {
            // If notifications were cleared, don't reload them
            isCleared = true
            updateNotificationCount()
            notificationAdapter.updateNotifications(notifications)
            showEmptyStateIfNeeded()
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = taskRepository.getTasks()
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        val tasks = result.getOrNull() ?: emptyList()
                        generateNotificationsFromTasks(tasks)
                        updateNotificationCount()
                        notificationAdapter.updateNotifications(notifications)
                        showEmptyStateIfNeeded()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle error
                }
            }
        }
    }
    
    private fun generateNotificationsFromTasks(tasks: List<Task>) {
        notifications.clear()
        val readNotificationIds = getReadNotificationIds()
        
        tasks.forEach { task ->
            // Generate notifications based on task properties
            when {
                task.dueDate == "Today" && !task.isCompleted -> {
                    val notificationId = "due_${task.id}"
                    notifications.add(Notification(
                        id = notificationId,
                        title = "Task Due Today",
                        message = "'${task.title}' is due today",
                        type = NotificationType.TASK_DUE,
                        priority = task.priority,
                        taskId = task.id,
                        isRead = readNotificationIds.contains(notificationId)
                    ))
                }
                task.isCompleted -> {
                    val notificationId = "completed_${task.id}"
                    notifications.add(Notification(
                        id = notificationId,
                        title = "Task Completed",
                        message = "'${task.title}' has been completed",
                        type = NotificationType.TASK_COMPLETED,
                        priority = "low",
                        taskId = task.id,
                        isRead = readNotificationIds.contains(notificationId)
                    ))
                }
                task.priority.lowercase() == "high" && !task.isCompleted -> {
                    val notificationId = "high_priority_${task.id}"
                    notifications.add(Notification(
                        id = notificationId,
                        title = "High Priority Task",
                        message = "'${task.title}' is a high priority task",
                        type = NotificationType.INFO,
                        priority = "high",
                        taskId = task.id,
                        isRead = readNotificationIds.contains(notificationId)
                    ))
                }
            }
        }
        
        // Sort by timestamp (newest first)
        notifications.sortByDescending { it.timestamp }
    }
    
    private fun showAllNotifications() {
        notificationAdapter.updateNotifications(notifications)
        showEmptyStateIfNeeded()
    }
    
    private fun showUnreadNotifications() {
        val unreadNotifications = notifications.filter { !it.isRead }
        notificationAdapter.updateNotifications(unreadNotifications)
        showEmptyStateIfNeeded(unreadNotifications.isEmpty())
    }
    
    private fun showImportantNotifications() {
        val importantNotifications = notifications.filter { 
            it.priority.lowercase() == "high" || it.type == NotificationType.TASK_OVERDUE 
        }
        notificationAdapter.updateNotifications(importantNotifications)
        showEmptyStateIfNeeded(importantNotifications.isEmpty())
    }
    
    private fun markNotificationAsRead(notification: Notification) {
        val index = notifications.indexOfFirst { it.id == notification.id }
        if (index != -1) {
            notifications[index] = notification.copy(isRead = true)
            saveReadNotificationId(notification.id)
            notificationAdapter.updateNotifications(notifications)
            updateNotificationCount()
        }
    }
    
    private fun markAllNotificationsAsRead() {
        notifications.forEachIndexed { index, notification ->
            if (!notification.isRead) {
                notifications[index] = notification.copy(isRead = true)
                saveReadNotificationId(notification.id)
            }
        }
        notificationAdapter.updateNotifications(notifications)
        updateNotificationCount()
    }
    
    private fun clearAllNotifications() {
        notifications.clear()
        clearAllReadNotificationIds()
        isCleared = true
        
        // Save the cleared state persistently
        sharedPreferences.edit()
            .putBoolean("notifications_cleared", true)
            .apply()
        
        notificationAdapter.updateNotifications(notifications)
        updateNotificationCount()
        showEmptyStateIfNeeded(true)
    }
    
    private fun getReadNotificationIds(): Set<String> {
        val readIdsJson = sharedPreferences.getString(KEY_READ_NOTIFICATIONS, "[]")
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(readIdsJson, type) ?: emptySet()
    }
    
    private fun saveReadNotificationId(notificationId: String) {
        val readIds = getReadNotificationIds().toMutableSet()
        readIds.add(notificationId)
        val readIdsJson = gson.toJson(readIds)
        sharedPreferences.edit().putString(KEY_READ_NOTIFICATIONS, readIdsJson).apply()
    }
    
    private fun clearAllReadNotificationIds() {
        sharedPreferences.edit().remove(KEY_READ_NOTIFICATIONS).apply()
    }
    
    private fun resetClearedState() {
        isCleared = false
        sharedPreferences.edit()
            .putBoolean("notifications_cleared", false)
            .apply()
    }
    
    private fun updateNotificationChipAppearance(selectedChipId: Int) {
        val context = requireContext()
        
        // Reset all chips to unselected appearance with more subtle styling
        view?.findViewById<Chip>(R.id.chip_all)?.apply {
            setChipBackgroundColorResource(R.color.white)
            setTextColor(context.getColor(R.color.primary))
            chipStrokeColor = context.getColorStateList(R.color.primary)
            chipStrokeWidth = 2f
            elevation = 2f
            alpha = 0.7f
            scaleX = 1.0f
            scaleY = 1.0f
        }
        
        view?.findViewById<Chip>(R.id.chip_unread)?.apply {
            setChipBackgroundColorResource(R.color.white)
            setTextColor(context.getColor(R.color.primary))
            chipStrokeColor = context.getColorStateList(R.color.primary)
            chipStrokeWidth = 2f
            elevation = 2f
            alpha = 0.7f
            scaleX = 1.0f
            scaleY = 1.0f
        }
        
        view?.findViewById<Chip>(R.id.chip_important)?.apply {
            setChipBackgroundColorResource(R.color.white)
            setTextColor(context.getColor(R.color.primary))
            chipStrokeColor = context.getColorStateList(R.color.primary)
            chipStrokeWidth = 2f
            elevation = 2f
            alpha = 0.7f
            scaleX = 1.0f
            scaleY = 1.0f
        }
        
        // Set selected chip appearance with enhanced visibility
        when (selectedChipId) {
            R.id.chip_all -> {
                view?.findViewById<Chip>(R.id.chip_all)?.apply {
                    setChipBackgroundColorResource(R.color.primary)
                    setTextColor(context.getColor(R.color.white))
                    chipStrokeWidth = 0f
                    elevation = 8f
                    alpha = 1.0f
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            }
            R.id.chip_unread -> {
                view?.findViewById<Chip>(R.id.chip_unread)?.apply {
                    setChipBackgroundColorResource(R.color.priority_medium)
                    setTextColor(context.getColor(R.color.white))
                    chipStrokeWidth = 0f
                    elevation = 8f
                    alpha = 1.0f
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            }
            R.id.chip_important -> {
                view?.findViewById<Chip>(R.id.chip_important)?.apply {
                    setChipBackgroundColorResource(R.color.priority_high)
                    setTextColor(context.getColor(R.color.white))
                    chipStrokeWidth = 0f
                    elevation = 8f
                    alpha = 1.0f
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            }
        }
    }
    
    private fun updateNotificationCount() {
        val totalCount = notifications.size
        val unreadCount = notifications.count { !it.isRead }
        
        view?.findViewById<TextView>(R.id.notification_count)?.text = "You have $totalCount notification${if (totalCount != 1) "s" else ""}"
        view?.findViewById<TextView>(R.id.unread_count)?.text = "$unreadCount unread message${if (unreadCount != 1) "s" else ""}"
    }
    
    private fun showEmptyStateIfNeeded(isEmpty: Boolean = notifications.isEmpty()) {
        val emptyState = view?.findViewById<LinearLayout>(R.id.empty_state)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.notifications_list)
        
        if (isEmpty) {
            emptyState?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        } else {
            emptyState?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }
    }
}



