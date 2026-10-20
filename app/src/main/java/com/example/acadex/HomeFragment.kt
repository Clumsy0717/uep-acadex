package com.example.acadex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.acadex.databinding.FragmentHomeBinding
import com.example.acadex.databinding.ItemClassBinding
import com.example.acadex.databinding.ItemReminderBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private lateinit var db: AcadexDb
    private var isSpeedDialOpen = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = AcadexDb(requireContext())
        setupSpeedDial()
        load()
    }

    private fun setupSpeedDial() {
        val nav = requireActivity() as MainActivity

        b.fabQuickLinks.setOnClickListener {
            toggleSpeedDial()
        }

        b.fabOverlay.setOnClickListener {
            if (isSpeedDialOpen) closeSpeedDial()
        }

        val closeAndRun = { action: () -> Unit ->
            closeSpeedDial()
            action()
        }

        b.itemAddTask.setOnClickListener { closeAndRun { nav.navigateTo(R.id.nav_planner) } }
        b.fabAddTask.setOnClickListener { closeAndRun { nav.navigateTo(R.id.nav_planner) } }

        b.itemAddSubject.setOnClickListener { closeAndRun { Dialogs.addClass(requireContext(), db) { load() } } }
        b.fabAddSubject.setOnClickListener { closeAndRun { Dialogs.addClass(requireContext(), db) { load() } } }

        b.itemAddReminder.setOnClickListener { closeAndRun { Dialogs.addReminder(requireContext(), db) { load() } } }
        b.fabAddReminder.setOnClickListener { closeAndRun { Dialogs.addReminder(requireContext(), db) { load() } } }

        b.itemCampusGuide.setOnClickListener { closeAndRun { nav.navigateTo(R.id.nav_map) } }
        b.fabCampusGuide.setOnClickListener { closeAndRun { nav.navigateTo(R.id.nav_map) } }
    }

    private fun toggleSpeedDial() {
        if (isSpeedDialOpen) closeSpeedDial() else openSpeedDial()
    }

    private fun openSpeedDial() {
        isSpeedDialOpen = true
        b.speedDialMenu.visibility = View.VISIBLE
        b.speedDialMenu.alpha = 0f
        b.speedDialMenu.translationY = 40f
        b.speedDialMenu.animate().alpha(1f).translationY(0f).setDuration(200).start()

        b.fabOverlay.visibility = View.VISIBLE
        b.fabOverlay.alpha = 0f
        b.fabOverlay.animate().alpha(1f).setDuration(200).start()

        b.fabQuickLinks.animate().rotation(45f).setDuration(200).start()
    }

    private fun closeSpeedDial() {
        isSpeedDialOpen = false
        b.speedDialMenu.animate().alpha(0f).translationY(40f).setDuration(150).withEndAction {
            _b?.speedDialMenu?.visibility = View.GONE
        }.start()

        b.fabOverlay.animate().alpha(0f).setDuration(150).withEndAction {
            _b?.fabOverlay?.visibility = View.GONE
        }.start()

        b.fabQuickLinks.animate().rotation(0f).setDuration(150).start()
    }

    private fun load() {
        val inflater = LayoutInflater.from(requireContext())

        // Load classes from DB
        val classes = db.getClasses()
        b.classContainer.removeAllViews()
        if (classes.isEmpty()) {
            b.tvEmptyClasses.visibility = View.VISIBLE
            b.classContainer.addView(b.tvEmptyClasses)
        } else {
            b.tvEmptyClasses.visibility = View.GONE
            classes.forEach { item ->
                val row = ItemClassBinding.inflate(inflater, b.classContainer, false)
                row.tvTime.text = item.time
                row.tvCode.text = item.code
                row.tvRoom.text = if (item.name.isBlank()) item.room else "${item.room} • ${item.name}"
                row.root.setOnLongClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Class")
                        .setMessage("Remove ${item.code} from schedule?")
                        .setPositiveButton("Delete") { _, _ ->
                            db.deleteClass(item.id)
                            Toast.makeText(requireContext(), "Class removed", Toast.LENGTH_SHORT).show()
                            load()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
                b.classContainer.addView(row.root)
            }
        }
        b.tvClassCount.text = "${classes.size} Classes"

        // Load reminders from DB
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val reminders = db.getReminders().filter { it.date >= startOfToday }
        b.reminderContainer.removeAllViews()
        if (reminders.isEmpty()) {
            b.tvEmptyReminders.visibility = View.VISIBLE
            b.reminderContainer.addView(b.tvEmptyReminders)
        } else {
            b.tvEmptyReminders.visibility = View.GONE
            reminders.take(5).forEach { item ->
                val row = ItemReminderBinding.inflate(inflater, b.reminderContainer, false)
                row.tvTitle.text = item.title
                row.tvDate.text = Fmt.dateLong(item.date)
                row.tvNote.text = item.note.ifBlank { "No additional note" }
                row.root.setOnClickListener {
                    Dialogs.reminderDetails(requireContext(), item, db) { load() }
                }
                b.reminderContainer.addView(row.root)
            }
        }
        b.tvReminderCount.text = "${reminders.size} reminders"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
