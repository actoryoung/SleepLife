package com.sleeplife.app.di

import android.content.Context
import com.sleeplife.app.data.SleepLifeDatabase
import com.sleeplife.app.data.dao.HabitDao
import com.sleeplife.app.data.dao.NoteDao
import com.sleeplife.app.data.dao.PomodoroSessionDao
import com.sleeplife.app.data.dao.SleepRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SleepLifeDatabase {
        return SleepLifeDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideSleepRecordDao(database: SleepLifeDatabase): SleepRecordDao {
        return database.sleepRecordDao()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: SleepLifeDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: SleepLifeDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun providePomodoroSessionDao(database: SleepLifeDatabase): PomodoroSessionDao {
        return database.pomodoroSessionDao()
    }
}
