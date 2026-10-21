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

class GetYearGridUseCaseTest {

    private lateinit var repository: DayRecordRepository
    private lateinit var useCase: GetYearGridUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetYearGridUseCase(repository)
    }

    @Test
    fun `regular year has 365 cells`() = runTest {
        // 2023 is not a leap year
        coEvery { repository.getRecordsForYear(2023) } returns flowOf(emptyList())

        useCase(2023).test {
            val cells = awaitItem()
            assertEquals(365, cells.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `leap year 2024 has 366 cells`() = runTest {
        coEvery { repository.getRecordsForYear(2024) } returns flowOf(emptyList())

        useCase(2024).test {
            val cells = awaitItem()
            assertEquals(366, cells.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cells are in chronological order`() = runTest {
        coEvery { repository.getRecordsForYear(2024) } returns flowOf(emptyList())

        useCase(2024).test {
            val cells = awaitItem()
            assertEquals(LocalDate.of(2024, 1, 1), cells.first().date)
            assertEquals(LocalDate.of(2024, 12, 31), cells.last().date)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `productive record is mapped to correct cell`() = runTest {
        val targetDate = LocalDate.of(2024, 6, 15)
        val record = DayRecord(date = targetDate, status = DayStatus.PRODUCTIVE)

        coEvery { repository.getRecordsForYear(2024) } returns flowOf(listOf(record))

        useCase(2024).test {
            val cells = awaitItem()
            val targetCell = cells.find { it.date == targetDate }
            assertNotNull(targetCell)
            assertEquals(DayStatus.PRODUCTIVE, targetCell?.status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `future cells have isFuture true`() = runTest {
        coEvery { repository.getRecordsForYear(2100) } returns flowOf(emptyList())

        useCase(2100).test {
            val cells = awaitItem()
            assertTrue(cells.all { it.isFuture })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `past year cells have isPast true`() = runTest {
        coEvery { repository.getRecordsForYear(2000) } returns flowOf(emptyList())

        useCase(2000).test {
            val cells = awaitItem()
            assertTrue(cells.all { it.isPast })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
