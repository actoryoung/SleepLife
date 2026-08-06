# Resource Management Specification

> **Version**: 1.0  
> **Status**: Active  
> **Last Updated**: 2026-02-28

## Overview

This specification defines best practices for resource management in the SleepLife application, including string externalization, database migrations, logging configuration, and accessibility support.

## String Resources

### Goals

1. **Zero hard-coded strings**: All user-facing text in strings.xml
2. **Maintainability**: Easy to update text across app
3. **Localization-ready**: Foundation for future multi-language support
4. **Consistency**: Standardized naming and organization

### Organization Structure

```xml
<!-- strings.xml structure -->
<resources>
    <!-- App-wide strings -->
    <string name="app_name">SleepLife</string>
    
    <!-- Common strings -->
    <string name="common_save">保存</string>
    <string name="common_cancel">取消</string>
    <string name="common_delete">删除</string>
    <string name="common_retry">重试</string>
    <string name="common_loading">加载中...</string>
    
    <!-- Sleep module -->
    <string name="sleep_title">睡眠追踪</string>
    <string name="sleep_start">开始睡眠</string>
    <string name="sleep_stop">结束睡眠</string>
    <string name="sleep_quality">睡眠质量</string>
    <string name="sleep_notes">备注</string>
    <string name="sleep_history">历史记录</string>
    <string name="sleep_empty_message">还没有睡眠记录,点击下方按钮开始追踪睡眠</string>
    
    <!-- Sleep quality levels -->
    <string name="sleep_quality_excellent">极好</string>
    <string name="sleep_quality_good">好</string>
    <string name="sleep_quality_fair">一般</string>
    <string name="sleep_quality_poor">差</string>
    <string name="sleep_quality_very_poor">很差</string>
    
    <!-- Habit module -->
    <string name="habit_title">习惯打卡</string>
    <string name="habit_create">创建习惯</string>
    <string name="habit_name">习惯名称</string>
    <string name="habit_description">描述</string>
    <string name="habit_target_days">目标天数</string>
    <string name="habit_check_in">打卡</string>
    <string name="habit_progress">进度: %1$d/%2$d天</string>
    <string name="habit_empty_message">还没有习惯,创建一个新习惯开始打卡吧</string>
    
    <!-- Note module -->
    <string name="note_title">笔记日记</string>
    <string name="note_create">新建笔记</string>
    <string name="note_title_hint">标题</string>
    <string name="note_content_hint">开始书写...</string>
    <string name="note_mood">心情</string>
    <string name="note_tags">标签</string>
    <string name="note_favorite">收藏</string>
    <string name="note_search">搜索笔记</string>
    <string name="note_empty_message">还没有笔记,点击右下角创建第一条笔记</string>
    
    <!-- Pomodoro module -->
    <string name="pomodoro_title">专注时间</string>
    <string name="pomodoro_task">任务名称</string>
    <string name="pomodoro_duration">时长(分钟)</string>
    <string name="pomodoro_start">开始专注</string>
    <string name="pomodoro_pause">暂停</string>
    <string name="pomodoro_resume">继续</string>
    <string name="pomodoro_stop">停止</string>
    <string name="pomodoro_completed">已完成</string>
    <string name="pomodoro_today_focus">今日专注: %d分钟</string>
    <string name="pomodoro_empty_message">开始一个专注会话,提高工作效率</string>
    
    <!-- Error messages -->
    <string name="error_database">数据库错误,请稍后重试</string>
    <string name="error_unknown">未知错误,请稍后重试</string>
    <string name="error_network">网络连接失败</string>
    <string name="error_load_failed">数据加载失败</string>
    <string name="error_save_failed">保存失败,请重试</string>
    <string name="error_delete_failed">删除失败,请重试</string>
    
    <!-- Validation messages -->
    <string name="validation_required">此项为必填</string>
    <string name="validation_too_long">输入过长</string>
    <string name="validation_invalid_format">格式无效</string>
    <string name="validation_sleep_end_before_start">结束时间必须晚于开始时间</string>
    <string name="validation_sleep_duration_too_long">睡眠时长不能超过24小时</string>
    <string name="validation_habit_name_empty">习惯名称不能为空</string>
    <string name="validation_note_title_empty">标题不能为空</string>
    <string name="validation_pomodoro_task_empty">任务名称不能为空</string>
    
    <!-- Bottom navigation -->
    <string name="nav_sleep">睡眠</string>
    <string name="nav_habits">习惯</string>
    <string name="nav_notes">笔记</string>
    <string name="nav_pomodoro">专注</string>
    <string name="nav_more">更多</string>
    
    <!-- More screen -->
    <string name="more_title">更多功能</string>
    <string name="more_settings">设置</string>
    <string name="more_about">关于</string>
    <string name="more_version">版本 %s</string>
</resources>
```

### Naming Conventions

| Pattern | Example | Usage |
|---------|---------|-------|
| `<module>_<element>` | `sleep_title` | Module-specific strings |
| `common_<action>` | `common_save` | Shared action strings |
| `error_<type>` | `error_database` | Error messages |
| `validation_<rule>` | `validation_required` | Validation errors |
| `nav_<screen>` | `nav_sleep` | Navigation labels |

### Plurals

```xml
<plurals name="habit_days_count">
    <item quantity="one">%d天</item>
    <item quantity="other">%d天</item>
</plurals>
```

### Formatted Strings

```xml
<string name="pomodoro_today_focus">今日专注: %d分钟</string>
<string name="habit_progress">进度: %1$d/%2$d天</string>
```

Usage:
```kotlin
getString(R.string.pomodoro_today_focus, minutes)
getString(R.string.habit_progress, current, target)
```

## Database Migrations

### Migration Strategy

**Current State**: Version 1 with `fallbackToDestructiveMigration()`

**Target State**: Proper migration path for future versions

### Migration Implementation

```kotlin
@Database(
    entities = [
        SleepRecord::class,
        Habit::class,
        HabitCheckIn::class,
        Note::class,
        PomodoroSession::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SleepLifeDatabase : RoomDatabase() {
    // DAOs...
}

// Future migrations
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: Add new column
        database.execSQL("ALTER TABLE sleep_records ADD COLUMN dream_notes TEXT DEFAULT ''")
    }
}

// In DatabaseModule
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): SleepLifeDatabase {
    return Room.databaseBuilder(
        context,
        SleepLifeDatabase::class.java,
        "sleeplife_database"
    )
        .addMigrations(MIGRATION_1_2) // Add future migrations here
        .build()
}
```

### Schema Export

Enable schema export in build.gradle.kts:

```kotlin
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
}
```

This creates JSON schema files for each version, useful for migration testing.

### Migration Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SleepLifeDatabase::class.java
    )
    
    @Test
    fun migrate1To2() {
        // Create database at version 1
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data
            execSQL("INSERT INTO sleep_records (...) VALUES (...)")
            close()
        }
        
        // Migrate to version 2
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        
        // Verify migration succeeded
        helper.openDatabase(TEST_DB).apply {
            // Query and verify data
            val cursor = query("SELECT * FROM sleep_records")
            // Assertions...
            close()
        }
    }
}
```

## Logging Configuration

### Timber Setup

Already implemented in `SleepLifeApplication.kt`:

```kotlin
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

### Production Logging

For production, consider a custom tree that:
- Filters out debug/verbose logs
- Sends errors to crash reporting (e.g., Firebase Crashlytics)
- Respects user privacy (no PII in logs)

```kotlin
class ProductionTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.DEBUG || priority == Log.VERBOSE) {
            return // Don't log debug/verbose in production
        }
        
        // Send to crash reporting
        if (t != null) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }
    }
}

// In Application.onCreate()
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
} else {
    Timber.plant(ProductionTree())
}
```

### Log Tags

Use automatic tagging:

```kotlin
// Timber automatically uses class name as tag
Timber.d("Message") // Tag: SleepViewModel

// Or explicit tag
Timber.tag("CUSTOM_TAG").d("Message")
```

## Accessibility Support

### Content Descriptions

Add content descriptions to all interactive elements:

```kotlin
Icon(
    imageVector = Icons.Default.Add,
    contentDescription = stringResource(R.string.content_desc_add_sleep_record),
    modifier = Modifier.clickable { }
)
```

### Semantic Properties

```kotlin
Text(
    text = "睡眠时长: 8小时30分",
    modifier = Modifier.semantics {
        contentDescription = "睡眠时长八小时三十分钟"
    }
)
```

### Touch Target Size

Ensure all clickable elements ≥ 48dp:

```kotlin
IconButton(
    onClick = { },
    modifier = Modifier.size(48.dp) // Minimum touch target
) {
    Icon(...)
}
```

### Focus Order

```kotlin
Column(
    modifier = Modifier.semantics {
        isTraversalGroup = true
    }
) {
    // Elements will be traversed in order
}
```

### TalkBack Testing

Test with TalkBack enabled:
1. Settings → Accessibility → TalkBack → Enable
2. Navigate app using swipe gestures
3. Verify all elements are announced correctly
4. Verify interaction works with TalkBack

## Build Configuration

### ProGuard Rules

For production builds, ensure ProGuard rules preserve necessary classes:

```proguard
# Timber
-dontwarn org.jetbrains.annotations.**

# Room
-keepdatabase class com.sleeplife.app.data.SleepLifeDatabase
-keep @androidx.room.Entity class *
-keep class com.sleeplife.app.data.entities.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
```

## Acceptance Criteria

### String Resources
- [ ] No hard-coded strings in Compose code
- [ ] All user-facing text in strings.xml
- [ ] Consistent naming convention
- [ ] Plurals and formatted strings working

### Database
- [ ] Schema export enabled
- [ ] Migration framework ready (even if no migrations yet)
- [ ] Database version documented

### Logging
- [ ] Timber initialized in Application
- [ ] Debug logging in debug builds only
- [ ] Error logging throughout app
- [ ] No PII in logs

### Accessibility
- [ ] All interactive elements have contentDescription
- [ ] Touch targets ≥ 48dp
- [ ] App usable with TalkBack
- [ ] Semantic properties on key elements

## References

- [Android String Resources](https://developer.android.com/guide/topics/resources/string-resource)
- [Room Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Timber](https://github.com/JakeWharton/timber)
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility)
- [Compose Accessibility](https://developer.android.com/jetpack/compose/accessibility)
