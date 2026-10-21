package com.daygrid.app.domain.usecase

import com.daygrid.app.domain.model.DayRecord
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.domain.repository.DayRecordRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class RecordDayUseCaseTest {

    private lateinit var repository: DayRecordRepository
    private lateinit var useCase: RecordDayUseCase

    @Before
    fun setup() {
        repository = mockk()
        coEvery { repository.upsertRecord(any()) } just Runs
    }

    @Test
    fun `recording today succeeds`() = runTest {
        useCase = RecordDayUseCase(repository)
        val result = useCase(LocalDate.now(), DayStatus.PRODUCTIVE)
        assertTrue(result.isSuccess)
        coVerify { repository.upsertRecord(match { it.status == DayStatus.PRODUCTIVE }) }
    }

    @Test
    fun `recording future date fails`() = runTest {
        useCase = RecordDayUseCase(repository)
        val future = LocalDate.now().plusDays(5)
        val result = useCase(future, DayStatus.PRODUCTIVE)
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.upsertRecord(any()) }
    }

    @Test
    fun `recording past date succeeds`() = runTest {
        useCase = RecordDayUseCase(repository)
        val past = LocalDate.now().minusDays(10)
        val result = useCase(past, DayStatus.NEUTRAL)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `note is trimmed`() = runTest {
        useCase = RecordDayUseCase(repository)
        val slot = slot<DayRecord>()
        coEvery { repository.upsertRecord(capture(slot)) } just Runs

        useCase(LocalDate.now(), DayStatus.PRODUCTIVE, note = "  some note  ")
        assertEquals("some note", slot.captured.note)
    }

    @Test
    fun `empty note is stored as null`() = runTest {
        useCase = RecordDayUseCase(repository)
        val slot = slot<DayRecord>()
        coEvery { repository.upsertRecord(capture(slot)) } just Runs

        useCase(LocalDate.now(), DayStatus.PRODUCTIVE, note = "   ")
        assertNull(slot.captured.note)
    }
}
