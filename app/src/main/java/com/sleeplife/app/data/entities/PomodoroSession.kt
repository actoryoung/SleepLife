package com.sleeplife.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import kotlin.math.min

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskName: String,
    val duration: Int, // in minutes
    val actualDuration: Int, // actual time completed
    val sessionType: SessionType,
    val completed: Boolean = false,
    val interrupted: Boolean = false,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null
)

enum class SessionType {
    WORK,
    SHORT_BREAK,
    LONG_BREAK
}

fun SessionType.getDisplayName(): String {
    return when (this) {
        SessionType.WORK -> "专注时间"
        SessionType.SHORT_BREAK -> "短休息"
        SessionType.LONG_BREAK -> "长休息"
    }
}

fun SessionType.getDefaultDuration(): Int {
    return when (this) {
        SessionType.WORK -> 25
        SessionType.SHORT_BREAK -> 5
        SessionType.LONG_BREAK -> 15
    }
}

fun SessionType.getDefaultColor(): androidx.compose.ui.graphics.Color {
    return when (this) {
        SessionType.WORK -> androidx.compose.ui.graphics.Color(0xFFE53935)
        SessionType.SHORT_BREAK -> androidx.compose.ui.graphics.Color(0xFF43A047)
        SessionType.LONG_BREAK -> androidx.compose.ui.graphics.Color(0xFF1E88E5)
    }
}
