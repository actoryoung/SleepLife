package com.sleeplife.app.core.validators

import com.sleeplife.app.data.entities.Note

/**
 * Validates Note entities
 */
object NoteValidator {
    private const val MAX_TITLE_LENGTH = 200
    private const val MAX_CONTENT_LENGTH = 10000
    private const val MAX_TAG_LENGTH = 50
    private const val MAX_TAGS_COUNT = 10
    
    fun validateNote(note: Note): ValidationResult {
        // Title validation
        if (note.title.isBlank()) {
            return ValidationResult.Error("标题不能为空")
        }
        
        if (note.title.length > MAX_TITLE_LENGTH) {
            return ValidationResult.Error("标题不能超过${MAX_TITLE_LENGTH}字符")
        }
        
        // Content validation
        if (note.content.isBlank()) {
            return ValidationResult.Error("内容不能为空")
        }
        
        if (note.content.length > MAX_CONTENT_LENGTH) {
            return ValidationResult.Error("内容不能超过${MAX_CONTENT_LENGTH}字符")
        }
        
        // Tags validation
        if (note.tags.isNotEmpty()) {
            val tagList = note.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            
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
    
    fun validateTitle(title: String): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult.Error("标题不能为空")
        }
        if (title.length > MAX_TITLE_LENGTH) {
            return ValidationResult.Error("标题不能超过${MAX_TITLE_LENGTH}字符")
        }
        return ValidationResult.Valid
    }
}
