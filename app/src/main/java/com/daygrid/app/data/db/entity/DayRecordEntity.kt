package com.daygrid.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single day's record in the database.
 *
 * @param date ISO local date string (yyyy-MM-dd). Stored as a local calendar date,
 *             NOT a UTC timestamp, to prevent timezone-related date shifts.
 * @param status The user-recorded day status.
 * @param note Optional short reflection note (private, stored locally only).
 * @param updatedAt Unix epoch milliseconds at last modification.
 */
@Entity(tableName = "day_records")
data class DayRecordEntity(
    @PrimaryKey
    val date: String, // ISO format: yyyy-MM-dd

    val status: String, // "PRODUCTIVE" | "UNPRODUCTIVE" | "NEUTRAL"

    val note: String? = null,

    val updatedAt: Long = System.currentTimeMillis()
)
