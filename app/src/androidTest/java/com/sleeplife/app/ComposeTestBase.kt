package com.sleeplife.app

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule

/**
 * Base class for Compose UI tests that provides:
 * - ComposeTestRule for UI testing
 * - Common UI testing utilities
 */
abstract class ComposeTestBase {

    @get:Rule
    val composeTestRule: ComposeTestRule = createComposeRule()

    /**
     * Wait for idle state in Compose
     */
    protected fun waitForIdle() {
        composeTestRule.waitForIdle()
    }

    /**
     * Wait for a condition to be true with timeout
     */
    protected fun waitUntil(
        timeoutMillis: Long = 3000,
        condition: () -> Boolean
    ) {
        composeTestRule.waitUntil(timeoutMillis) {
            condition()
        }
    }
}
