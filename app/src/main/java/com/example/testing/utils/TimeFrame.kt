package com.example.testing.utils

import java.util.Calendar

enum class TimeFrame {
    WEEK,
    MONTH,
    QUARTER,
    YEAR
}

object TimeFrameUtils {
    fun getTimeFrameRange(timeFrame: TimeFrame): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val end = cal.timeInMillis

        when (timeFrame) {
            TimeFrame.WEEK -> cal.add(Calendar.DAY_OF_YEAR, -7)
            TimeFrame.MONTH -> cal.add(Calendar.MONTH, -1)
            TimeFrame.QUARTER -> cal.add(Calendar.MONTH, -3)
            TimeFrame.YEAR -> cal.add(Calendar.YEAR, -1)
        }
        
        // Start of the day for the start date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val start = cal.timeInMillis
        return start to end
    }
}
