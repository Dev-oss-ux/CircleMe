package com.barry.circleme.utils

import android.text.format.DateUtils
import java.util.Date

object TimeUtils {
    fun formatTimestamp(timestamp: Date?): String {
        if (timestamp == null) return ""
        val now = System.currentTimeMillis()
        val time = timestamp.time

        return DateUtils.getRelativeTimeSpanString(
            time, 
            now, 
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    fun getTimeAgo(timestamp: Date?): String {
        if (timestamp == null) return ""
        val now = System.currentTimeMillis()
        val time = timestamp.time

        val diff = now - time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days >= 2 -> "${days}d ago"
            days == 1L -> "Yesterday"
            else -> "Today"
        }
    }
}
