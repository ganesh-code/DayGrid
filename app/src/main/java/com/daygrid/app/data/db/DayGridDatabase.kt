package com.daygrid.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.daygrid.app.data.db.dao.DayRecordDao
import com.daygrid.app.data.db.entity.DayRecordEntity

@Database(
    entities = [DayRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DayGridDatabase : RoomDatabase() {
    abstract fun dayRecordDao(): DayRecordDao
}
