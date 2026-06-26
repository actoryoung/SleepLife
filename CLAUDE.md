# CLAUDE.md

## Project Overview

SleepLife — Android 个人生活管理应用 (Kotlin + Jetpack Compose)

**包名**: `com.sleeplife.app`
**版本**: 1.0.0-alpha
**Min SDK**: API 26 / **Target SDK**: API 34

## 技术栈

| 类别 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Repository Pattern |
| 数据库 | Room (v2.6.1) |
| DI | Hilt (v2.48) |
| 异步 | Kotlin Coroutines + Flow |
| 导航 | Navigation Compose (v2.7.5) |
| 日期 | kotlinx-datetime (v0.5.0) |
| 图表 | MPAndroidChart (v3.1.0，尚未使用) |
| 测试 | JUnit 4 + Robolectric + MockK |

## 项目结构

```
app/src/main/java/com/sleeplife/app/
├── MainActivity.kt                     # 入口 Activity
├── SleepLifeApplication.kt             # @HiltAndroidApp
├── core/
│   ├── Result.kt                       # Result<T> 密封类 + AppException 体系
│   └── validators/                     # 各模块校验器
├── data/
│   ├── SleepLifeDatabase.kt            # Room 数据库 (version=1, destructive migration)
│   ├── Converters.kt                   # TypeConverters (LocalDateTime, 枚举序列化)
│   ├── dao/                            # SleepRecordDao, HabitDao, NoteDao, PomodoroSessionDao
│   ├── entities/                       # SleepRecord, Habit, HabitCheckIn, Note, PomodoroSession
│   └── repository/                     # 仓库层 (直接封装 DAO)
├── di/
│   └── DatabaseModule.kt               # Hilt 模块: 提供 Database + DAO 单例
└── ui/
    ├── navigation/Navigation.kt        # 底部导航 (Sleep/Habits/Pomodoro/Notes/More)
    ├── screens/                        # 各功能 Composable 界面
    ├── theme/                          # Color.kt, Theme.kt, Type.kt (深色主题)
    └── viewmodels/                     # ViewModel 层
```

## 构建命令

```bash
# 调试构建
./gradlew assembleDebug

# 运行 lint 检查
./gradlew lint

# 运行所有单元测试 (需要 JDK 17)
./gradlew test

# 运行所有 instrumented 测试 (需要模拟器)
./gradlew connectedAndroidTest

# 代码清理
./gradlew clean
```

**环境要求**: JDK 17 (`C:/Program Files/Java/jdk-17`), Android SDK, Gradle 8.2

## 架构模式

严格的 MVVM 单向数据流：

```
Screen (Composable) ←→ ViewModel (StateFlow<UiState>)
                         ViewModel ←→ Repository ←→ DAO ←→ Room DB
```

- ViewModel 暴露 `StateFlow<XxxUiState>`，UI 通过 `collectAsStateWithLifecycle()` 订阅
- ViewModel 内使用 `viewModelScope.launch` 处理异步操作
- Room DAO 返回 `List<Xxx>` 的方法标记 `suspend`，返回 `Flow<List<Xxx>>` 的方法用于实时观察
- 所有仓库方法是 DAO 的直接透传（尚未实现 `*WithValidation` 方法）

## 数据实体 (5 张表)

1. **SleepRecord** — 睡眠记录 (startTime, endTime, quality, notes)
2. **Habit** — 习惯定义 (name, description, icon, targetDays, color, isActive)
3. **HabitCheckIn** — 习惯打卡 (habitId FK, checkInDate, note)
4. **Note** — 笔记 (title, content, mood, tags, isFavorite)
5. **PomodoroSession** — 番茄钟会话 (taskName, duration, actualDuration, sessionType, completed)

**注意**: 所有 `LocalDateTime` 字段通过 TypeConverter 存为 String（ISO 格式），不是 UNIX 时间戳。

## 关键注意事项

1. **文档与代码不一致**: ARCHITECTURE.md 和 API_DOCS.md 描述的 `*WithValidation` 方法和 Timber 日志在当前代码中不存在，这些是计划中的功能，修改代码时以实际源文件为准。

2. **Release 构建失败**: AAPT2 无法处理 mipmap 中的启动图标 PNG，仅 Debug 构建通过。

3. **硬编码中文字符串**: UI 文本直接写在 Composable 中，`strings.xml` 极少使用，不要假设已国际化。

4. **`HabitDetailScreen` 返回按钮**: 使用了 `Icons.Default.Add` 而非返回箭头，存在 UI 问题。

5. **`SleepQuality.getSleepDurationHours()`**: 始终返回 `0f`，函数体未实现。

6. **`LocalDateTime.parse()` 反模式**: ViewModels 中的 `LocalDateTime.parse(now.toString(), ...)` 写法脆弱，优先直接传递 `Clock.System.now()` 或已构造好的 `LocalDateTime` 对象。

7. **DataStore 依赖已声明但未使用** — 不要添加 DataStore 相关代码除非明确需求。

## Git 规范

- Commit 信息使用英文，如 `Add command-line build support`
- Co-Authored-By 行: `Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>`
