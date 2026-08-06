# Input Validation Specification

> **Version**: 1.0  
> **Status**: Active  
> **Last Updated**: 2026-02-28

## Overview

This specification defines comprehensive input validation rules for all user inputs in the SleepLife application, ensuring data integrity, preventing invalid states, and providing helpful user feedback.

## Goals

1. **Data Integrity**: Prevent invalid data from entering the database
2. **Early Detection**: Validate at the point of input
3. **Clear Feedback**: Provide specific, actionable error messages
4. **Consistent Validation**: Centralized validation logic for reusability

## Validation Architecture

### Validation Result

```kotlin
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

### Validator Pattern

```kotlin
object SleepValidator {
    fun validateSleepRecord(record: SleepRecord): ValidationResult {
        // Validation logic
    }
    
    fun validateStartTime(startTime: LocalDateTime): ValidationResult {
        // Individual field validation
    }
}
```

## Sleep Module Validation

### SleepRecord Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| startTime | Not null | "开始时间不能为空" |
| endTime | Not null | "结束时间不能为空" |
| endTime | > startTime | "结束时间必须晚于开始时间" |
| duration | ≤ 24 hours | "睡眠时长不能超过24小时" |
| duration | ≥ 5 minutes | "睡眠时长必须至少5分钟" |
| startTime | ≤ now + 1 hour | "开始时间不能在未来" |
| quality | Valid enum | "睡眠质量无效" |
| notes | ≤ 500 chars | "备注不能超过500字符" |

### Implementation

```kotlin
object SleepValidator {
    private const val MAX_NOTES_LENGTH = 500
    private const val MAX_SLEEP_HOURS = 24
    private const val MIN_SLEEP_MINUTES = 5
    
    fun validateSleepRecord(record: SleepRecord): ValidationResult {
        // Check time ordering
        if (record.endTime <= record.startTime) {
            return ValidationResult.Error("结束时间必须晚于开始时间")
        }
        
        // Check duration
        val duration = record.endTime.toInstant(TimeZone.currentSystemDefault()) -
                       record.startTime.toInstant(TimeZone.currentSystemDefault())
        
        if (duration.inWholeHours > MAX_SLEEP_HOURS) {
            return ValidationResult.Error("睡眠时长不能超过${MAX_SLEEP_HOURS}小时")
        }
        
        if (duration.inWholeMinutes < MIN_SLEEP_MINUTES) {
            return ValidationResult.Error("睡眠时长必须至少${MIN_SLEEP_MINUTES}分钟")
        }
        
        // Check future time
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        if (record.startTime > now.plus(1, DateTimeUnit.HOUR)) {
            return ValidationResult.Error("开始时间不能在未来")
        }
        
        // Check notes length
        if (record.notes.length > MAX_NOTES_LENGTH) {
            return ValidationResult.Error("备注不能超过${MAX_NOTES_LENGTH}字符")
        }
        
        return ValidationResult.Valid
    }
}
```

### Test Cases

- ✅ Valid sleep record passes
- ✅ End before start fails
- ✅ Duration > 24 hours fails
- ✅ Duration < 5 minutes fails
- ✅ Future start time fails
- ✅ Notes > 500 chars fails

## Habit Module Validation

### Habit Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| name | Not empty | "习惯名称不能为空" |
| name | ≤ 100 chars | "习惯名称不能超过100字符" |
| description | ≤ 500 chars | "描述不能超过500字符" |
| targetDays | ≥ 1 | "目标天数至少为1天" |
| targetDays | ≤ 365 | "目标天数不能超过365天" |
| iconName | Valid icon | "图标名称无效" |
| color | Valid color | "颜色值无效" |

### Implementation

```kotlin
object HabitValidator {
    private const val MAX_NAME_LENGTH = 100
    private const val MAX_DESCRIPTION_LENGTH = 500
    private const val MIN_TARGET_DAYS = 1
    private const val MAX_TARGET_DAYS = 365
    
    private val VALID_ICONS = setOf(
        "fitness_center", "book", "water_drop", "self_improvement",
        "directions_run", "nightlight", "restaurant", "code"
    )
    
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
        
        // Icon validation
        if (habit.iconName !in VALID_ICONS) {
            return ValidationResult.Error("图标名称无效")
        }
        
        return ValidationResult.Valid
    }
}
```

### Test Cases

- ✅ Valid habit passes
- ✅ Empty name fails
- ✅ Name > 100 chars fails
- ✅ Description > 500 chars fails
- ✅ Target days < 1 fails
- ✅ Target days > 365 fails
- ✅ Invalid icon fails

## Note Module Validation

### Note Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| title | Not empty | "标题不能为空" |
| title | ≤ 200 chars | "标题不能超过200字符" |
| content | ≤ 10000 chars | "内容不能超过10000字符" |
| mood | Valid emoji | "心情表情无效" |
| tags | Each tag ≤ 50 chars | "标签不能超过50字符" |
| tags | ≤ 10 tags | "最多只能添加10个标签" |

### Implementation

```kotlin
object NoteValidator {
    private const val MAX_TITLE_LENGTH = 200
    private const val MAX_CONTENT_LENGTH = 10000
    private const val MAX_TAG_LENGTH = 50
    private const val MAX_TAGS_COUNT = 10
    
    private val VALID_MOODS = setOf("😊", "😢", "😡", "😌", "😴")
    
    fun validateNote(note: Note): ValidationResult {
        // Title validation
        if (note.title.isBlank()) {
            return ValidationResult.Error("标题不能为空")
        }
        
        if (note.title.length > MAX_TITLE_LENGTH) {
            return ValidationResult.Error("标题不能超过${MAX_TITLE_LENGTH}字符")
        }
        
        // Content validation
        if (note.content.length > MAX_CONTENT_LENGTH) {
            return ValidationResult.Error("内容不能超过${MAX_CONTENT_LENGTH}字符")
        }
        
        // Mood validation
        if (note.mood.isNotEmpty() && note.mood !in VALID_MOODS) {
            return ValidationResult.Error("心情表情无效")
        }
        
        // Tags validation
        if (note.tags.isNotEmpty()) {
            val tagList = note.tags.split(",").map { it.trim() }
            
            if (tagList.size > MAX_TAGS_COUNT) {
                return ValidationResult.Error("最多只能添加${MAX_TAGS_COUNT}个标签")
            }
            
            tagList.forEach { tag ->
                if (tag.length > MAX_TAG_LENGTH) {
                    return ValidationResult.Error("标签不能超过${MAX_TAG_LENGTH}字符")
                }
            }
        }
        
        return ValidationResult.Valid
    }
}
```

### Test Cases

- ✅ Valid note passes
- ✅ Empty title fails
- ✅ Title > 200 chars fails
- ✅ Content > 10000 chars fails
- ✅ Invalid mood fails
- ✅ Tags > 10 fails
- ✅ Tag > 50 chars fails

## Pomodoro Module Validation

### PomodoroSession Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| taskName | Not empty | "任务名称不能为空" |
| taskName | ≤ 200 chars | "任务名称不能超过200字符" |
| duration | ≥ 1 minute | "时长至少为1分钟" |
| duration | ≤ 120 minutes | "时长不能超过120分钟" |
| sessionType | Valid type | "会话类型无效" |

### Implementation

```kotlin
object PomodoroValidator {
    private const val MAX_TASK_NAME_LENGTH = 200
    private const val MIN_DURATION_MINUTES = 1
    private const val MAX_DURATION_MINUTES = 120
    
    private val VALID_SESSION_TYPES = setOf("Work", "Short Break", "Long Break")
    
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
        
        // Session type validation
        if (session.sessionType !in VALID_SESSION_TYPES) {
            return ValidationResult.Error("会话类型无效")
        }
        
        return ValidationResult.Valid
    }
}
```

### Test Cases

- ✅ Valid session passes
- ✅ Empty task name fails
- ✅ Task name > 200 chars fails
- ✅ Duration < 1 minute fails
- ✅ Duration > 120 minutes fails
- ✅ Invalid session type fails

## UI Integration

### Form Validation Display

```kotlin
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    validationError: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = validationError != null,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (validationError != null) {
            Text(
                text = validationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
```

### Real-time Validation

- Validate on blur (when field loses focus)
- Validate on submit (when form is submitted)
- Show errors inline below the field
- Disable submit button if validation fails

## Acceptance Criteria

- [ ] All 4 validators implemented
- [ ] All validation rules tested
- [ ] Validators integrated into Repositories
- [ ] UI shows inline validation errors
- [ ] Submit buttons disabled when validation fails
- [ ] No invalid data can be saved to database

## References

- [Android Form Validation](https://developer.android.com/guide/topics/text/autofill-optimize)
- [Compose TextField Validation](https://developer.android.com/jetpack/compose/text)
