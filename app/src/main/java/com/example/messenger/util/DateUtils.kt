package com.example.messenger.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    fun formatLastSeen(timestamp: Long): String {
        if (timestamp <= 0L) return "Offline"

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)

        return when {
            minutes < 1 -> "Last seen just now"
            minutes in 1..59 -> "Last seen $minutes min ago"
            hours in 1..23 -> "Last seen $hours hr ago"
            hours in 24..48 -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Last seen yesterday at ${timeFormat.format(Date(timestamp))}"
            }
            else -> {
                val dateFormat = SimpleDateFormat("MM/dd 'at' HH:mm", Locale.getDefault())
                "Last seen ${dateFormat.format(Date(timestamp))}"
            }
        }
    }

    fun formatMessageTime(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }
}