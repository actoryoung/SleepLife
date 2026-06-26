package com.sleeplife.app.core.validators

import com.sleeplife.app.data.entities.SleepQuality
import com.sleeplife.app.data.entities.SleepRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class SleepValidatorTest {

    private val now: LocalDateTime =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private fun localDateTimeFromNow(
        offset: kotlin.time.Duration
    ): LocalDateTime {
        val instant = Clock.System.now() + offset
        return instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    private fun createRecord(
        startTime: LocalDateTime = localDateTimeFromNow(offset = (-8).hours),
        endTime: LocalDateTime? = now,
        quality: SleepQuality = SleepQuality.GOOD,
        notes: String = "Test sleep notes"
    ): SleepRecord = SleepRecord(
        startTime = startTime,
        endTime = endTime,
        quality = quality,
        notes = notes
    )

    @Test
    fun `given valid sleep record, should return Valid`() {
        val record = createRecord()
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue(
            "Expected Valid for a valid sleep record, got: $result",
            result is ValidationResult.Valid
        )
    }

    @Test
    fun `given record with exactly 500-character notes, should return Valid`() {
        val record = createRecord(notes = "x".repeat(500))
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue("Expected Valid for exactly 500-char notes", result is ValidationResult.Valid)
    }

    @Test
    fun `given record with start time exactly 1 hour in future, should return Valid`() {
        val futureStart = localDateTimeFromNow(offset = 1.hours)
        val futureEnd = localDateTimeFromNow(offset = 2.hours)
        val record = createRecord(startTime = futureStart, endTime = futureEnd)
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue(
            "Expected Valid for start time exactly 1 hour in future",
            result is ValidationResult.Valid
        )
    }

    @Test
    fun `given null endTime, should return Error`() {
        val record = createRecord(endTime = null)
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue("Expected Error for null endTime", result is ValidationResult.Error)
        assertEquals("结束时间不能为空", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given endTime before startTime, should return Error`() {
        val start = now
        val before = localDateTimeFromNow(offset = (-1).hours)
        val record = createRecord(startTime = start, endTime = before)
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue("Expected Error for endTime before startTime", result is ValidationResult.Error)
        assertEquals("结束时间必须晚于开始时间", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given endTime equal to startTime, should return Error`() {
        val sameTime = now
        val record = createRecord(startTime = sameTime, endTime = sameTime)
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue("Expected Error for endTime == startTime", result is ValidationResult.Error)
        assertEquals("结束时间必须晚于开始时间", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given start time more than 1 hour in future, should return Error`() {
        val futureStart = localDateTimeFromNow(offset = 2.hours)
        val futureEnd = localDateTimeFromNow(offset = 3.hours)
        val record = createRecord(startTime = futureStart, endTime = futureEnd)
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue(
            "Expected Error for startTime > 1 hour in future",
            result is ValidationResult.Error
        )
        assertEquals("开始时间不能超过1小时后", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given notes longer than 500 characters, should return Error`() {
        val record = createRecord(notes = "x".repeat(501))
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue("Expected Error for notes > 500 chars", result is ValidationResult.Error)
        assertEquals("备注不能超过500字符", (result as ValidationResult.Error).message)
    }

    @Test
    fun `given notes blank but within length limit, should return Valid`() {
        val record = createRecord(notes = "")
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue("Expected Valid for empty notes", result is ValidationResult.Valid)
    }

    @Test
    fun `given start time 61 minutes in future, should return Error`() {
        val futureStart = localDateTimeFromNow(offset = 61.minutes)
        val futureEnd = localDateTimeFromNow(offset = 91.minutes)
        val record = createRecord(startTime = futureStart, endTime = futureEnd)
        val result = SleepValidator.validateSleepRecord(record)
        assertTrue("Expected Error for startTime 61 min in future", result is ValidationResult.Error)
    }
}
