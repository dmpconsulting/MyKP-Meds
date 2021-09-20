package org.kp.tpmg.mykpmeds.activation.util

import java.util.*

/**
 * Created by D113186 on 01,March,2021
 */
object HistoryCalendarUtil {

    fun getWeeksBetween(start: Date?, end: Date?): Int {
        val calendar1 = Calendar.getInstance(Locale.US)
        val calendar2 = Calendar.getInstance(Locale.US)

        // Skip time part! Not sure if this is needed, but I wanted clean dates.
        setDateWithNoTime(calendar1, start)
        setDateWithNoTime(calendar2, end)
        goToFirstDayOfWeek(calendar1)
        goToFirstDayOfWeek(calendar2)
        var weeks = 0
        while (calendar1 < calendar2) {
            calendar1.add(Calendar.WEEK_OF_YEAR, 1)
            weeks++
        }
        return weeks + 1
    }

    private fun goToFirstDayOfWeek(calendar: Calendar): Int {
        val firstDayOfWeek = calendar.firstDayOfWeek
        calendar[Calendar.DAY_OF_WEEK] = firstDayOfWeek
        return firstDayOfWeek
    }

    private fun setDateWithNoTime(calendar: Calendar, date: Date?) {
        date?.let { calendar.time = it }
        clearTime(calendar)
    }

    private fun clearTime(calendar: Calendar) {
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
    }
}