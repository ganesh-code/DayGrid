package com.daygrid.app.domain.model

/**
 * Statistical summary for a given year.
 *
 * Productive percentage = productiveDays / elapsedDays * 100
 * Future dates are never included in the denominator.
 */
data class YearStats(
    val year: Int,
    val totalDays: Int,
    val elapsedDays: Int,
    val futureDays: Int,
    val productiveDays: Int,
    val unproductiveDays: Int,
    val neutralDays: Int,
    val unmarkedElapsedDays: Int
) {
    /** Returns null if no days have elapsed (prevents division by zero). */
    val productivePercentage: Float?
        get() = if (elapsedDays == 0) null
        else (productiveDays.toFloat() / elapsedDays.toFloat()) * 100f

    val formattedPercentage: String
        get() = productivePercentage?.let { "%.1f%%".format(it) } ?: "—"
}
