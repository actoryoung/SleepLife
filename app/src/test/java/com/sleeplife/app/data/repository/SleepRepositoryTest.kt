package com.sleeplife.app.data.repository

import com.sleeplife.app.RepositoryTestBase
import com.sleeplife.app.core.AppException
import com.sleeplife.app.core.Result
import com.sleeplife.app.data.entities.SleepRecord
import com.sleeplife.app.data.entities.SleepQuality
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SleepRepository
 */
class SleepRepositoryTest : RepositoryTestBase() {

    private lateinit var sleepRepository: SleepRepository

    private val testSleepRecord = SleepRecord(
        id = 0,
        startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        endTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        notes = "Test sleep note",
        quality = SleepQuality.GOOD,
        createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )

    @Before
    override fun setUp() {
        super.setUp()
        sleepRepository = SleepRepository(sleepDao)
    }

    // ==================== Insertion Tests ====================

    @Test
    fun `insertSleepRecordWithValidation should return Success when record is valid`() = runTest {
        // Arrange
        val validRecord = testSleepRecord.copy(
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().plus(kotlin.time.Duration.parse("8h")).toLocalDateTime(TimeZone.currentSystemDefault())
        )

        // Act
        val result = sleepRepository.insertSleepRecordWithValidation(validRecord)

        // Assert
        assertTrue(result.isSuccess())
        assertTrue((result as Result.Success).data > 0)
    }

    @Test
    fun `insertSleepRecordWithValidation should return Error when end time is before start time`() = runTest {
        // Arrange
        val invalidRecord = testSleepRecord.copy(
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().minus(kotlin.time.Duration.parse("1h")).toLocalDateTime(TimeZone.currentSystemDefault())
        )

        // Act
        val result = sleepRepository.insertSleepRecordWithValidation(invalidRecord)

        // Assert
        assertTrue(result.isError())
        assertFalse(result.isSuccess())
    }

    @Test
    fun `insertSleepRecordWithValidation_validationFails_returnsError`() = runTest {
        // Arrange - record with null endTime triggers validation failure
        val invalidRecord = testSleepRecord.copy(endTime = null)

        // Act
        val result = sleepRepository.insertSleepRecordWithValidation(invalidRecord)

        // Assert
        assertTrue(result.isError())
        val error = result as Result.Error
        assertTrue(error.exception is AppException.ValidationException)
    }

    @Test
    fun `insertSleepRecordWithValidation should return Error when notes exceed max length`() = runTest {
        // Arrange
        val invalidRecord = testSleepRecord.copy(
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().plus(kotlin.time.Duration.parse("8h")).toLocalDateTime(TimeZone.currentSystemDefault()),
            notes = "x".repeat(501) // Exceeds MAX_NOTES_LENGTH of 500
        )

        // Act
        val result = sleepRepository.insertSleepRecordWithValidation(invalidRecord)

        // Assert
        assertTrue(result.isError())
    }

    // ==================== Update Tests ====================

    @Test
    fun `updateSleepRecordWithValidation should return Success when record is valid`() = runTest {
        // Arrange
        val record = testSleepRecord.copy(
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().plus(kotlin.time.Duration.parse("8h")).toLocalDateTime(TimeZone.currentSystemDefault())
        )
        val insertedId = sleepDao.insertSleepRecord(record)
        val updatedRecord = record.copy(id = insertedId, quality = SleepQuality.EXCELLENT)

        // Act
        val result = sleepRepository.updateSleepRecordWithValidation(updatedRecord)

        // Assert
        assertTrue(result.isSuccess())
        val fetchedRecord = sleepDao.getSleepRecordById(insertedId)
        assertEquals(SleepQuality.EXCELLENT, fetchedRecord?.quality)
    }

    // ==================== Deletion Tests ====================

    @Test
    fun `deleteSleepRecordsWithValidation should return Success and delete record`() = runTest {
        // Arrange
        val record = testSleepRecord.copy(
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().plus(kotlin.time.Duration.parse("8h")).toLocalDateTime(TimeZone.currentSystemDefault())
        )
        val insertedId = sleepDao.insertSleepRecord(record)

        // Act
        val result = sleepRepository.deleteSleepRecordsWithValidation(insertedId)

        // Assert
        assertTrue(result.isSuccess())
        assertNull(sleepDao.getSleepRecordById(insertedId))
    }

    // ==================== Query Tests ====================

    @Test
    fun `getAllSleepRecords should return all records as Flow`() = runTest {
        // Arrange
        val record1 = testSleepRecord.copy(
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().plus(kotlin.time.Duration.parse("8h")).toLocalDateTime(TimeZone.currentSystemDefault())
        )
        val record2 = record1.copy(notes = "Another record")
        sleepDao.insertSleepRecord(record1)
        sleepDao.insertSleepRecord(record2)

        // Act & Assert
        val records = sleepRepository.getAllSleepRecords().first()
        assertEquals(2, records.size)
    }

    @Test
    fun `getRecentSleepRecords should return limited records`() = runTest {
        // Arrange
        val nowInstant = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        repeat(5) { i ->
            sleepDao.insertSleepRecord(
                testSleepRecord.copy(
                    startTime = nowInstant.plus(i.hours).toLocalDateTime(tz),
                    endTime = nowInstant.plus((i + 8).hours).toLocalDateTime(tz),
                    notes = "Record $i"
                )
            )
        }

        // Act & Assert
        val records = sleepRepository.getRecentSleepRecords(limit = 3).first()
        assertEquals(3, records.size)
    }

    @Test
    fun `getSleepRecordById should return correct record`() = runTest {
        // Arrange
        val record = testSleepRecord.copy(
            startTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            endTime = Clock.System.now().plus(kotlin.time.Duration.parse("8h")).toLocalDateTime(TimeZone.currentSystemDefault())
        )
        val insertedId = sleepDao.insertSleepRecord(record)

        // Act
        val result = sleepRepository.getSleepRecordById(insertedId)

        // Assert
        assertNotNull(result)
        assertEquals(insertedId, result?.id)
        assertEquals("Test sleep note", result?.notes)
    }
}
