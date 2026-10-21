package com.daygrid.app.di

import android.content.Context
import androidx.room.Room
import com.daygrid.app.data.db.DayGridDatabase
import com.daygrid.app.data.db.dao.DayRecordDao
import com.daygrid.app.data.repository.DayRecordRepositoryImpl
import com.daygrid.app.domain.repository.DayRecordRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DayGridDatabase {
        return Room.databaseBuilder(
            context,
            DayGridDatabase::class.java,
            "daygrid.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDayRecordDao(db: DayGridDatabase): DayRecordDao = db.dayRecordDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDayRecordRepository(
        impl: DayRecordRepositoryImpl
    ): DayRecordRepository
}
