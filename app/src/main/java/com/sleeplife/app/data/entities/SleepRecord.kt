package com.sleeplife.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat

@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val quality: SleepQuality,
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.parse(
        LocalDateTime.Clock.System.now().toString(),
        DateTimeFormat.ISO_LOCAL_DATE_TIME
    )
)

enum class SleepQuality {
    VERY_POOR,
    POOR,
    AVERAGE,
    GOOD,
    EXCELLENT
}

fun SleepQuality.getDisplayString(): String {
    return when (this) {
        SleepQuality.VERY_POOR -> "很差"
        SleepQuality.POOR -> "较差"
        SleepQuality.AVERAGE -> "一般"
        SleepQuality.GOOD -> "良好"
        SleepQuality.EXCELLENT -> "极佳"
    }
}

fun SleepQuality.getSleepDurationHours(): Float {
    return when (this) {
        SleepQuality.VERY_POOR -> 0f
        SleepQuality.POOR -> 0f
        SleepQuality.AVERAGE -> 0f
        SleepQuality.GOOD -> 0f
        SleepQuality.EXCELLENT -> 0f
    }
}
