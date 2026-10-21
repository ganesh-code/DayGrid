package com.daygrid.app.data.repository

import com.daygrid.app.data.db.dao.DayRecordDao
import com.daygrid.app.data.db.entity.DayRecordEntity
import com.daygrid.app.domain.model.DayRecord
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.domain.repository.DayRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayRecordRepositoryImpl @Inject constructor(
    private val dao: DayRecordDao
) : DayRecordRepository {

    override fun getRecordsForYear(year: Int): Flow<List<DayRecord>> {
        return dao.getRecordsForYear("$year%").map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override fun getRecordForDateFlow(date: LocalDate): Flow<DayRecord?> {
        return dao.getRecordForDateFlow(date.toString()).map { it?.toDomain() }
    }

    override suspend fun getRecordForDate(date: LocalDate): DayRecord? {
        return dao.getRecordForDate(date.toString())?.toDomain()
    }

    override suspend fun getAllRecords(): List<DayRecord> {
        return dao.getAllRecords().mapNotNull { it.toDomain() }
    }

    override suspend fun upsertRecord(record: DayRecord) {
        dao.upsertRecord(record.toEntity())
    }

    override suspend fun deleteRecord(date: LocalDate) {
        dao.deleteRecord(date.toString())
    }

    override suspend fun deleteAllRecords() {
        dao.deleteAllRecords()
    }

    override suspend fun upsertRecords(records: List<DayRecord>) {
        dao.upsertRecords(records.map { it.toEntity() })
    }

    // --- Mapping ---

    private fun DayRecordEntity.toDomain(): DayRecord? {
        val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
        val parsedStatus = DayStatus.fromString(status) ?: return null
        return DayRecord(
            date = parsedDate,
            status = parsedStatus,
            note = note,
            updatedAt = updatedAt
        )
    }

    private fun DayRecord.toEntity(): DayRecordEntity = DayRecordEntity(
        date = date.toString(),
        status = status.name,
        note = note,
        updatedAt = updatedAt
    )
}
