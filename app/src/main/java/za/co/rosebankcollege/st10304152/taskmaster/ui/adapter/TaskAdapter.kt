package za.co.rosebankcollege.st10304152.taskmaster.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import za.co.rosebankcollege.st10304152.taskmaster.R
import za.co.rosebankcollege.st10304152.taskmaster.data.Task

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskToggle: (Task, Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.task_title)
        val description: TextView = itemView.findViewById(R.id.task_description)
        val dueDate: TextView = itemView.findViewById(R.id.task_due_date)
        val category: TextView = itemView.findViewById(R.id.task_category)
        val checkbox: MaterialCheckBox = itemView.findViewById(R.id.task_checkbox)
        val priorityIndicator: View = itemView.findViewById(R.id.priority_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        val context = holder.itemView.context
        
        holder.title.text = task.title
        holder.description.text = task.description
        
        // Localize dueDate display (e.g., "Today" -> localized version)
        val localizedDueDate = when (task.dueDate.lowercase()) {
            "today" -> context.getString(R.string.today)
            "tomorrow" -> context.getString(R.string.tomorrow)
            else -> task.dueDate
        }
        holder.dueDate.text = localizedDueDate
        
        // Set category (using priority as category for now, localized)
        val localizedPriority = when (task.priority.lowercase()) {
            "high" -> context.getString(R.string.high)
            "medium" -> context.getString(R.string.medium)
            "low" -> context.getString(R.string.low)
            else -> task.priority
        }
        holder.category.text = localizedPriority
        
        // Set priority indicator color
        val priorityColorRes = when (task.priority.lowercase()) {
            "high" -> R.color.priority_high
            "medium" -> R.color.priority_medium
            "low" -> R.color.priority_low
            else -> R.color.priority_medium
        }
        holder.priorityIndicator.setBackgroundColor(
            holder.itemView.context.resources.getColor(priorityColorRes, null)
        )
        
        // Strike through text if completed
        if (task.isCompleted) {
            holder.title.paintFlags = holder.title.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.description.paintFlags = holder.description.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.title.paintFlags = holder.title.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.description.paintFlags = holder.description.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        
        // Clear any existing listeners to prevent multiple calls
        holder.checkbox.setOnCheckedChangeListener(null)
        
        // Set the checkbox state
        holder.checkbox.isChecked = task.isCompleted
        
        // Set the listener after setting the state
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != task.isCompleted) {
                onTaskToggle(task, isChecked)
            }
        }
        
        // Handle item click
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
