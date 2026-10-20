package com.example.acadex

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.acadex.databinding.FragmentPlannerBinding
import java.util.Calendar

class PlannerFragment : Fragment() {
    private var _b: FragmentPlannerBinding? = null
    private val b get() = _b!!

    private val picked: Calendar = Calendar.getInstance().apply { set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    private var hasDate = false
    private var hasTime = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentPlannerBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.header.tvHeaderTitle.text = "Task Planner"
        b.header.btnBack.setOnClickListener { (requireActivity() as MainActivity).goHome() }

        b.tvDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                picked.set(y, m, d); hasDate = true
                b.tvDate.text = Fmt.dateLong(picked.timeInMillis)
            }, picked.get(Calendar.YEAR), picked.get(Calendar.MONTH), picked.get(Calendar.DAY_OF_MONTH)).show()
        }
        b.tvTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                picked.set(Calendar.HOUR_OF_DAY, h); picked.set(Calendar.MINUTE, m); hasTime = true
                b.tvTime.text = Fmt.time(picked.timeInMillis)
            }, picked.get(Calendar.HOUR_OF_DAY), picked.get(Calendar.MINUTE), false).show()
        }
        b.btnCancel.setOnClickListener { clearForm() }
        b.btnAddTask.setOnClickListener { save() }
    }

    private fun save() {
        val subject = b.etSubject.text.toString().trim()
        val code = b.etCode.text.toString().trim()
        val desc = b.etDesc.text.toString().trim()
        when {
            subject.isEmpty() -> b.etSubject.error = "Required"
            !hasDate || !hasTime -> Toast.makeText(requireContext(), "Select a deadline date and time", Toast.LENGTH_SHORT).show()
            else -> {
                AcadexDb(requireContext()).addTask(subject, code, desc, picked.timeInMillis)
                clearForm()
                b.tvToast.visibility = View.VISIBLE
                b.tvToast.postDelayed({ _b?.tvToast?.visibility = View.GONE }, 2500)
            }
        }
    }

    private fun clearForm() {
        b.etSubject.text.clear(); b.etCode.text.clear(); b.etDesc.text.clear()
        b.tvDate.text = ""; b.tvTime.text = ""
        hasDate = false; hasTime = false
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
