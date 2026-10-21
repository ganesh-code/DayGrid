package com.daygrid.app.domain.model

import java.time.LocalDate

/**
 * Domain model for a day record.
 */
data class DayRecord(
    val date: LocalDate,
    val status: DayStatus,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class DayStatus {
    PRODUCTIVE,
    UNPRODUCTIVE,
    NEUTRAL;

    companion object {
        fun fromString(value: String): DayStatus? = entries.find { it.name == value }
    }
}
