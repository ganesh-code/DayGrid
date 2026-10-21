package com.daygrid.app.domain.usecase

import com.daygrid.app.domain.model.DayRecord
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.domain.repository.DayRecordRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import javax.inject.Inject

data class ExportRecord(
    val date: String,
    val status: String,
    val note: String?,
    val updatedAt: Long
)

class ExportDataUseCase @Inject constructor(
    private val repository: DayRecordRepository
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend operator fun invoke(): Result<String> = runCatching {
        val records = repository.getAllRecords()
        val exportRecords = records.map {
            ExportRecord(
                date = it.date.toString(),
                status = it.status.name,
                note = it.note,
                updatedAt = it.updatedAt
            )
        }
        gson.toJson(exportRecords)
    }
}

class ImportDataUseCase @Inject constructor(
    private val repository: DayRecordRepository
) {
    private val gson: Gson = Gson()

    /**
     * Validates imported JSON and merges with existing records.
     * Never destroys valid existing data due to malformed import.
     */
    suspend operator fun invoke(json: String, replaceAll: Boolean = false): Result<Int> = runCatching {
        val type = object : TypeToken<List<ExportRecord>>() {}.type
        val exported: List<ExportRecord> = gson.fromJson(json, type)
            ?: throw IllegalArgumentException("Invalid JSON: null result")

        val validRecords = exported.mapNotNull { export ->
            val date = runCatching { LocalDate.parse(export.date) }.getOrNull() ?: return@mapNotNull null
            val status = DayStatus.fromString(export.status) ?: return@mapNotNull null
            DayRecord(
                date = date,
                status = status,
                note = export.note?.trim()?.takeIf { it.isNotEmpty() },
                updatedAt = export.updatedAt
            )
        }

        if (validRecords.isEmpty()) throw IllegalArgumentException("No valid records found in import file")

        if (replaceAll) repository.deleteAllRecords()
        repository.upsertRecords(validRecords)
        validRecords.size
    }
}
