package com.daygrid.app.domain.usecase

import com.daygrid.app.domain.model.DayCell
import com.daygrid.app.domain.model.DayRecord
import com.daygrid.app.domain.repository.DayRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Year
import javax.inject.Inject

/**
 * Builds the full year grid combining persisted records with calendar day data.
 */
class GetYearGridUseCase @Inject constructor(
    private val repository: DayRecordRepository
) {
    operator fun invoke(year: Int): Flow<List<DayCell>> {
        val today = LocalDate.now()
        val yearDays = buildYearDays(year)

        return repository.getRecordsForYear(year).map { records ->
            val recordMap = records.associateBy { it.date }
            yearDays.map { date ->
                DayCell(
                    date = date,
                    record = recordMap[date],
                    isToday = date == today,
                    isFuture = date.isAfter(today),
                    isPast = date.isBefore(today)
                )
            }
        }
    }

    private fun buildYearDays(year: Int): List<LocalDate> {
        val isLeap = Year.isLeap(year.toLong())
        val totalDays = if (isLeap) 366 else 365
        val start = LocalDate.of(year, 1, 1)
        return (0 until totalDays).map { start.plusDays(it.toLong()) }
    }
}
