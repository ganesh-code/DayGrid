package com.daygrid.app.domain.repository

import com.daygrid.app.domain.model.DayRecord
import com.daygrid.app.domain.model.DayStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DayRecordRepository {
    fun getRecordsForYear(year: Int): Flow<List<DayRecord>>
    fun getRecordForDateFlow(date: LocalDate): Flow<DayRecord?>
    suspend fun getRecordForDate(date: LocalDate): DayRecord?
    suspend fun getAllRecords(): List<DayRecord>
    suspend fun upsertRecord(record: DayRecord)
    suspend fun deleteRecord(date: LocalDate)
    suspend fun deleteAllRecords()
    suspend fun upsertRecords(records: List<DayRecord>)
}
