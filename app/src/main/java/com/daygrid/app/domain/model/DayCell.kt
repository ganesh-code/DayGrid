package com.daygrid.app.domain.model

import java.time.LocalDate

/**
 * Represents a single day cell in the year grid.
 *
 * @param date The calendar date this cell represents.
 * @param record The day record if one exists, null if unmarked.
 * @param isToday Whether this cell is today's date.
 * @param isFuture Whether this cell is a future date.
 * @param isPast Whether this cell is a past date (not today).
 */
data class DayCell(
    val date: LocalDate,
    val record: DayRecord? = null,
    val isToday: Boolean = false,
    val isFuture: Boolean = false,
    val isPast: Boolean = false
) {
    val status: DayStatus?
        get() = record?.status

    val displayStatus: DayCellDisplayStatus
        get() = when {
            isToday && record != null -> DayCellDisplayStatus.fromStatus(record.status, isToday = true)
            isToday -> DayCellDisplayStatus.TODAY_UNMARKED
            isFuture -> DayCellDisplayStatus.FUTURE
            record != null -> DayCellDisplayStatus.fromStatus(record.status, isToday = false)
            else -> DayCellDisplayStatus.PAST_UNMARKED
        }
}

enum class DayCellDisplayStatus {
    PRODUCTIVE,         // Orange fill
    UNPRODUCTIVE,       // Dark grey fill
    NEUTRAL,            // Subtle accent fill (rest)
    TODAY_UNMARKED,     // Outline ring, no fill
    TODAY_PRODUCTIVE,   // Orange fill + outline ring
    TODAY_UNPRODUCTIVE, // Dark grey fill + outline ring
    TODAY_NEUTRAL,      // Neutral fill + outline ring
    PAST_UNMARKED,      // Muted background
    FUTURE;             // Barely visible

    companion object {
        fun fromStatus(status: DayStatus, isToday: Boolean): DayCellDisplayStatus = when {
            isToday -> when (status) {
                DayStatus.PRODUCTIVE -> TODAY_PRODUCTIVE
                DayStatus.UNPRODUCTIVE -> TODAY_UNPRODUCTIVE
                DayStatus.NEUTRAL -> TODAY_NEUTRAL
            }
            else -> when (status) {
                DayStatus.PRODUCTIVE -> PRODUCTIVE
                DayStatus.UNPRODUCTIVE -> UNPRODUCTIVE
                DayStatus.NEUTRAL -> NEUTRAL
            }
        }
    }
}
