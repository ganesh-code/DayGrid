package com.daygrid.app.domain.usecase

import com.daygrid.app.domain.model.DayRecord
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.domain.repository.DayRecordRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Records or updates a day's status.
 * Future dates cannot be marked productive through the normal check-in flow.
 */
class RecordDayUseCase @Inject constructor(
    private val repository: DayRecordRepository
) {
    suspend operator fun invoke(
        date: LocalDate,
        status: DayStatus,
        note: String? = null,
        allowFuture: Boolean = false
    ): Result<Unit> {
        val today = LocalDate.now()

        if (!allowFuture && date.isAfter(today)) {
            return Result.failure(IllegalArgumentException("Cannot mark future dates as completed"))
        }

        return runCatching {
            repository.upsertRecord(
                DayRecord(
                    date = date,
                    status = status,
                    note = note?.trim()?.takeIf { it.isNotEmpty() },
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}

/**
 * Removes a day record, returning it to the "unmarked" state.
 */
class DeleteDayRecordUseCase @Inject constructor(
    private val repository: DayRecordRepository
) {
    suspend operator fun invoke(date: LocalDate): Result<Unit> = runCatching {
        repository.deleteRecord(date)
    }
}
