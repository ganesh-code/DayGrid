package com.daygrid.app.data.db.dao

import androidx.room.*
import com.daygrid.app.data.db.entity.DayRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayRecordDao {

    @Query("SELECT * FROM day_records WHERE date LIKE :yearPrefix ORDER BY date ASC")
    fun getRecordsForYear(yearPrefix: String): Flow<List<DayRecordEntity>>

    @Query("SELECT * FROM day_records WHERE date = :date LIMIT 1")
    suspend fun getRecordForDate(date: String): DayRecordEntity?

    @Query("SELECT * FROM day_records WHERE date = :date LIMIT 1")
    fun getRecordForDateFlow(date: String): Flow<DayRecordEntity?>

    @Query("SELECT * FROM day_records ORDER BY date ASC")
    suspend fun getAllRecords(): List<DayRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(record: DayRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecords(records: List<DayRecordEntity>)

    @Query("DELETE FROM day_records WHERE date = :date")
    suspend fun deleteRecord(date: String)

    @Query("DELETE FROM day_records")
    suspend fun deleteAllRecords()

    @Query("""
        SELECT COUNT(*) FROM day_records 
        WHERE date LIKE :yearPrefix AND status = :status
    """)
    suspend fun countByYearAndStatus(yearPrefix: String, status: String): Int

    @Query("""
        SELECT COUNT(*) FROM day_records 
        WHERE date LIKE :yearPrefix
    """)
    suspend fun countForYear(yearPrefix: String): Int
}
