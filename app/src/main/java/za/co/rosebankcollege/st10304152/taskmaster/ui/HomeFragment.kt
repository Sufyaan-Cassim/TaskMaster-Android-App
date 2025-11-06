package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import za.co.rosebankcollege.st10304152.taskmaster.R
import za.co.rosebankcollege.st10304152.taskmaster.data.Task
import za.co.rosebankcollege.st10304152.taskmaster.data.TaskRepositoryOffline
import za.co.rosebankcollege.st10304152.taskmaster.ui.adapter.TaskAdapter
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import za.co.rosebankcollege.st10304152.taskmaster.data.NotificationRepository

class HomeFragment : Fragment() {
    
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var taskRepository: TaskRepositoryOffline
    private var isLoadingTasks = false
    private var isUpdatingTask = false
    private lateinit var notificationRepository: NotificationRepository
    private var notificationsBadge: BadgeDrawable? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize offline repository
        taskRepository = TaskRepositoryOffline(requireContext())
        notificationRepository = NotificationRepository(requireContext())
        
        setupToolbar()
        setupNotificationBadge()
        setupRecyclerView()
        setupFilterChips()
        setupFab()
        updateTaskCount()
        setupFragmentResultListener()
        setupWelcomeMessage()
        setupNetworkMonitoring()
    }
    
    
    override fun onStart() {
        super.onStart()
        // Only load tasks if we don't have any yet and we're not currently updating
        if (tasks.isEmpty() && !isUpdatingTask) {
            loadTasks()
        }
    }
    
    private fun setupToolbar() {
        val toolbar = view?.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_notifications -> {
                    findNavController().navigate(R.id.action_home_to_notifications)
                    true
                }
                R.id.action_settings -> {
                    findNavController().navigate(R.id.action_home_to_settings)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNotificationBadge() {
        val toolbar = view?.findViewById<MaterialToolbar>(R.id.toolbar) ?: return
        notificationsBadge = BadgeDrawable.create(requireContext()).apply {
            isVisible = false
            backgroundColor = requireContext().getColor(R.color.error)
            badgeTextColor = requireContext().getColor(R.color.white)
        }

        // Observe unread notifications and update badge
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                notificationRepository.getUnreadNotifications().collect { unreadList ->
                    val count = unreadList.size
                    notificationsBadge?.number = count
                    notificationsBadge?.isVisible = count > 0
                    try {
                        BadgeUtils.attachBadgeDrawable(notificationsBadge!!, toolbar, R.id.action_notifications)
                    } catch (_: Exception) {
                        // Ignore if attachment fails on older libs
                    }
                }
            } catch (_: Exception) { }
        }
    }
    
    private fun setupRecyclerView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.task_list)
        taskAdapter = TaskAdapter(
            tasks = tasks,
            onTaskClick = { task ->
                // Handle task click - navigate to details with task data
                val bundle = Bundle().apply {
                    putSerializable("task", task)
                }
                findNavController().navigate(R.id.action_home_to_details, bundle)
            },
            onTaskToggle = { task, isCompleted ->
                // Handle task completion toggle
                isUpdatingTask = true
                val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                    Log.e("HomeFragment", "Exception in task toggle", throwable)
                }
                viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            taskRepository.toggleTaskCompletion(task.id, isCompleted)
                        }
                        withContext(Dispatchers.Main) {
                            if (!isAdded || context == null || view == null) {
                                Log.w("HomeFragment", "Fragment not attached, skipping UI update")
                                return@withContext
                            }
                            
                            if (result.isSuccess) {
                                val taskIndex = tasks.indexOfFirst { it.id == task.id }
                                if (taskIndex != -1) {
                                    tasks[taskIndex] = task.copy(isCompleted = isCompleted)
                                    taskAdapter.updateTasks(tasks)
                                    updateTaskCount()
                                    refreshCurrentFilter()
                                    updateWelcomeMessage()
                                    Toast.makeText(context, "Task ${if (isCompleted) "completed" else "marked pending"}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.w("HomeFragment", "Task not found in list: ${task.id}")
                                }
                            } else {
                                Log.e("HomeFragment", "Failed to toggle task completion: ${result.exceptionOrNull()?.message}")
                                Toast.makeText(context, "Failed to update task: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Exception in task toggle", e)
                        withContext(Dispatchers.Main) {
                            if (isAdded && context != null) {
                                Toast.makeText(context, "Error updating task: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } finally {
                        isUpdatingTask = false
                    }
                }
            }
        )
        
        recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }
    
    private fun setupFilterChips() {
        val chipGroup = view?.findViewById<ChipGroup>(R.id.filter_chips)
        
        // Set up individual chip click listeners
        view?.findViewById<Chip>(R.id.chip_all)?.setOnClickListener {
            chipGroup?.check(R.id.chip_all)
            updateChipAppearance(R.id.chip_all)
            showAllTasks()
        }
        
        view?.findViewById<Chip>(R.id.chip_pending)?.setOnClickListener {
            chipGroup?.check(R.id.chip_pending)
            updateChipAppearance(R.id.chip_pending)
            showPendingTasks()
        }
        
        view?.findViewById<Chip>(R.id.chip_completed)?.setOnClickListener {
            chipGroup?.check(R.id.chip_completed)
            updateChipAppearance(R.id.chip_completed)
            showCompletedTasks()
        }
        
        view?.findViewById<Chip>(R.id.chip_today)?.setOnClickListener {
            chipGroup?.check(R.id.chip_today)
            updateChipAppearance(R.id.chip_today)
            showTodayTasks()
        }
        
        // Also keep the group listener as backup
        chipGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds.first()
                updateChipAppearance(chipId)
                when (chipId) {
                R.id.chip_all -> showAllTasks()
                R.id.chip_pending -> showPendingTasks()
                R.id.chip_completed -> showCompletedTasks()
                    R.id.chip_today -> showTodayTasks()
                }
            }
        }
        
        // Set initial selection and apply filter
        chipGroup?.check(R.id.chip_all)
        updateChipAppearance(R.id.chip_all)
        showAllTasks() // Apply the initial filter
    }
    
    private fun updateChipAppearance(selectedChipId: Int) {
        val chipGroup = view?.findViewById<ChipGroup>(R.id.filter_chips)
        val context = requireContext()
        
        // Reset all chips to unselected appearance with more subtle styling
        view?.findViewById<Chip>(R.id.chip_all)?.apply {
            setChipBackgroundColorResource(R.color.white)
            setTextColor(context.getColor(R.color.primary))
            chipStrokeColor = context.getColorStateList(R.color.primary)
            chipStrokeWidth = 2f
            elevation = 2f
            alpha = 0.7f
        }
        
        view?.findViewById<Chip>(R.id.chip_pending)?.apply {
            setChipBackgroundColorResource(R.color.white)
            setTextColor(context.getColor(R.color.primary))
            chipStrokeColor = context.getColorStateList(R.color.primary)
            chipStrokeWidth = 2f
            elevation = 2f
            alpha = 0.7f
        }
        
        view?.findViewById<Chip>(R.id.chip_completed)?.apply {
            setChipBackgroundColorResource(R.color.white)
            setTextColor(context.getColor(R.color.primary))
            chipStrokeColor = context.getColorStateList(R.color.primary)
            chipStrokeWidth = 2f
            elevation = 2f
            alpha = 0.7f
        }
        
        view?.findViewById<Chip>(R.id.chip_today)?.apply {
            setChipBackgroundColorResource(R.color.white)
            setTextColor(context.getColor(R.color.primary))
            chipStrokeColor = context.getColorStateList(R.color.primary)
            chipStrokeWidth = 2f
            elevation = 2f
            alpha = 0.7f
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
                    // Add a subtle animation effect
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            }
            R.id.chip_pending -> {
                view?.findViewById<Chip>(R.id.chip_pending)?.apply {
                    setChipBackgroundColorResource(R.color.priority_medium)
                    setTextColor(context.getColor(R.color.white))
                    chipStrokeWidth = 0f
                    elevation = 8f
                    alpha = 1.0f
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            }
            R.id.chip_completed -> {
                view?.findViewById<Chip>(R.id.chip_completed)?.apply {
                    setChipBackgroundColorResource(R.color.priority_low)
                    setTextColor(context.getColor(R.color.white))
                    chipStrokeWidth = 0f
                    elevation = 8f
                    alpha = 1.0f
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            }
            R.id.chip_today -> {
                view?.findViewById<Chip>(R.id.chip_today)?.apply {
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
    
    private fun setupFab() {
        view?.findViewById<ExtendedFloatingActionButton>(R.id.add_task_fab)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addEdit)
        }
    }
    
    private fun showAllTasks() {
        if (tasks.isEmpty()) {
            showEmptyState("No tasks yet", "Tap the + button to create your first task")
        } else {
            hideEmptyState()
            taskAdapter.updateTasks(tasks)
        updateTaskCount()
        }
    }
    
    private fun showPendingTasks() {
        val pendingTasks = tasks.filter { !it.isCompleted }
        if (pendingTasks.isEmpty()) {
            showEmptyState("No pending tasks", "All your tasks are completed! Great job!")
        } else {
            hideEmptyState()
        taskAdapter.updateTasks(pendingTasks)
        updateTaskCount(pendingTasks.size)
        }
    }
    
    private fun showCompletedTasks() {
        val completedTasks = tasks.filter { it.isCompleted }
        if (completedTasks.isEmpty()) {
            showEmptyState(
                getString(R.string.no_completed_tasks),
                getString(R.string.complete_some_tasks)
            )
        } else {
            hideEmptyState()
        taskAdapter.updateTasks(completedTasks)
        updateTaskCount(completedTasks.size)
    }
    }
    
    private fun showTodayTasks() {
        val todayTasks = tasks.filter { it.dueDate == "Today" }
        if (todayTasks.isEmpty()) {
            showEmptyState("No tasks for today", "You're all set for today! Add some tasks if needed")
        } else {
            hideEmptyState()
            taskAdapter.updateTasks(todayTasks)
            updateTaskCount(todayTasks.size)
        }
    }
    
    private fun refreshCurrentFilter() {
        val chipGroup = view?.findViewById<ChipGroup>(R.id.filter_chips)
        val checkedChipId = chipGroup?.checkedChipId
        when (checkedChipId) {
            R.id.chip_all -> {
                updateChipAppearance(R.id.chip_all)
                showAllTasks()
            }
            R.id.chip_pending -> {
                updateChipAppearance(R.id.chip_pending)
                showPendingTasks()
            }
            R.id.chip_completed -> {
                updateChipAppearance(R.id.chip_completed)
                showCompletedTasks()
            }
            R.id.chip_today -> {
                updateChipAppearance(R.id.chip_today)
                showTodayTasks()
            }
            else -> {
                updateChipAppearance(R.id.chip_all)
                showAllTasks() // Default to all tasks
            }
        }
        updateWelcomeMessage() // Update welcome message with today's task count
    }
    
    private fun updateTaskCount(count: Int = tasks.size) {
        view?.findViewById<TextView>(R.id.task_count)?.text = getString(
            R.string.n_tasks_count,
            count,
            if (count != 1) "s" else ""
        )
    }
    
    private fun showEmptyState(title: String, subtitle: String) {
        val emptyState = view?.findViewById<LinearLayout>(R.id.empty_state)
        val emptyTitle = emptyState?.findViewById<TextView>(R.id.empty_title)
        val emptySubtitle = emptyState?.findViewById<TextView>(R.id.empty_subtitle)
        
        emptyTitle?.text = title
        emptySubtitle?.text = subtitle
        emptyState?.visibility = View.VISIBLE
        
        // Hide the task list
        view?.findViewById<RecyclerView>(R.id.task_list)?.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        val emptyState = view?.findViewById<LinearLayout>(R.id.empty_state)
        emptyState?.visibility = View.GONE
        
        // Show the task list
        view?.findViewById<RecyclerView>(R.id.task_list)?.visibility = View.VISIBLE
    }
    
    private fun setupWelcomeMessage() {
        updateWelcomeMessage()
    }
    
    private fun updateWelcomeMessage() {
        val welcomeText = view?.findViewById<TextView>(R.id.welcome_text)
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val displayName = user?.displayName ?: getString(R.string.user)
        welcomeText?.text = getString(R.string.welcome_back_name, displayName)
        
        // Update today's task count in the welcome card
        val todayTasksCount = tasks.count { it.dueDate == "Today" }
        val todayTaskCountText = view?.findViewById<TextView>(R.id.today_task_count)
        todayTaskCountText?.text = getString(
            R.string.you_have_n_tasks_today,
            todayTasksCount,
            if (todayTasksCount != 1) "s" else ""
        )
    }
    
    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("task_saved", this) { _, result ->
            val savedTask = result.getSerializable("saved_task") as? Task
            if (savedTask != null) {
                tasks.add(0, savedTask) // Add to beginning of list
                taskAdapter.updateTasks(tasks)
                updateTaskCount()
                saveTasksLocally() // Also save locally as backup
            }
        }
        
        parentFragmentManager.setFragmentResultListener("task_updated", this) { _, result ->
            val updatedTask = result.getSerializable("updated_task") as? Task
            if (updatedTask != null) {
                isUpdatingTask = true
                val index = tasks.indexOfFirst { it.id == updatedTask.id }
                if (index != -1) {
                    tasks[index] = updatedTask
                    saveTasksLocally()
                    refreshCurrentFilter() // Refresh the current filter
                }
                isUpdatingTask = false
            }
        }
        
        parentFragmentManager.setFragmentResultListener("task_deleted", this) { _, result ->
            val deletedTaskId = result.getString("deleted_task_id")
            if (deletedTaskId != null) {
                isUpdatingTask = true
                tasks.removeAll { it.id == deletedTaskId }
                saveTasksLocally()
                refreshCurrentFilter() // Refresh the current filter
                isUpdatingTask = false
            }
        }
    }
    
    private fun loadTasks() {
        if (isLoadingTasks) {
            return
        }
        
        isLoadingTasks = true
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("HomeFragment", "Exception in loadTasks", throwable)
            isLoadingTasks = false
        }
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            try {
                // First try to load from local database
                val localResult = withContext(Dispatchers.IO) {
                    taskRepository.getTasksSync()
                }
                
                withContext(Dispatchers.Main) {
                    if (!isAdded || context == null || view == null) {
                        isLoadingTasks = false
                        return@withContext
                    }
                    
                    if (localResult.isSuccess) {
                        val loadedTasks = localResult.getOrNull() ?: emptyList()
                        
                        // If local database is empty but we're online, download from Firebase
                        if (loadedTasks.isEmpty() && taskRepository.isOnline()) {
                            viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
                                try {
                                    val downloadResult = withContext(Dispatchers.IO) {
                                        taskRepository.downloadTasksFromFirebase()
                                    }
                                    if (downloadResult.isSuccess) {
                                        // Reload from local database after download
                                        val reloadResult = withContext(Dispatchers.IO) {
                                            taskRepository.getTasksSync()
                                        }
                                        withContext(Dispatchers.Main) {
                                            if (!isAdded || context == null || view == null) {
                                                isLoadingTasks = false
                                                return@withContext
                                            }
                                            
                                            if (reloadResult.isSuccess) {
                                                val reloadedTasks = reloadResult.getOrNull() ?: emptyList()
                                                tasks.clear()
                                                tasks.addAll(reloadedTasks)
                                                taskAdapter.updateTasks(tasks)
                                                updateTaskCount()
                                                refreshCurrentFilter()
                                                updateWelcomeMessage()
                                            }
                                            isLoadingTasks = false
                                        }
                                    } else {
                                        isLoadingTasks = false
                                    }
                                } catch (e: Exception) {
                                    // Handle download error silently
                                    Log.e("HomeFragment", "Error downloading tasks", e)
                                    isLoadingTasks = false
                                }
                            }
                        } else {
                            // Normal case: use local tasks
                            tasks.clear()
                            tasks.addAll(loadedTasks)
                            taskAdapter.updateTasks(tasks)
                            updateTaskCount()
                            refreshCurrentFilter()
                            updateWelcomeMessage()
                            isLoadingTasks = false
                        }
                    } else {
                        isLoadingTasks = false
                    }
                }
                
                // Then try to sync with Firebase if online (async, don't block)
                if (taskRepository.isOnline()) {
                    val syncExceptionHandler = CoroutineExceptionHandler { _, throwable ->
                        // Ignore cancellation exceptions
                        if (throwable !is kotlinx.coroutines.CancellationException) {
                            Log.e("HomeFragment", "Sync exception in handler", throwable)
                        }
                    }
                    viewLifecycleOwner.lifecycleScope.launch(syncExceptionHandler) {
                        try {
                            // Check if fragment is still attached before starting sync
                            if (!isAdded || context == null) {
                                return@launch
                            }
                            
                            // Only sync local changes to Firebase (upload)
                            val syncResult = withContext(Dispatchers.IO) {
                                taskRepository.syncOfflineChanges()
                            }
                            if (syncResult.isSuccess) {
                                Log.d("HomeFragment", "Local changes synced to Firebase")
                            } else {
                                Log.e("HomeFragment", "Sync failed: ${syncResult.exceptionOrNull()?.message}")
                            }
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            // Cancellation is expected when fragment is destroyed - ignore it
                        } catch (e: Exception) {
                            // Sync failed, but we still have local data
                            if (isAdded) {
                                Log.e("HomeFragment", "Sync exception", e)
                                withContext(Dispatchers.Main) {
                                    if (isAdded && context != null) {
                                        Toast.makeText(context, "Sync failed, showing offline data", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        if (isAdded && context != null) {
                            Toast.makeText(context, "Offline mode - showing local data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoadingTasks = false
            }
        }
    }
    
    private fun loadTasksFromLocal() {
        // Load tasks from SharedPreferences as fallback
        val sharedPreferences = requireContext().getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val tasksJson = sharedPreferences.getString("tasks_list", "[]")
        
        try {
            val gson = com.google.gson.Gson()
            val taskListType = object : com.google.gson.reflect.TypeToken<List<Task>>() {}.type
            val loadedTasks = gson.fromJson<List<Task>>(tasksJson, taskListType)
            
            tasks.clear()
            tasks.addAll(loadedTasks)
            taskAdapter.updateTasks(tasks)
            updateTaskCount()
            refreshCurrentFilter() // This will also update the welcome message
        } catch (e: Exception) {
            // If loading fails, start with empty list
            tasks.clear()
            taskAdapter.updateTasks(tasks)
            updateTaskCount()
            refreshCurrentFilter() // This will also update the welcome message
        }
    }
    
    
    private fun saveTasksLocally() {
        val sharedPreferences = requireContext().getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val gson = com.google.gson.Gson()
        val tasksJson = gson.toJson(tasks)
        sharedPreferences.edit().putString("tasks_list", tasksJson).apply()
    }
    
    /**
     * Setup network monitoring to show offline/online status
     */
    private fun setupNetworkMonitoring() {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            // Ignore cancellation exceptions - they're expected when fragment is destroyed
            if (throwable !is kotlinx.coroutines.CancellationException) {
                Log.e("HomeFragment", "Exception in network state monitoring", throwable)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            try {
                taskRepository.getNetworkState().collect { isOnline ->
                    withContext(Dispatchers.Main) {
                        // Only update if fragment is still attached
                        if (isAdded && view != null && context != null) {
                            updateNetworkStatus(isOnline)
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Cancellation is expected when fragment is destroyed - ignore it
            } catch (e: Exception) {
                if (isAdded) {
                    Log.e("HomeFragment", "Error collecting network state", e)
                }
            }
        }
    }
    
    /**
     * Update UI based on network status
     */
    private fun updateNetworkStatus(isOnline: Boolean) {
        // Check if fragment is still attached
        if (!isAdded || context == null) {
            Log.w("HomeFragment", "Fragment not attached, skipping network status update")
            return
        }
        
        // Update offline indicator visibility
        val offlineIndicator = view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.offline_indicator)
        offlineIndicator?.visibility = if (isOnline) View.GONE else View.VISIBLE
        
        if (isOnline) {
            // Try to sync when coming back online
            val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                // Ignore cancellation exceptions - they're expected when fragment is destroyed
                if (throwable !is kotlinx.coroutines.CancellationException) {
                    Log.e("HomeFragment", "Exception in sync", throwable)
                }
            }
            viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
                try {
                    // Check if fragment is still attached before starting sync
                    if (!isAdded || context == null) {
                        return@launch
                    }
                    
                    // Only sync local changes to Firebase (upload)
                    val syncResult = withContext(Dispatchers.IO) {
                        taskRepository.syncOfflineChanges()
                    }
                    withContext(Dispatchers.Main) {
                        if (!isAdded || context == null || view == null) return@withContext
                        
                        if (syncResult.isSuccess) {
                            Toast.makeText(context, "Back online - syncing changes", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Sync failed: ${syncResult.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // Cancellation is expected when fragment is destroyed - ignore it silently
                } catch (e: Exception) {
                    // Only log if fragment is still attached and it's not a cancellation
                    if (isAdded && e !is kotlinx.coroutines.CancellationException) {
                        Log.e("HomeFragment", "Network sync exception", e)
                        withContext(Dispatchers.Main) {
                            if (isAdded && context != null) {
                                Toast.makeText(context, "Sync failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        } else {
            if (isAdded && context != null) {
                Toast.makeText(context, "Offline mode", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


