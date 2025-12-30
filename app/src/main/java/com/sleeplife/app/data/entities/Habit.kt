package com.sleeplife.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val icon: String = "📌",
    val targetDays: Int = 30,
    val color: Long = 0xFF6750A4,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.Clock.System.now()
)

@Entity(tableName = "habit_checkins")
data class HabitCheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val checkInDate: LocalDateTime,
    val note: String = "",
    val completedAt: LocalDateTime = LocalDateTime.Clock.System.now()
)
