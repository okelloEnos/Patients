package com.okellosoftwarez.patients.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * DateUtils
 *
 * Provides utilities for converting between epoch timestamps, ISO-8601 date strings,
 * and user-friendly date formats. Ensures all dates are consistent for
 * visit comparisons and syncing with the backend.
 */
object DateUtils {

    // ISO-8601 date formatter used for API payloads (UTC)
    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // User-readable date formatter for display (local time)
    private val displayFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    /**
     * Convert epoch milliseconds -> ISO date string (UTC).
     * Example: 1716518400000L → "2024-05-24"
     */
    fun toIsoDate(epochMillis: Long?): String {
        return epochMillis?.let { isoFormatter.format(Date(it)) } ?: ""
    }

    /**
     * Convert ISO date string (e.g., "2024-05-24") -> epoch milliseconds (UTC midnight).
     */
    fun fromIsoDate(isoString: String?): Long? {
        if (isoString.isNullOrBlank()) return null
        return try {
            isoFormatter.parse(isoString)?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert epoch millis -> formatted date for UI.
     * Example: 1716518400000L → "24 May 2024"
     */
    fun toDisplayDate(epochMillis: Long?): String {
        return epochMillis?.let { displayFormatter.format(Date(it)) } ?: ""
    }

    /**
     * Convert year, month, day -> epoch milliseconds at local midnight.
     * Useful for generating DOB or visit dates from pickers.
     */
    fun fromYMD(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        return normalizeToMidnight(cal.timeInMillis)
    }

    /**
     * Normalize a timestamp to local midnight (00:00:00.000).
     * This is crucial for date-only comparisons, so "same day" works regardless of time zone.
     */
    fun normalizeToMidnight(epochMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Get the patient's age from their DOB (epoch).
     * Returns age in years (approx).
     */
    fun calculateAge(dobEpoch: Long?): Int? {
        if (dobEpoch == null) return null
        val now = Calendar.getInstance()
        val dob = Calendar.getInstance().apply { timeInMillis = dobEpoch }

        var age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    /**
     * Get the current date as epoch milliseconds normalized to local midnight.
     * Useful for default visitDate or registrationDate.
     */
    fun todayEpoch(): Long {
        return normalizeToMidnight(System.currentTimeMillis())
    }
}
