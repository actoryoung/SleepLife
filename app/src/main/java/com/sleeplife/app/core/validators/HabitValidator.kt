package com.sleeplife.app.core.validators

import com.sleeplife.app.data.entities.Habit

/**
 * Validates Habit entities
 */
object HabitValidator {
    private const val MAX_NAME_LENGTH = 100
    private const val MAX_DESCRIPTION_LENGTH = 500
    private const val MIN_TARGET_DAYS = 1
    private const val MAX_TARGET_DAYS = 365
    
    fun validateHabit(habit: Habit): ValidationResult {
        // Name validation
        if (habit.name.isBlank()) {
            return ValidationResult.Error("习惯名称不能为空")
        }
        
        if (habit.name.length > MAX_NAME_LENGTH) {
            return ValidationResult.Error("习惯名称不能超过${MAX_NAME_LENGTH}字符")
        }
        
        // Description validation
        if (habit.description.length > MAX_DESCRIPTION_LENGTH) {
            return ValidationResult.Error("描述不能超过${MAX_DESCRIPTION_LENGTH}字符")
        }
        
        // Target days validation
        if (habit.targetDays < MIN_TARGET_DAYS) {
            return ValidationResult.Error("目标天数至少为${MIN_TARGET_DAYS}天")
        }
        
        if (habit.targetDays > MAX_TARGET_DAYS) {
            return ValidationResult.Error("目标天数不能超过${MAX_TARGET_DAYS}天")
        }
        
        return ValidationResult.Valid
    }
    
    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error("习惯名称不能为空")
        }
        if (name.length > MAX_NAME_LENGTH) {
            return ValidationResult.Error("习惯名称不能超过${MAX_NAME_LENGTH}字符")
        }
        return ValidationResult.Valid
    }
}
