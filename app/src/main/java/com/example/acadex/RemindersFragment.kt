package com.example.acadex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.acadex.databinding.FragmentRemindersBinding
import com.example.acadex.databinding.ItemTaskCardBinding
import com.example.acadex.databinding.ItemTaskRowBinding

class RemindersFragment : Fragment() {
    private var _b: FragmentRemindersBinding? = null
    private val b get() = _b!!
    private lateinit var db: AcadexDb

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentRemindersBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = AcadexDb(requireContext())
        b.header.tvHeaderTitle.text = "Tasks & Reminders"
        b.header.btnBack.setOnClickListener { (requireActivity() as MainActivity).goHome() }
        load()
    }

    private fun load() {
        val inflater = LayoutInflater.from(requireContext())
        val tasks = db.getTasks(true)
        b.cardContainer.removeAllViews()
        b.scheduleContainer.removeAllViews()
        val featured = tasks.take(3)      // navy cards: next 3 deadlines
        val rest = tasks.drop(3)          // everything after that

        featured.forEach { t ->
            val card = ItemTaskCardBinding.inflate(inflater, b.cardContainer, false)
            card.tvTitle.text = t.subject
            card.tvDesc.text = t.description.ifBlank { "No description" }
            card.tvDeadline.text = "Deadline: ${Fmt.dateLong(t.deadline)}"
            card.root.setOnClickListener { Dialogs.taskDetails(requireContext(), t, db) { load() } }
            b.cardContainer.addView(card.root)
        }
        rest.forEach { t ->
            val row = ItemTaskRowBinding.inflate(inflater, b.scheduleContainer, false)
            row.tvTitle.text = t.subject
            row.tvDesc.text = t.description.ifBlank { "No description" }
            row.tvDate.text = Fmt.dateShort(t.deadline)
            row.tvTime.text = Fmt.time(t.deadline)
            row.root.setOnClickListener { Dialogs.taskDetails(requireContext(), t, db) { load() } }
            b.scheduleContainer.addView(row.root)
        }
        b.tvScheduleCount.text = "${rest.size} tasks"
        b.tvEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
