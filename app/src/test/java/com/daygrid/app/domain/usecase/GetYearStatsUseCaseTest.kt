package com.daygrid.app.domain.usecase

import app.cash.turbine.test
import com.daygrid.app.domain.model.DayRecord
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.domain.repository.DayRecordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetYearStatsUseCaseTest {

    private lateinit var repository: DayRecordRepository
    private lateinit var useCase: GetYearStatsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetYearStatsUseCase(repository)
    }

    @Test
    fun `leap year 2024 total days is 366`() = runTest {
        coEvery { repository.getRecordsForYear(2024) } returns flowOf(emptyList())

        useCase(2024).test {
            val stats = awaitItem()
            assertEquals(366, stats.totalDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `regular year 2023 total days is 365`() = runTest {
        coEvery { repository.getRecordsForYear(2023) } returns flowOf(emptyList())

        useCase(2023).test {
            val stats = awaitItem()
            assertEquals(365, stats.totalDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `no elapsed days means no percentage division error`() = runTest {
        coEvery { repository.getRecordsForYear(2100) } returns flowOf(emptyList())

        useCase(2100).test {
            val stats = awaitItem()
            assertEquals(0, stats.elapsedDays)
            assertNull(stats.productivePercentage)
            assertEquals("—", stats.formattedPercentage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `productive count is correct`() = runTest {
        val records = listOf(
            DayRecord(date = LocalDate.of(2023, 1, 1), status = DayStatus.PRODUCTIVE),
            DayRecord(date = LocalDate.of(2023, 1, 2), status = DayStatus.PRODUCTIVE),
            DayRecord(date = LocalDate.of(2023, 1, 3), status = DayStatus.UNPRODUCTIVE),
        )
        coEvery { repository.getRecordsForYear(2023) } returns flowOf(records)

        useCase(2023).test {
            val stats = awaitItem()
            assertEquals(2, stats.productiveDays)
            assertEquals(1, stats.unproductiveDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `future year has zero elapsed days`() = runTest {
        coEvery { repository.getRecordsForYear(2099) } returns flowOf(emptyList())

        useCase(2099).test {
            val stats = awaitItem()
            assertEquals(0, stats.elapsedDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `past year has all days elapsed`() = runTest {
        coEvery { repository.getRecordsForYear(2020) } returns flowOf(emptyList()) // 2020 = leap year

        useCase(2020).test {
            val stats = awaitItem()
            assertEquals(366, stats.totalDays)
            assertEquals(366, stats.elapsedDays)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
