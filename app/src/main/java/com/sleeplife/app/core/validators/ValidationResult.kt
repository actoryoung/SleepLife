package com.sleeplife.app.core.validators

/**
 * 业务验证操作的结果。
 *
 * Result of a validation operation.
 *
 * 用于表示业务规则验证的两种结果状态：
 * - **Valid**: 验证通过
 * - **Error**: 验证失败，携带失败原因
 *
 * ## 使用示例
 *
 * ```kotlin
 * val result = SleepValidator.validateSleepRecord(record)
 * when (result) {
 *     is ValidationResult.Valid -> println("验证通过")
 *     is ValidationResult.Error -> println("验证失败: ${result.message}")
 * }
 * ```
 *
 * @see SleepValidator
 * @see HabitValidator
 * @see NoteValidator
 * @see PomodoroValidator
 * @since 1.0.0
 */
sealed class ValidationResult {
    /**
     * 验证通过状态。
     *
     * 表示所有业务规则检查都通过，数据有效。
     */
    object Valid : ValidationResult()

    /**
     * 验证失败状态。
     *
     * @param message 验证失败的具体原因（面向用户的友好提示）
     */
    data class Error(val message: String) : ValidationResult()
}
