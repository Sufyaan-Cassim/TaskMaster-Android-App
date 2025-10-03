package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.rosebankcollege.st10304152.taskmaster.R
import za.co.rosebankcollege.st10304152.taskmaster.data.Task
import za.co.rosebankcollege.st10304152.taskmaster.data.TaskRepository
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailsFragment : Fragment() {
    
    private lateinit var task: Task
    private val taskRepository = TaskRepository()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_task_details, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
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
        }
    }
    
    private fun setupTaskData() {
        // Set task title
        view?.findViewById<TextView>(R.id.task_title)?.text = task.title
        
        // Set task status
        val statusText = if (task.isCompleted) "Completed" else "In Progress"
        view?.findViewById<TextView>(R.id.task_status)?.text = statusText
        
        // Set priority chip
        val priorityChip = view?.findViewById<Chip>(R.id.priority_chip)
        priorityChip?.text = task.priority
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
        view?.findViewById<TextView>(R.id.category)?.text = task.priority
        
        // Set description
        view?.findViewById<TextView>(R.id.description)?.text = task.description
        
        // Update button visibility based on completion status
        val markPendingButton = view?.findViewById<MaterialButton>(R.id.mark_pending_button)
        val markCompleteButton = view?.findViewById<MaterialButton>(R.id.mark_complete_button)
        
        if (task.isCompleted) {
            // Task is completed - show "Mark Pending" and "Mark Incomplete" buttons
            markPendingButton?.visibility = View.VISIBLE
            markCompleteButton?.text = "Mark Incomplete"
        } else {
            // Task is pending - show "Mark Complete" button, hide "Mark Pending"
            markPendingButton?.visibility = View.GONE
            markCompleteButton?.text = "Mark Complete"
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
            deleteTask()
        }
    }
    
    private fun toggleTaskCompletion(isCompleted: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = taskRepository.toggleTaskCompletion(task.id, isCompleted)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    // Update the task object
                    task = task.copy(isCompleted = isCompleted)
                    // Update the UI
                    setupTaskData()
                    
                    // Notify HomeFragment about the change
                    val result = Bundle().apply {
                        putSerializable("updated_task", task)
                    }
                    parentFragmentManager.setFragmentResult("task_updated", result)
                    
                    Toast.makeText(context, if (isCompleted) "Task marked as complete" else "Task marked as pending", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun deleteTask() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = taskRepository.deleteTask(task.id)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    // Notify HomeFragment about the deletion
                    val result = Bundle().apply {
                        putString("deleted_task_id", task.id)
                    }
                    parentFragmentManager.setFragmentResult("task_deleted", result)
                    
                    Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun formatCreatedDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val days = diff / (24 * 60 * 60 * 1000)
        
        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7L -> "$days days ago"
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
}


