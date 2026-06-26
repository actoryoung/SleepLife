package com.sleeplife.app.core.validators

import com.sleeplife.app.data.entities.PomodoroSession

/**
 * Validates PomodoroSession entities
 */
object PomodoroValidator {
    private const val MAX_TASK_NAME_LENGTH = 200
    private const val MIN_DURATION_MINUTES = 1
    private const val MAX_DURATION_MINUTES = 120
    
    fun validatePomodoroSession(session: PomodoroSession): ValidationResult {
        // Task name validation
        if (session.taskName.isBlank()) {
            return ValidationResult.Error("任务名称不能为空")
        }
        
        if (session.taskName.length > MAX_TASK_NAME_LENGTH) {
            return ValidationResult.Error("任务名称不能超过${MAX_TASK_NAME_LENGTH}字符")
        }
        
        // Duration validation
        if (session.duration < MIN_DURATION_MINUTES) {
            return ValidationResult.Error("时长至少为${MIN_DURATION_MINUTES}分钟")
        }
        
        if (session.duration > MAX_DURATION_MINUTES) {
            return ValidationResult.Error("时长不能超过${MAX_DURATION_MINUTES}分钟")
        }
        
        return ValidationResult.Valid
    }
    
    fun validateTaskName(taskName: String): ValidationResult {
        if (taskName.isBlank()) {
            return ValidationResult.Error("任务名称不能为空")
        }
        if (taskName.length > MAX_TASK_NAME_LENGTH) {
            return ValidationResult.Error("任务名称不能超过${MAX_TASK_NAME_LENGTH}字符")
        }
        return ValidationResult.Valid
    }
    
    fun validateDuration(duration: Int): ValidationResult {
        if (duration < MIN_DURATION_MINUTES) {
            return ValidationResult.Error("时长至少为${MIN_DURATION_MINUTES}分钟")
        }
        if (duration > MAX_DURATION_MINUTES) {
            return ValidationResult.Error("时长不能超过${MAX_DURATION_MINUTES}分钟")
        }
        return ValidationResult.Valid
    }
}
