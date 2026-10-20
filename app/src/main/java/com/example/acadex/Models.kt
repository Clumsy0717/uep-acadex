package com.example.acadex

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Task(
    val id: Long,
    val subject: String,
    val code: String,
    val description: String,
    val deadline: Long,
    val done: Boolean
)

data class Reminder(val id: Long, val title: String, val note: String, val date: Long)

data class ClassItem(val id: Long, val time: String, val code: String, val room: String, val name: String = "")

data class RoomInfo(
    val name: String,
    val iconRes: Int,
    val number: String,
    val phone: String,
    val email: String,
    val hours: String
)

/** Date helpers matching the formats used in the app. */
object Fmt {
    private fun f(p: String) = SimpleDateFormat(p, Locale.getDefault())
    fun dateLong(ms: Long): String = f("MMMM d").format(Date(ms))      // September 28
    fun dateShort(ms: Long): String = f("MMM dd").format(Date(ms))     // Sep 18
    fun time(ms: Long): String = f("hh:mm a").format(Date(ms))         // 11:59 PM
    fun dateTime(ms: Long): String = f("MMM d, yyyy 'at' hh:mm a").format(Date(ms))
}

/** Real campus offices & directory information. */
object SampleData {
    val rooms = listOf(
        RoomInfo("Office of the Registrar", R.drawable.ic_building, "Room 101, Main Bldg", "(02) 8888-1000", "registrar@acadex.edu", "Mon - Fri, 8:00 AM - 5:00 PM"),
        RoomInfo("University Clinic", R.drawable.ic_clinic, "Room 102, Health Center", "(02) 8888-2000", "clinic@acadex.edu", "Mon - Sat, 7:00 AM - 7:00 PM"),
        RoomInfo("Digitel / IT Support", R.drawable.ic_monitor, "Room 205, Tech Wing", "(02) 8888-3000", "itsupport@acadex.edu", "Mon - Fri, 8:00 AM - 6:00 PM"),
        RoomInfo("University Library", R.drawable.ic_clipboard, "Learning Commons Bldg", "(02) 8888-4000", "library@acadex.edu", "Mon - Sat, 7:30 AM - 8:00 PM"),
        RoomInfo("Student Affairs Office", R.drawable.ic_building, "Room 105, Admin Bldg", "(02) 8888-5000", "osa@acadex.edu", "Mon - Fri, 8:00 AM - 5:00 PM")
    )
}
