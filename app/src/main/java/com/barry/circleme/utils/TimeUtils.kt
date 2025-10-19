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
}
