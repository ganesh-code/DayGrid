package com.daygrid.app.domain.usecase

import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.domain.model.YearStats
import com.daygrid.app.domain.repository.DayRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Year
import javax.inject.Inject

/**
 * Computes year statistics from DB records.
 *
 * - Productive % = productiveDays / elapsedDays * 100
 * - elapsedDays = days from Jan 1 up to and including today (within the year)
 * - Future days are NEVER included in any denominator
 */
class GetYearStatsUseCase @Inject constructor(
    private val repository: DayRecordRepository
) {
    operator fun invoke(year: Int): Flow<YearStats> {
        val today = LocalDate.now()
        val isLeap = Year.isLeap(year.toLong())
        val totalDays = if (isLeap) 366 else 365

        val elapsedDays = when {
            today.year > year -> totalDays
            today.year < year -> 0
            else -> today.dayOfYear
        }
        val futureDays = totalDays - elapsedDays

        return repository.getRecordsForYear(year).map { records ->
            // Only count records for elapsed days (don't count future-dated records)
            val elapsedRecords = records.filter {
                !it.date.isAfter(today) || it.date.year < year
            }

            val productiveDays = elapsedRecords.count { it.status == DayStatus.PRODUCTIVE }
            val unproductiveDays = elapsedRecords.count { it.status == DayStatus.UNPRODUCTIVE }
            val neutralDays = elapsedRecords.count { it.status == DayStatus.NEUTRAL }
            val unmarkedElapsedDays = elapsedDays - elapsedRecords.size

            YearStats(
                year = year,
                totalDays = totalDays,
                elapsedDays = elapsedDays,
                futureDays = futureDays,
                productiveDays = productiveDays,
                unproductiveDays = unproductiveDays,
                neutralDays = neutralDays,
                unmarkedElapsedDays = maxOf(0, unmarkedElapsedDays)
            )
        }
    }
}
