package com.sleeplife.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sleeplife.app.data.SleepLifeDatabase
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Base class for Repository tests that provides:
 * - In-memory Room database
 * - DAO instances
 * - Database cleanup
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
abstract class RepositoryTestBase : TestBase() {

    protected lateinit var database: SleepLifeDatabase

    @Before
    override fun setUp() {
        super.setUp()
        
        // Create an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SleepLifeDatabase::class.java
        )
            .allowMainThreadQueries() // Allow queries on main thread in tests
            .build()
    }

    @After
    override fun tearDown() {
        super.tearDown()
        
        // Close the database
        database.close()
    }

    // Convenience methods for accessing DAOs
    protected val sleepDao get() = database.sleepRecordDao()
    protected val habitDao get() = database.habitDao()
    protected val noteDao get() = database.noteDao()
    protected val pomodoroDao get() = database.pomodoroSessionDao()
}
