package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import za.co.rosebankcollege.st10304152.taskmaster.R
import za.co.rosebankcollege.st10304152.taskmaster.data.Task
import za.co.rosebankcollege.st10304152.taskmaster.data.TaskRepositoryOffline
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import java.text.SimpleDateFormat
import java.util.*

class AddEditTaskFragment : Fragment() {
    
    private lateinit var titleInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var priorityChips: ChipGroup
    private lateinit var dueDateText: android.widget.TextView
    private lateinit var dueTimeText: android.widget.TextView
    private lateinit var remindersSwitch: MaterialSwitch
    private lateinit var reminderTimeLayout: View
    private lateinit var reminderTimeText: android.widget.TextView
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    
    private var selectedPriority = "Medium"
    private var selectedDate = Calendar.getInstance()
    private var selectedTime = Calendar.getInstance()
    private var selectedReminderTime = "15 minutes before"
    private lateinit var taskRepository: TaskRepositoryOffline
    private var taskToEdit: Task? = null
    private var isEditing = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_edit_task, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize offline repository
        taskRepository = TaskRepositoryOffline(requireContext())
        
        // Check if we're editing an existing task
        taskToEdit = arguments?.getSerializable("task_to_edit") as? Task
        isEditing = taskToEdit != null
        
        initializeViews(view)
        setupToolbar()
        setupClickListeners()
        setupReminderSwitch()
        
        if (isEditing) {
            populateFieldsForEditing()
        }
    }
    
    private fun initializeViews(view: View) {
        titleInput = view.findViewById(R.id.title_input)
        descriptionInput = view.findViewById(R.id.description_input)
        priorityChips = view.findViewById(R.id.priority_chips)
        dueDateText = view.findViewById(R.id.due_date_text)
        dueTimeText = view.findViewById(R.id.due_time_text)
        remindersSwitch = view.findViewById(R.id.reminders_switch)
        reminderTimeLayout = view.findViewById(R.id.reminder_time_layout)
        reminderTimeText = view.findViewById(R.id.reminder_time_text)
        saveButton = view.findViewById(R.id.save_button)
        cancelButton = view.findViewById(R.id.cancel_button)
        
        // Set default time to 9:00 AM
        selectedTime.set(Calendar.HOUR_OF_DAY, 9)
        selectedTime.set(Calendar.MINUTE, 0)
        updateTimeDisplay()
        
        // Set default reminder time display
        reminderTimeText.text = getLocalizedReminderTime(selectedReminderTime)
    }
    
    private fun getLocalizedReminderTime(reminderTime: String): String {
        return when (reminderTime) {
            "5 minutes before" -> getString(R.string.reminder_5_min_before)
            "15 minutes before" -> getString(R.string.reminder_15_min_before)
            "30 minutes before" -> getString(R.string.reminder_30_min_before)
            "1 hour before" -> getString(R.string.reminder_1_hour_before)
            "2 hours before" -> getString(R.string.reminder_2_hours_before)
            else -> reminderTime
        }
    }
    
    private fun setupToolbar() {
        val toolbar = view?.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupClickListeners() {
        // Due date picker - find the clickable LinearLayout parent
        val dueDateLayout = view?.findViewById<View>(R.id.due_date_text)?.parent as? View
        dueDateLayout?.setOnClickListener {
            showDatePicker()
        }
        
        // Due time picker - find the clickable LinearLayout parent
        val dueTimeLayout = view?.findViewById<View>(R.id.due_time_text)?.parent as? View
        dueTimeLayout?.setOnClickListener {
            showTimePicker()
        }
        
        // Reminder time picker
        reminderTimeLayout.setOnClickListener {
            showReminderTimePicker()
        }
        
        // Priority selection
        priorityChips.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            selectedPriority = when (chip?.id) {
                R.id.priority_low -> getString(R.string.low)
                R.id.priority_medium -> getString(R.string.medium)
                R.id.priority_high -> getString(R.string.high)
                else -> getString(R.string.medium)
            }
        }
        
        // Set default priority
        view?.findViewById<Chip>(R.id.priority_medium)?.isChecked = true
        
        // Save button
        saveButton.setOnClickListener {
            saveTask()
        }
        
        // Cancel button
        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupReminderSwitch() {
        remindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            reminderTimeLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }
    
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
            },
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }
    
    private fun showReminderTimePicker() {
        val reminderOptions = arrayOf(
            getString(R.string.reminder_5_min_before),
            getString(R.string.reminder_15_min_before),
            getString(R.string.reminder_30_min_before),
            getString(R.string.reminder_1_hour_before),
            getString(R.string.reminder_2_hours_before)
        )
        
        // Map localized strings back to original format for storage
        val originalOptions = listOf(
            "5 minutes before",
            "15 minutes before",
            "30 minutes before",
            "1 hour before",
            "2 hours before"
        )
        
        val currentIndex = originalOptions.indexOf(selectedReminderTime)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.reminder_time_title))
            .setSingleChoiceItems(reminderOptions, currentIndex.coerceAtLeast(0)) { dialog, which ->
                selectedReminderTime = originalOptions[which]
                reminderTimeText.text = reminderOptions[which]
                dialog.dismiss()
            }
            .show()
    }
    
    private fun updateDateDisplay() {
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateString = when {
            selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
            selectedDate.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) -> "Tomorrow"
            else -> dateFormat.format(selectedDate.time)
        }
        
        dueDateText.text = dateString
    }
    
    private fun updateTimeDisplay() {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        dueTimeText.text = timeFormat.format(selectedTime.time)
    }
    
    private fun saveTask() {
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        
        if (title.isEmpty()) {
            titleInput.error = getString(R.string.title_required)
            return
        }
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dueDate = when {
            selectedDate.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) -> "Today"
            selectedDate.get(Calendar.YEAR) == Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_YEAR) -> "Tomorrow"
            else -> dateFormat.format(selectedDate.time)
        }
        
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dueTime = timeFormat.format(selectedTime.time)
        
        val task = if (isEditing) {
            // Update existing task
            taskToEdit!!.copy(
                title = title,
                description = description,
                dueDate = dueDate,
                priority = selectedPriority,
                dueTime = dueTime,
                reminderEnabled = remindersSwitch.isChecked,
                reminderTime = selectedReminderTime
            )
        } else {
            // Create new task
            Task(
                title = title,
                description = description,
                isCompleted = false,
                dueDate = dueDate,
                priority = selectedPriority,
                dueTime = dueTime,
                reminderEnabled = remindersSwitch.isChecked,
                reminderTime = selectedReminderTime
            )
        }
        
        // Save task to Firestore
        saveTaskToFirestore(task)
    }
    
    private fun saveTaskToFirestore(task: Task) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            android.util.Log.e("AddEditTaskFragment", "Exception in save task", throwable)
        }
        
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            try {
                val result = withContext(Dispatchers.IO) {
                    if (isEditing) {
                        taskRepository.updateTask(task)
                    } else {
                        taskRepository.addTask(task)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    // Check if fragment is still attached before updating UI
                    if (!isAdded || context == null || view == null) {
                        android.util.Log.w("AddEditTaskFragment", "Fragment not attached, skipping UI update")
                        return@withContext
                    }
                    
                    if (result.isSuccess) {
                        val savedTask = result.getOrNull()
                        if (savedTask != null) {
                            // Pass the saved/updated task back to the HomeFragment
                            val resultBundle = Bundle().apply {
                                putSerializable(if (isEditing) "updated_task" else "saved_task", savedTask)
                            }
                            
                            try {
                                parentFragmentManager.setFragmentResult(
                                    if (isEditing) "task_updated" else "task_saved", 
                                    resultBundle
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("AddEditTaskFragment", "Failed to set fragment result", e)
                            }
                            
                            Toast.makeText(
                                context, 
                                if (isEditing) "Task updated successfully!" else "Task saved successfully!", 
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            try {
                                findNavController().navigateUp()
                            } catch (e: Exception) {
                                android.util.Log.e("AddEditTaskFragment", "Failed to navigate up", e)
                                // Navigation might fail if fragment is detached
                                activity?.finish()
                            }
                        }
                    } else {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                        android.util.Log.e("AddEditTaskFragment", "Save task failed: $errorMessage")
                        Toast.makeText(
                            context, 
                            getString(if (isEditing) R.string.failed_to_update_task else R.string.failed_to_save_task, errorMessage), 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AddEditTaskFragment", "Exception in save task", e)
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(context, getString(R.string.error_saving_task, e.message ?: getString(R.string.unknown_error)), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun populateFieldsForEditing() {
        taskToEdit?.let { task ->
            // Populate title and description
            titleInput.setText(task.title)
            descriptionInput.setText(task.description)
            
            // Set priority - map localized priority back to English for storage
            selectedPriority = when (task.priority.lowercase()) {
                getString(R.string.high).lowercase() -> getString(R.string.high)
                getString(R.string.medium).lowercase() -> getString(R.string.medium)
                getString(R.string.low).lowercase() -> getString(R.string.low)
                "high" -> getString(R.string.high)
                "medium" -> getString(R.string.medium)
                "low" -> getString(R.string.low)
                else -> getString(R.string.medium)
            }
            when (selectedPriority.lowercase()) {
                getString(R.string.high).lowercase(), "high" -> priorityChips.check(R.id.priority_high)
                getString(R.string.medium).lowercase(), "medium" -> priorityChips.check(R.id.priority_medium)
                getString(R.string.low).lowercase(), "low" -> priorityChips.check(R.id.priority_low)
            }
            
            // Set due date
            if (task.dueDate.isNotEmpty()) {
                dueDateText.text = task.dueDate
            }
            
            // Set due time
            if (task.dueTime.isNotEmpty()) {
                dueTimeText.text = task.dueTime
            }
            
            // Set reminder settings
            remindersSwitch.isChecked = task.reminderEnabled
            if (task.reminderTime.isNotEmpty()) {
                selectedReminderTime = task.reminderTime
                // Display localized version
                reminderTimeText.text = getLocalizedReminderTime(task.reminderTime)
            }
            
            // Update save button text
            saveButton.text = getString(R.string.update_task)
        }
    }
}


