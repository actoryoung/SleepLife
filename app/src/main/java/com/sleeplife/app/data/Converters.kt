package com.sleeplife.app.data

import androidx.room.TypeConverter
import com.sleeplife.app.data.entities.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat

class Converters {
    private val formatter = DateTimeFormat.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let {
            try {
                LocalDateTime.parse(it, formatter)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun fromSleepQuality(quality: SleepQuality): String {
        return quality.name
    }

    @TypeConverter
    fun toSleepQuality(value: String): SleepQuality {
        return try {
            SleepQuality.valueOf(value)
        } catch (e: Exception) {
            SleepQuality.AVERAGE
        }
    }

    @TypeConverter
    fun fromNoteMood(mood: NoteMood): String {
        return mood.name
    }

    @TypeConverter
    fun toNoteMood(value: String): NoteMood {
        return try {
            NoteMood.valueOf(value)
        } catch (e: Exception) {
            NoteMood.NEUTRAL
        }
    }

    @TypeConverter
    fun fromSessionType(type: SessionType): String {
        return type.name
    }

    @TypeConverter
    fun toSessionType(value: String): SessionType {
        return try {
            SessionType.valueOf(value)
        } catch (e: Exception) {
            SessionType.WORK
        }
    }
}
