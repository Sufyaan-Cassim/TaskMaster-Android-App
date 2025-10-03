package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.content.Context
import android.os.Bundle
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.rosebankcollege.st10304152.taskmaster.R
import za.co.rosebankcollege.st10304152.taskmaster.data.Task
import za.co.rosebankcollege.st10304152.taskmaster.data.TaskRepository
import za.co.rosebankcollege.st10304152.taskmaster.ui.adapter.TaskAdapter

class HomeFragment : Fragment() {
    
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private val taskRepository = TaskRepository()
    private var isLoadingTasks = false
    private var isUpdatingTask = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupFilterChips()
        setupFab()
        updateTaskCount()
        setupFragmentResultListener()
        setupWelcomeMessage()
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
                CoroutineScope(Dispatchers.IO).launch {
                    val result = taskRepository.toggleTaskCompletion(task.id, isCompleted)
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            val taskIndex = tasks.indexOfFirst { it.id == task.id }
                            if (taskIndex != -1) {
                                tasks[taskIndex] = task.copy(isCompleted = isCompleted)
                                saveTasksLocally()
                                refreshCurrentFilter() // This will update the adapter and count
                                Toast.makeText(context, "Task ${if (isCompleted) "completed" else "marked pending"}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to update task: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
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
            showEmptyState("No completed tasks", "Complete some tasks to see them here")
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
        view?.findViewById<TextView>(R.id.task_count)?.text = "$count task${if (count != 1) "s" else ""}"
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
        val displayName = user?.displayName ?: "User"
        welcomeText?.text = "Welcome back, $displayName!"
        
        // Update today's task count in the welcome card
        val todayTasksCount = tasks.count { it.dueDate == "Today" }
        val todayTaskCountText = view?.findViewById<TextView>(R.id.today_task_count)
        todayTaskCountText?.text = "You have $todayTasksCount task${if (todayTasksCount != 1) "s" else ""} for today"
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = taskRepository.getTasks()
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        val loadedTasks = result.getOrNull() ?: emptyList()
                        tasks.clear()
                        tasks.addAll(loadedTasks)
                        taskAdapter.updateTasks(tasks)
                        updateTaskCount()
                        // Refresh the current filter after loading
                        refreshCurrentFilter()
                    } else {
                        // Only show offline message if there's a real network issue
                        val exception = result.exceptionOrNull()
                        if (exception?.message?.contains("network") == true || 
                            exception?.message?.contains("timeout") == true) {
                            loadTasksFromLocal()
                            Toast.makeText(context, "Using offline data", Toast.LENGTH_SHORT).show()
                        } else {
                            // For other errors, try local first, then show error
                            loadTasksFromLocal()
                            if (tasks.isEmpty()) {
                                Toast.makeText(context, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadTasksFromLocal()
                    if (tasks.isEmpty()) {
                        Toast.makeText(context, "Failed to load tasks: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
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
}


