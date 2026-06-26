package com.sleeplife.app.core.validators

import com.sleeplife.app.data.entities.SleepRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * 睡眠记录业务验证器。
 *
 * Sleep record business rules validator.
 *
 * 负责验证 [SleepRecord] 实体的业务规则，确保数据符合应用逻辑约束。
 *
 * ## 验证规则
 *
 * 1. **时间有效性**:
 *    - 结束时间必须晚于开始时间
 *    - 开始时间不能超过当前时间 1 小时（容错未来时间）
 *
 * 2. **备注长度**: 不超过 500 字符
 *
 * ## 使用示例
 *
 * ```kotlin
 * val record = SleepRecord(startTime = ..., endTime = ..., notes = "...")
 * val result = SleepValidator.validateSleepRecord(record)
 *
 * when (result) {
 *     is ValidationResult.Valid -> {
 *         // 验证通过，可以插入数据库
 *         repository.insertSleepRecord(record)
 *     }
 *     is ValidationResult.Error -> {
 *         // 验证失败，显示错误信息给用户
 *         showError(result.message)
 *     }
 * }
 * ```
 *
 * @see SleepRecord
 * @see ValidationResult
 * @since 1.0.0
 */
object SleepValidator {
    /** 备注最大长度（字符数） */
    private const val MAX_NOTES_LENGTH = 500
    /** 最大睡眠时长（小时） */
    private const val MAX_SLEEP_HOURS = 24
    /** 最小睡眠时长（分钟） */
    private const val MIN_SLEEP_MINUTES = 5

    /**
     * 验证睡眠记录的完整性和合法性。
     *
     * 执行以下验证：
     * 1. 时间顺序：结束时间 > 开始时间
     * 2. 时间合理性：开始时间不超过当前时间 1 小时（容错时钟偏差）
     * 3. 备注长度：不超过 500 字符
     *
     * @param record 待验证的睡眠记录
     * @return [ValidationResult.Valid] 如果验证通过，否则返回 [ValidationResult.Error] 包含错误信息
     *
     * @see SleepRecord
     * @since 1.0.0
     */
    fun validateSleepRecord(record: SleepRecord): ValidationResult {
        // Check time ordering (endTime is nullable)
        if (record.endTime == null) {
            return ValidationResult.Error("结束时间不能为空")
        }
        if (record.endTime <= record.startTime) {
            return ValidationResult.Error("结束时间必须晚于开始时间")
        }
        
        // Calculate duration
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        // Check future start time (within 1 hour tolerance)
        val startInstant = record.startTime.toInstant(TimeZone.currentSystemDefault())
        val nowInstant = now.toInstant(TimeZone.currentSystemDefault())
        val oneHourLater = nowInstant.plus(kotlin.time.Duration.parse("1h"))
        
        if (startInstant > oneHourLater) {
            return ValidationResult.Error("开始时间不能超过1小时后")
        }
        
        // Check notes length
        if (record.notes.length > MAX_NOTES_LENGTH) {
            return ValidationResult.Error("备注不能超过${MAX_NOTES_LENGTH}字符")
        }
        
        return ValidationResult.Valid
    }

}
