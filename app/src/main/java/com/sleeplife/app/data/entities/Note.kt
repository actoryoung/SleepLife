package com.sleeplife.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val mood: NoteMood,
    val tags: String = "",
    val isFavorite: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.Clock.System.now(),
    val updatedAt: LocalDateTime = LocalDateTime.Clock.System.now()
)

enum class NoteMood {
    VERY_HAPPY,
    HAPPY,
    NEUTRAL,
    SAD,
    ANXIOUS
}

fun NoteMood.getDisplayEmoji(): String {
    return when (this) {
        NoteMood.VERY_HAPPY -> "😄"
        NoteMood.HAPPY -> "🙂"
        NoteMood.NEUTRAL -> "😐"
        NoteMood.SAD -> "😔"
        NoteMood.ANXIOUS -> "😰"
    }
}
