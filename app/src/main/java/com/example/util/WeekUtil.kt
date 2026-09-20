package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WeekUtil {
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val monthDayFormat = SimpleDateFormat("MMM d", Locale.US)
    private val dayNameFormat = SimpleDateFormat("EEE", Locale.US)

    fun todayDateString(): String {
        return isoDateFormat.format(Date())
    }

    fun formatDateDisplay(dateStr: String): String {
        return try {
            val date = isoDateFormat.parse(dateStr) ?: return dateStr
            displayDateFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getDayOfWeekShort(dateStr: String): String {
        return try {
            val date = isoDateFormat.parse(dateStr) ?: return ""
            dayNameFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Generates a weekKey in format "yyyy-Www", e.g. "2026-W38"
     */
    fun getCurrentWeekKey(weekStartsOnMonday: Boolean = true): String {
        val cal = Calendar.getInstance(Locale.US)
        cal.firstDayOfWeek = if (weekStartsOnMonday) Calendar.MONDAY else Calendar.SUNDAY
        cal.minimalDaysInFirstWeek = 4
        return formatWeekKey(cal)
    }

    fun getWeekKeyForDate(dateStr: String, weekStartsOnMonday: Boolean = true): String {
        return try {
            val date = isoDateFormat.parse(dateStr) ?: Date()
            val cal = Calendar.getInstance(Locale.US)
            cal.firstDayOfWeek = if (weekStartsOnMonday) Calendar.MONDAY else Calendar.SUNDAY
            cal.minimalDaysInFirstWeek = 4
            cal.time = date
            formatWeekKey(cal)
        } catch (e: Exception) {
            getCurrentWeekKey(weekStartsOnMonday)
        }
    }

    private fun formatWeekKey(cal: Calendar): String {
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%04d-W%02d", year, week)
    }

    private fun parseWeekKey(weekKey: String, weekStartsOnMonday: Boolean): Calendar {
        val parts = weekKey.split("-W")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val week = parts.getOrNull(1)?.toIntOrNull() ?: 1

        val cal = Calendar.getInstance(Locale.US)
        cal.clear()
        cal.firstDayOfWeek = if (weekStartsOnMonday) Calendar.MONDAY else Calendar.SUNDAY
        cal.minimalDaysInFirstWeek = 4
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.WEEK_OF_YEAR, week)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return cal
    }

    fun getPreviousWeekKey(weekKey: String, weekStartsOnMonday: Boolean = true): String {
        val cal = parseWeekKey(weekKey, weekStartsOnMonday)
        cal.add(Calendar.WEEK_OF_YEAR, -1)
        return formatWeekKey(cal)
    }

    fun getNextWeekKey(weekKey: String, weekStartsOnMonday: Boolean = true): String {
        val cal = parseWeekKey(weekKey, weekStartsOnMonday)
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        return formatWeekKey(cal)
    }

    /**
     * Returns a human readable date range e.g. "Sep 15 – Sep 21, 2026"
     */
    fun getWeekRangeDisplay(weekKey: String, weekStartsOnMonday: Boolean = true): String {
        val startCal = parseWeekKey(weekKey, weekStartsOnMonday)
        val startDate = startCal.time

        val endCal = startCal.clone() as Calendar
        endCal.add(Calendar.DAY_OF_YEAR, 6)
        val endDate = endCal.time

        val startYear = startCal.get(Calendar.YEAR)
        val endYear = endCal.get(Calendar.YEAR)

        return if (startYear == endYear) {
            "${monthDayFormat.format(startDate)} – ${monthDayFormat.format(endDate)}, $startYear"
        } else {
            "${displayDateFormat.format(startDate)} – ${displayDateFormat.format(endDate)}"
        }
    }

    /**
     * Returns list of 7 dates (yyyy-MM-dd) for the week
     */
    fun getDaysForWeek(weekKey: String, weekStartsOnMonday: Boolean = true): List<Pair<String, String>> {
        val cal = parseWeekKey(weekKey, weekStartsOnMonday)
        val days = mutableListOf<Pair<String, String>>()
        for (i in 0 until 7) {
            val dateStr = isoDateFormat.format(cal.time)
            val dayName = dayNameFormat.format(cal.time)
            days.add(Pair(dateStr, dayName))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }
}
