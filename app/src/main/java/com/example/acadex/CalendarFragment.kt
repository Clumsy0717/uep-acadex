package com.example.acadex

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.acadex.databinding.FragmentCalendarBinding
import com.example.acadex.databinding.ItemScheduledBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {
    private var _b: FragmentCalendarBinding? = null
    private val b get() = _b!!
    private lateinit var db: AcadexDb

    private val month: Calendar = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    private var selectedDay: Int? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCalendarBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = AcadexDb(requireContext())
        b.header.tvHeaderTitle.text = "Calendar"
        b.header.btnBack.setOnClickListener { (requireActivity() as MainActivity).goHome() }
        b.btnPrev.setOnClickListener { month.add(Calendar.MONTH, -1); selectedDay = null; render() }
        b.btnNext.setOnClickListener { month.add(Calendar.MONTH, 1); selectedDay = null; render() }
        render()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun cell(text: String, bold: Boolean = false, small: Boolean = false): FrameLayout {
        val label = TextView(requireContext()).apply {
            this.text = text
            textSize = if (small) 12f else 14f
            gravity = Gravity.CENTER
            if (bold) setTypeface(typeface, Typeface.BOLD)
            layoutParams = FrameLayout.LayoutParams(dp(38), dp(38), Gravity.CENTER)
        }
        return FrameLayout(requireContext()).apply {
            tag = label
            addView(label)
            layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply { width = 0; height = dp(42) }
        }
    }

    private fun render() {
        val ctx = requireContext()
        val allTasks = db.getTasks(false)
        val inMonth = allTasks.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.deadline }
            c.get(Calendar.YEAR) == month.get(Calendar.YEAR) && c.get(Calendar.MONTH) == month.get(Calendar.MONTH)
        }
        val taskDays = inMonth.map { Calendar.getInstance().apply { timeInMillis = it.deadline }.get(Calendar.DAY_OF_MONTH) }.toSet()

        b.tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(month.time)

        b.gridDays.removeAllViews()
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { b.gridDays.addView(cell(it, small = true)) }
        repeat(month.get(Calendar.DAY_OF_WEEK) - 1) { b.gridDays.addView(cell("")) }

        val today = Calendar.getInstance()
        val isThisMonth = today.get(Calendar.YEAR) == month.get(Calendar.YEAR) && today.get(Calendar.MONTH) == month.get(Calendar.MONTH)
        for (day in 1..month.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            val holder = cell(day.toString(), bold = day in taskDays)
            val tv = holder.tag as TextView
            val highlighted = selectedDay == day || (selectedDay == null && isThisMonth && today.get(Calendar.DAY_OF_MONTH) == day)
            if (highlighted) {
                tv.setBackgroundResource(R.drawable.bg_circle_selected)
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.white))
            } else if (day in taskDays) {
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.acadex_primary))
            }
            holder.setOnClickListener { selectedDay = if (selectedDay == day) null else day; render() }
            b.gridDays.addView(holder)
        }

        val shown = inMonth.filter { t ->
            selectedDay == null || Calendar.getInstance().apply { timeInMillis = t.deadline }.get(Calendar.DAY_OF_MONTH) == selectedDay
        }
        b.taskContainer.removeAllViews()
        if (shown.isEmpty()) {
            b.tvEmptyTasks.visibility = View.VISIBLE
            b.taskContainer.addView(b.tvEmptyTasks)
        } else {
            b.tvEmptyTasks.visibility = View.GONE
            val inflater = LayoutInflater.from(ctx)
            shown.forEach { t ->
                val row = ItemScheduledBinding.inflate(inflater, b.taskContainer, false)
                row.tvDate.text = Fmt.dateShort(t.deadline)
                row.tvTime.text = Fmt.time(t.deadline)
                row.tvTitle.text = t.subject
                row.tvDesc.text = t.description.ifBlank { "No description" }

                val primaryColor = ContextCompat.getColor(ctx, R.color.acadex_primary)
                val navyColor = ContextCompat.getColor(ctx, R.color.acadex_navy)

                if (t.done) {
                    row.btnFinish.text = "Finished"
                    row.btnFinish.setIconResource(R.drawable.ic_check)
                    row.btnFinish.backgroundTintList = ColorStateList.valueOf(navyColor)
                } else {
                    row.btnFinish.text = "Ongoing"
                    row.btnFinish.setIconResource(R.drawable.ic_clock)
                    row.btnFinish.backgroundTintList = ColorStateList.valueOf(primaryColor)
                }

                row.btnFinish.setOnHoverListener { _, event ->
                    if (!t.done) {
                        when (event.action) {
                            MotionEvent.ACTION_HOVER_ENTER -> {
                                row.btnFinish.text = "Finish"
                                row.btnFinish.setIconResource(R.drawable.ic_check)
                            }
                            MotionEvent.ACTION_HOVER_EXIT -> {
                                row.btnFinish.text = "Ongoing"
                                row.btnFinish.setIconResource(R.drawable.ic_clock)
                            }
                        }
                    }
                    false
                }

                row.btnMore.setOnClickListener { Dialogs.taskDetails(ctx, t, db) { render() } }

                row.btnFinish.setOnClickListener {
                    val newDone = !t.done
                    db.setTaskDone(t.id, newDone)
                    val msg = if (newDone) "Task marked as finished" else "Task reopened as ongoing"
                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                    render()
                }

                b.taskContainer.addView(row.root)
            }
        }
        b.tvTaskCount.text = "${shown.size} tasks"
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
