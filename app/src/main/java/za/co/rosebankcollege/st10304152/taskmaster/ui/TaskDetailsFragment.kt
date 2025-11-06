package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import za.co.rosebankcollege.st10304152.taskmaster.R
import za.co.rosebankcollege.st10304152.taskmaster.data.Task
import za.co.rosebankcollege.st10304152.taskmaster.data.TaskRepositoryOffline
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailsFragment : Fragment() {
    
    private lateinit var task: Task
    private lateinit var taskRepository: TaskRepositoryOffline
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_task_details, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize offline repository
        taskRepository = TaskRepositoryOffline(requireContext())
        
        // Get task from arguments
        task = arguments?.getSerializable("task") as? Task ?: return
        
        setupToolbar()
        setupTaskData()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        view?.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            
            // Set up menu item click listener
            setOnMenuItemClickListener { menuItem ->
                handleMenuItemClick(menuItem)
            }
        }
    }
    
    private fun handleMenuItemClick(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_share -> {
                shareTask()
                true
            }
            R.id.action_duplicate -> {
                duplicateTask()
                true
            }
            R.id.action_export -> {
                exportTask()
                true
            }
            R.id.action_add_to_calendar -> {
                addTaskToCalendar()
                true
            }
            else -> false
        }
    }
    
    private fun setupTaskData() {
        // Set task title
        view?.findViewById<TextView>(R.id.task_title)?.text = task.title
        
        // Set task status
        val statusText = if (task.isCompleted) {
            getString(R.string.status_completed)
        } else {
            getString(R.string.status_in_progress)
        }
        view?.findViewById<TextView>(R.id.task_status)?.text = statusText
        
        // Set priority chip
        val priorityChip = view?.findViewById<Chip>(R.id.priority_chip)
        priorityChip?.text = getLocalizedPriority(task.priority)
        when (task.priority.lowercase()) {
            "high" -> priorityChip?.setChipBackgroundColorResource(R.color.priority_high)
            "medium" -> priorityChip?.setChipBackgroundColorResource(R.color.priority_medium)
            "low" -> priorityChip?.setChipBackgroundColorResource(R.color.priority_low)
        }
        
        // Set due date
        val dueDateText = "${task.dueDate}, ${task.dueTime}"
        view?.findViewById<TextView>(R.id.due_date)?.text = dueDateText
        
        // Set created date
        val createdDate = formatCreatedDate(task.createdAt)
        view?.findViewById<TextView>(R.id.created_date)?.text = createdDate
        
        // Set category (using priority as category for now)
        view?.findViewById<TextView>(R.id.category)?.text = getLocalizedPriority(task.priority)
        
        // Set description
        view?.findViewById<TextView>(R.id.description)?.text = task.description
        
        // Update button visibility based on completion status
        val markPendingButton = view?.findViewById<MaterialButton>(R.id.mark_pending_button)
        val markCompleteButton = view?.findViewById<MaterialButton>(R.id.mark_complete_button)
        
        if (task.isCompleted) {
            // Task is completed - show "Mark Pending" and "Mark Incomplete" buttons
            markPendingButton?.visibility = View.VISIBLE
            markCompleteButton?.text = getString(R.string.mark_incomplete)
        } else {
            // Task is pending - show "Mark Complete" button, hide "Mark Pending"
            markPendingButton?.visibility = View.GONE
            markCompleteButton?.text = getString(R.string.mark_complete)
        }
    }
    
    private fun setupClickListeners() {
        // Mark pending button
        view?.findViewById<MaterialButton>(R.id.mark_pending_button)?.setOnClickListener {
            toggleTaskCompletion(false)
        }
        
        // Mark complete/incomplete button
        view?.findViewById<MaterialButton>(R.id.mark_complete_button)?.setOnClickListener {
            val newStatus = !task.isCompleted
            toggleTaskCompletion(newStatus)
        }
        
        // Edit button
        view?.findViewById<MaterialButton>(R.id.edit_button)?.setOnClickListener {
            navigateToEditTask()
        }
        
        // Delete button
        view?.findViewById<MaterialButton>(R.id.delete_button)?.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    
    private fun toggleTaskCompletion(isCompleted: Boolean) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("TaskDetailsFragment", "Exception in task toggle", throwable)
            // Error already handled in try-catch below
        }
        
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            try {
                val result = taskRepository.toggleTaskCompletion(task.id, isCompleted)
                withContext(Dispatchers.Main) {
                    if (!isAdded || context == null || view == null) {
                        Log.w("TaskDetailsFragment", "Fragment not attached, skipping UI update")
                        return@withContext
                    }
                    
                    if (result.isSuccess) {
                        // Update the task object
                        task = task.copy(isCompleted = isCompleted)
                        // Update the UI
                        setupTaskData()
                        
                        // Notify HomeFragment about the change
                        val result = Bundle().apply {
                            putSerializable("updated_task", task)
                        }
                        try {
                            parentFragmentManager.setFragmentResult("task_updated", result)
                        } catch (e: Exception) {
                            Log.e("TaskDetailsFragment", "Failed to set fragment result", e)
                        }
                        
                        Toast.makeText(context, if (isCompleted) "Task marked as complete" else "Task marked as pending", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("TaskDetailsFragment", "Failed to toggle task completion: ${result.exceptionOrNull()?.message}")
                        Toast.makeText(context, "Failed to update task: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("TaskDetailsFragment", "Exception in task toggle", e)
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(context, "Error updating task: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        if (!isAdded || context == null) {
            return
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_task_title))
            .setMessage(getString(R.string.delete_task_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteTask()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun getLocalizedPriority(priority: String): String {
        return when (priority.lowercase()) {
            "high" -> getString(R.string.high)
            "medium" -> getString(R.string.medium)
            "low" -> getString(R.string.low)
            else -> priority
        }
    }
    
    private fun deleteTask() {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("TaskDetailsFragment", "Exception in delete task", throwable)
        }
        
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            try {
                val taskId = task.id
                val result = withContext(Dispatchers.IO) {
                    taskRepository.deleteTask(taskId)
                }
                
                withContext(Dispatchers.Main) {
                    // Check if fragment is still attached before updating UI
                    if (!isAdded || context == null || view == null) {
                        Log.w("TaskDetailsFragment", "Fragment not attached, skipping UI update")
                        return@withContext
                    }
                    
                    if (result.isSuccess) {
                        // Notify HomeFragment about the deletion
                        val resultBundle = Bundle().apply {
                            putString("deleted_task_id", taskId)
                        }
                        
                        try {
                            parentFragmentManager.setFragmentResult("task_deleted", resultBundle)
                        } catch (e: Exception) {
                            Log.e("TaskDetailsFragment", "Failed to set fragment result", e)
                        }
                        
                        Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                        
                        try {
                            findNavController().navigateUp()
                        } catch (e: Exception) {
                            Log.e("TaskDetailsFragment", "Failed to navigate up", e)
                            // Navigation might fail if fragment is detached, try activity finish as fallback
                            activity?.finish()
                        }
                    } else {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete task"
                        Log.e("TaskDetailsFragment", "Delete task failed: $errorMessage")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("TaskDetailsFragment", "Exception in delete task", e)
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(context, "Error deleting task: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun formatCreatedDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val days = diff / (24 * 60 * 60 * 1000)
        
        return when {
            days == 0L -> getString(R.string.today)
            days == 1L -> getString(R.string.yesterday)
            days < 7L -> getString(R.string.n_days_ago, days.toInt())
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }
    
    private fun navigateToEditTask() {
        // Pass the task data to the edit fragment
        val bundle = Bundle().apply {
            putSerializable("task_to_edit", task)
        }
        findNavController().navigate(R.id.action_taskDetails_to_addEdit, bundle)
    }
    
    private fun shareTask() {
        if (!isAdded || context == null) {
            return
        }
        
        val shareText = buildString {
            append(getString(R.string.share_task_title, task.title))
            append("\n\n")
            if (task.description.isNotBlank()) {
                append(getString(R.string.share_task_description, task.description))
                append("\n\n")
            }
            append(getString(R.string.share_task_priority, getLocalizedPriority(task.priority)))
            append("\n")
            append(getString(R.string.share_task_due_date, task.dueDate, task.dueTime))
            append("\n")
            val statusText = if (task.isCompleted) {
                getString(R.string.status_completed)
            } else {
                getString(R.string.status_in_progress)
            }
            append(getString(R.string.share_task_status, statusText))
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_task_title, task.title))
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_task)))
    }
    
    private fun duplicateTask() {
        if (!isAdded || context == null) {
            return
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Duplicate Task")
            .setMessage("Create a copy of this task?")
            .setPositiveButton("Duplicate") { _, _ ->
                performDuplicateTask()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performDuplicateTask() {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("TaskDetailsFragment", "Exception in duplicate task", throwable)
        }
        
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            try {
                // Create a new task with same data but new ID and reset completion status
                val duplicatedTask = task.copy(
                    id = "", // Will be generated by repository
                    isCompleted = false,
                    createdAt = System.currentTimeMillis()
                )
                
                val result = withContext(Dispatchers.IO) {
                    taskRepository.addTask(duplicatedTask)
                }
                
                withContext(Dispatchers.Main) {
                    if (!isAdded || context == null || view == null) {
                        Log.w("TaskDetailsFragment", "Fragment not attached, skipping UI update")
                        return@withContext
                    }
                    
                    if (result.isSuccess) {
                        Toast.makeText(context, "Task duplicated successfully", Toast.LENGTH_SHORT).show()
                        // Navigate back to home to see the duplicated task
                        findNavController().navigateUp()
                    } else {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Failed to duplicate task"
                        Log.e("TaskDetailsFragment", "Duplicate task failed: $errorMessage")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("TaskDetailsFragment", "Exception in duplicate task", e)
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(context, "Error duplicating task: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun exportTask() {
        if (!isAdded || context == null) {
            return
        }
        
        val exportText = buildString {
            append(getString(R.string.share_task_title, task.title))
            append("\n")
            append("=".repeat(50))
            append("\n\n")
            append(getString(R.string.task_title))
            append(": ${task.title}\n\n")
            
            if (task.description.isNotBlank()) {
                append(getString(R.string.share_task_description, task.description))
                append("\n\n")
            }
            
            append(getString(R.string.share_task_priority, getLocalizedPriority(task.priority)))
            append("\n")
            append(getString(R.string.share_task_due_date, task.dueDate, task.dueTime))
            append("\n")
            val statusText = if (task.isCompleted) {
                getString(R.string.status_completed)
            } else {
                getString(R.string.status_in_progress)
            }
            append(getString(R.string.share_task_status, statusText))
            append("\n")
            append(getString(R.string.created))
            append(": ${formatCreatedDate(task.createdAt)}\n")
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, exportText)
            putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.app_name)} - ${task.title}")
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_task)))
    }
    
    private fun addTaskToCalendar() {
        if (!isAdded || context == null) {
            return
        }
        
        try {
            // Create an intent to add event to calendar
            val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
                type = "vnd.android.cursor.item/event"
                putExtra(android.provider.CalendarContract.Events.TITLE, task.title)
                putExtra(android.provider.CalendarContract.Events.DESCRIPTION, task.description)
                putExtra(android.provider.CalendarContract.Events.ALL_DAY, false)
                
                // Try to parse the date and time
                // For now, set reminder for the due date/time
                // Note: This is a simplified implementation
                // You might want to add proper date parsing based on your date format
            }
            
            if (calendarIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(calendarIntent)
                Toast.makeText(context, "Opening calendar...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No calendar app found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("TaskDetailsFragment", "Error opening calendar", e)
            Toast.makeText(context, "Error opening calendar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}


