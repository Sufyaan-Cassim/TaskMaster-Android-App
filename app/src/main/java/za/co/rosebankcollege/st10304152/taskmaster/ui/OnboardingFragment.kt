package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import za.co.rosebankcollege.st10304152.taskmaster.R

class OnboardingFragment : Fragment() {
    
    private var pagePosition: Int = 0
    
    companion object {
        private const val ARG_POSITION = "position"
        
        fun newInstance(position: Int): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pagePosition = it.getInt(ARG_POSITION, 0)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val titleText = view.findViewById<TextView>(R.id.titleText)
        val descriptionText = view.findViewById<TextView>(R.id.descriptionText)
        
        when (pagePosition) {
            0 -> {
                // Welcome Screen
                imageView.setImageResource(R.drawable.ic_welcome)
                titleText.text = "Welcome to TaskMaster"
                descriptionText.text = "Your personal productivity companion that helps you stay organized and focused on what matters most."
            }
            1 -> {
                // Task Management
                imageView.setImageResource(R.drawable.ic_task_management)
                titleText.text = "Create & Organize Tasks"
                descriptionText.text = "Easily create tasks, set priorities, due dates, and reminders. Keep track of everything in one place."
            }
            2 -> {
                // Smart Notifications
                imageView.setImageResource(R.drawable.ic_notifications)
                titleText.text = "Never Miss Important Tasks"
                descriptionText.text = "Get smart notifications about deadlines and reminders. Stay on top of your commitments."
            }
            3 -> {
                // Offline Sync
                imageView.setImageResource(R.drawable.ic_sync)
                titleText.text = getString(R.string.work_anywhere_anytime)
                descriptionText.text = getString(R.string.work_anywhere_anytime_desc)
            }
        }
    }
}
