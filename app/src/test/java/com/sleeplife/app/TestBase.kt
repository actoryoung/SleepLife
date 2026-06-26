package com.sleeplife.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule

/**
 * Base class for unit tests that provides:
 * - Coroutine test dispatcher setup
 * - InstantTaskExecutorRule for LiveData/Flow testing
 * - Common setup/teardown
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class TestBase {

    /**
     * Executes each task synchronously using Architecture Components.
     * Useful for testing LiveData and Flow.
     */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * Test dispatcher for coroutines
     */
    protected val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    open fun setUp() {
        // Set the main dispatcher to a test dispatcher
        Dispatchers.setMain(testDispatcher)
    }

    @After
    open fun tearDown() {
        // Reset the main dispatcher
        Dispatchers.resetMain()
    }
}
