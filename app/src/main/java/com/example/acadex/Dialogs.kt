package com.example.acadex

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.acadex.databinding.DialogRoomBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

object Dialogs {

    fun taskDetails(ctx: Context, t: Task, db: AcadexDb? = null, onUpdated: (() -> Unit)? = null) {
        val msg = buildString {
            if (t.code.isNotBlank()) append("Subject code: ${t.code}\n\n")
            if (t.description.isNotBlank()) append("${t.description}\n\n")
            append("Deadline: ${Fmt.dateTime(t.deadline)}")
            if (t.done) append("\n\nStatus: Completed")
        }
        val builder = MaterialAlertDialogBuilder(ctx)
            .setTitle(t.subject)
            .setMessage(msg)
            .setPositiveButton("Close", null)

        if (db != null && onUpdated != null) {
            if (!t.done) {
                builder.setNeutralButton("Mark Done") { _, _ ->
                    db.markDone(t.id)
                    Toast.makeText(ctx, "Task marked as completed", Toast.LENGTH_SHORT).show()
                    onUpdated()
                }
            }
            builder.setNegativeButton("Delete") { _, _ ->
                db.deleteTask(t.id)
                Toast.makeText(ctx, "Task deleted", Toast.LENGTH_SHORT).show()
                onUpdated()
            }
        }
        builder.show()
    }

    fun reminderDetails(ctx: Context, r: Reminder, db: AcadexDb, onUpdated: () -> Unit) {
        val msg = buildString {
            if (r.note.isNotBlank()) append("${r.note}\n\n")
            append("Date: ${Fmt.dateLong(r.date)}")
        }
        MaterialAlertDialogBuilder(ctx)
            .setTitle(r.title)
            .setMessage(msg)
            .setPositiveButton("Close", null)
            .setNegativeButton("Delete") { _, _ ->
                db.deleteReminder(r.id)
                Toast.makeText(ctx, "Reminder deleted", Toast.LENGTH_SHORT).show()
                onUpdated()
            }
            .show()
    }

    fun roomInfo(ctx: Context, room: RoomInfo) {
        val b = DialogRoomBinding.inflate(LayoutInflater.from(ctx))
        b.tvRoomName.text = room.name
        b.tvNumber.text = room.number
        b.tvPhone.text = room.phone
        b.tvEmail.text = room.email
        b.tvHours.text = room.hours
        val dialog: AlertDialog = MaterialAlertDialogBuilder(ctx).setView(b.root).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        b.btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun addReminder(ctx: Context, db: AcadexDb, onAdded: () -> Unit) {
        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_add_reminder, null)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val picked = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0) }
        var hasDate = false
        tvDate.setOnClickListener {
            DatePickerDialog(ctx, { _, y, m, d ->
                picked.set(y, m, d); hasDate = true
                tvDate.text = Fmt.dateLong(picked.timeInMillis)
            }, picked.get(Calendar.YEAR), picked.get(Calendar.MONTH), picked.get(Calendar.DAY_OF_MONTH)).show()
        }
        MaterialAlertDialogBuilder(ctx).setTitle("Add Reminder").setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isEmpty() || !hasDate) {
                    Toast.makeText(ctx, "Enter a title and pick a date", Toast.LENGTH_SHORT).show()
                } else {
                    db.addReminder(title, etNote.text.toString().trim(), picked.timeInMillis)
                    Toast.makeText(ctx, "Reminder has been added", Toast.LENGTH_SHORT).show()
                    onAdded()
                }
            }.show()
    }

    fun addClass(ctx: Context, db: AcadexDb, onAdded: () -> Unit) {
        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_add_class, null)
        val etCode = view.findViewById<EditText>(R.id.etCode)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etTime = view.findViewById<EditText>(R.id.etTime)
        val etRoom = view.findViewById<EditText>(R.id.etRoom)

        MaterialAlertDialogBuilder(ctx)
            .setTitle("Add Class Schedule")
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val code = etCode.text.toString().trim()
                val name = etName.text.toString().trim()
                val time = etTime.text.toString().trim()
                val room = etRoom.text.toString().trim()

                if (code.isEmpty() || time.isEmpty() || room.isEmpty()) {
                    Toast.makeText(ctx, "Please fill in course code, schedule time, and room", Toast.LENGTH_SHORT).show()
                } else {
                    db.addClass(time, code, room, name)
                    Toast.makeText(ctx, "Class added to schedule", Toast.LENGTH_SHORT).show()
                    onAdded()
                }
            }.show()
    }
}
