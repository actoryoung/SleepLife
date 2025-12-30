package com.sleeplife.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sleeplife.app.data.dao.*
import com.sleeplife.app.data.entities.*

@Database(
    entities = [
        SleepRecord::class,
        Habit::class,
        HabitCheckIn::class,
        Note::class,
        PomodoroSession::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SleepLifeDatabase : RoomDatabase() {
    abstract fun sleepRecordDao(): SleepRecordDao
    abstract fun habitDao(): HabitDao
    abstract fun noteDao(): NoteDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    companion object {
        private const val DATABASE_NAME = "sleeplife_database"

        @Volatile
        private var INSTANCE: SleepLifeDatabase? = null

        fun getDatabase(context: Context): SleepLifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepLifeDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
