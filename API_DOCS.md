# SleepLife API 文档索引

**版本**: 1.0.0-alpha  
**更新日期**: 2026-02-28

这是 SleepLife Android 应用的完整文档索引。所有核心类都已添加详细的 KDoc 注释。

---

## 📚 文档导航

### 项目文档

| 文档 | 描述 | 路径 |
|------|------|------|
| **README** | 项目概述和快速开始 | [README.md](README.md) |
| **架构设计** | 完整的架构设计文档 | [ARCHITECTURE.md](ARCHITECTURE.md) |
| **项目状态** | 开发进度和任务追踪 | [PROJECT_STATUS.md](PROJECT_STATUS.md) |
| **构建指南** | 命令行构建说明 | [BUILD.md](BUILD.md) |
| **API 文档** | 本文档 | [API_DOCS.md](API_DOCS.md) |

### 开发者指南

- 🏗️ **架构模式**: 查看 [ARCHITECTURE.md](ARCHITECTURE.md) 了解 MVVM + Repository Pattern
- 🧪 **测试策略**: 查看 [ARCHITECTURE.md#测试策略](ARCHITECTURE.md#测试策略)
- 🎨 **代码风格**: 遵循 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

---

## 🔍 核心 API 参考

### 错误处理基础设施

#### Result<T> 包装器

**文件**: `app/src/main/java/com/sleeplife/app/core/Result.kt`

类型安全的操作结果包装器，支持三种状态：Success、Error、Loading。

**核心类**:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T)
    data class Error(val exception: AppException)
    class Loading<T>
}
```

**工具方法**:
- `map()` - 转换成功结果
- `flatMap()` - 链式调用可能失败的操作
- `getOrNull()` - 安全获取数据（失败返回 null）
- `isSuccess()`, `isError()`, `isLoading()` - 状态检查

**使用示例**:
```kotlin
when (val result = repository.insertItem(item)) {
    is Result.Success -> println("ID: ${result.data}")
    is Result.Error -> println("错误: ${result.exception.message}")
    is Result.Loading -> println("加载中...")
}
```

#### AppException 层次结构

**文件**: `app/src/main/java/com/sleeplife/app/core/Result.kt`

分类错误类型的异常层次结构。

**异常类型**:

| 异常类 | 用途 | 日志级别 | 用户提示 |
|--------|------|---------|---------|
| `ValidationException` | 输入验证失败 | WARN | 具体验证失败原因 |
| `DatabaseException` | 数据库操作错误 | ERROR | 通用错误提示 |
| `UnknownException` | 未预期的异常 | ERROR | 通用错误提示 |

### 业务验证器

#### ValidationResult

**文件**: `app/src/main/java/com/sleeplife/app/core/validators/ValidationResult.kt`

业务验证操作的结果。

```kotlin
sealed class ValidationResult {
    object Valid              // 验证通过
    data class Error(val message: String)  // 验证失败
}
```

#### SleepValidator

**文件**: `app/src/main/java/com/sleeplife/app/core/validators/SleepValidator.kt`

睡眠记录业务验证器。

**验证规则**:
- 结束时间 > 开始时间
- 开始时间不超过当前时间 1 小时
- 备注长度 ≤ 500 字符

**API**:
```kotlin
fun validateSleepRecord(record: SleepRecord): ValidationResult
```

#### HabitValidator

**文件**: `app/src/main/java/com/sleeplife/app/core/validators/HabitValidator.kt`

习惯验证器。

**验证规则**:
- 习惯名称非空且长度 ≤ 50 字符
- 目标天数 > 0 且 ≤ 365 天

#### NoteValidator

**文件**: `app/src/main/java/com/sleeplife/app/core/validators/NoteValidator.kt`

笔记验证器。

**验证规则**:
- 标题非空且长度 ≤ 100 字符
- 内容长度 ≤ 10000 字符

#### PomodoroValidator

**文件**: `app/src/main/java/com/sleeplife/app/core/validators/PomodoroValidator.kt`

番茄钟会话验证器。

**验证规则**:
- 任务名称非空且长度 ≤ 100 字符
- 时长 1-120 分钟

---

## 📦 数据层 API

### 数据库实体

#### SleepRecord

**文件**: `app/src/main/java/com/sleeplife/app/data/entities/SleepRecord.kt`

睡眠记录实体。

```kotlin
@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val duration: Int = 0,        // 分钟
    val quality: Int = 3,          // 1-5
    val notes: String = ""
)
```

#### Habit & HabitCheckIn

**文件**: `app/src/main/java/com/sleeplife/app/data/entities/Habit.kt`

习惯及打卡记录。

```kotlin
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetDays: Int,
    val createdAt: LocalDateTime
)

@Entity(tableName = "habit_checkins")
data class HabitCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val note: String = ""
)
```

#### Note

**文件**: `app/src/main/java/com/sleeplife/app/data/entities/Note.kt`

笔记实体。

```kotlin
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val mood: NoteMood = NoteMood.NEUTRAL,
    val tags: String = "",
    val isFavorite: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class NoteMood { HAPPY, SAD, NEUTRAL, EXCITED, ANGRY }
```

#### PomodoroSession

**文件**: `app/src/main/java/com/sleeplife/app/data/entities/PomodoroSession.kt`

番茄钟会话实体。

```kotlin
@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskName: String,
    val duration: Int,             // 计划时长（分钟）
    val actualDuration: Int,       // 实际时长（分钟）
    val sessionType: SessionType = SessionType.WORK,
    val completed: Boolean = false,
    val interrupted: Boolean = false,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null
)

enum class SessionType { WORK, BREAK, LONG_BREAK }
```

### Repository 层

所有 Repository 类提供两种方法类型：

1. **Flow 查询方法**（原始方法）:
   - 返回 `Flow<Entity>` 用于响应式 UI 更新
   - 不抛出异常，数据库变化自动通知

2. **WithValidation 修改方法**（新增方法）:
   - 返回 `Result<T>` 用于错误处理
   - 集成业务验证器
   - 完整的异常处理和日志

#### SleepRepository

**文件**: `app/src/main/java/com/sleeplife/app/data/repository/SleepRepository.kt`

**查询方法**:
```kotlin
fun getTodaySleepRecord(): Flow<SleepRecord?>
fun getRecentSleepRecords(days: Int): Flow<List<SleepRecord>>
fun getAllSleepRecords(): Flow<List<SleepRecord>>
```

**修改方法（带验证）**:
```kotlin
suspend fun insertSleepRecordWithValidation(record: SleepRecord): Result<Long>
suspend fun updateSleepRecordWithValidation(record: SleepRecord): Result<Unit>
suspend fun deleteSleepRecordsWithValidation(id: Long): Result<Unit>
```

#### HabitRepository

**文件**: `app/src/main/java/com/sleeplife/app/data/repository/HabitRepository.kt`

**查询方法**:
```kotlin
fun getAllHabitsWithCheckIns(): Flow<List<HabitWithCheckIns>>
fun getHabitById(id: Long): Flow<Habit?>
fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>>
```

**修改方法（带验证）**:
```kotlin
suspend fun insertHabitWithValidation(habit: Habit): Result<Long>
suspend fun updateHabitWithValidation(habit: Habit): Result<Unit>
suspend fun deleteHabitWithValidation(id: Long, habit: Habit): Result<Unit>
suspend fun insertCheckInWithValidation(checkIn: HabitCheckIn): Result<Long>
suspend fun deleteCheckInWithValidation(id: Long, checkIn: HabitCheckIn): Result<Unit>
```

#### NoteRepository

**文件**: `app/src/main/java/com/sleeplife/app/data/repository/NoteRepository.kt`

**查询方法**:
```kotlin
fun getAllNotes(): Flow<List<Note>>
fun getFavoriteNotes(): Flow<List<Note>>
fun getNoteById(id: Long): Flow<Note?>
fun searchNotes(query: String): Flow<List<Note>>
```

**修改方法（带验证）**:
```kotlin
suspend fun insertNoteWithValidation(note: Note): Result<Long>
suspend fun updateNoteWithValidation(note: Note): Result<Unit>
suspend fun deleteNoteWithValidation(id: Long, note: Note): Result<Unit>
suspend fun toggleFavoriteWithValidation(id: Long): Result<Unit>
```

#### PomodoroRepository

**文件**: `app/src/main/java/com/sleeplife/app/data/repository/PomodoroRepository.kt`

**查询方法**:
```kotlin
fun getTodaySessions(): Flow<List<PomodoroSession>>
fun getAllSessions(): Flow<List<PomodoroSession>>
suspend fun getTodayFocusTime(): Int
```

**修改方法（带验证）**:
```kotlin
suspend fun insertSessionWithValidation(session: PomodoroSession): Result<Long>
suspend fun updateSessionWithValidation(session: PomodoroSession): Result<Unit>
suspend fun deleteSessionWithValidation(id: Long, session: PomodoroSession): Result<Unit>
```

---

## 🎨 Presentation 层 API

### ViewModels

所有 ViewModel 遵循统一模式：

1. **UiState 模式**: 单一状态对象管理 UI
2. **错误处理**: 所有操作集成 Result 模式
3. **StateFlow**: 响应式状态流
4. **Coroutines**: viewModelScope 管理生命周期

#### SleepViewModel

**文件**: `app/src/main/java/com/sleeplife/app/ui/viewmodels/SleepViewModel.kt`

**UiState**:
```kotlin
data class SleepUiState(
    val isTracking: Boolean = false,
    val todayRecord: SleepRecord? = null,
    val recentRecords: List<SleepRecord> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

**公共 API**:
```kotlin
val uiState: StateFlow<SleepUiState>
val todayRecord: StateFlow<SleepRecord?>
val recentRecords: StateFlow<List<SleepRecord>>

fun startSleepTracking()
fun stopSleepTracking()
fun deleteSleepRecord(id: Long)
fun addSleepRecord(startTime: LocalDateTime, endTime: LocalDateTime, quality: Int, notes: String)
fun clearErrorMessage()
```

#### HabitsViewModel

**文件**: `app/src/main/java/com/sleeplife/app/ui/viewmodels/HabitsViewModel.kt`

**UiState**:
```kotlin
data class HabitsUiState(
    val habits: List<HabitWithCheckIns> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

**公共 API**:
```kotlin
val uiState: StateFlow<HabitsUiState>
val allHabits: StateFlow<List<HabitWithCheckIns>>

fun addHabit(name: String, targetDays: Int)
fun checkInHabit(habitId: Long)
fun undoCheckIn(habitId: Long)
fun deleteHabit(habitId: Long)
fun clearErrorMessage()
```

#### NotesViewModel

**文件**: `app/src/main/java/com/sleeplife/app/ui/viewmodels/NotesViewModel.kt`

**UiState**:
```kotlin
data class NotesUiState(
    val showFavoritesOnly: Boolean = false,
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val editingNote: Note? = null
)
```

**公共 API**:
```kotlin
val uiState: StateFlow<NotesUiState>
val allNotes: StateFlow<List<Note>>
fun getFilteredNotes(): StateFlow<List<Note>>

fun addNote(title: String, content: String, mood: NoteMood, tags: String)
fun updateNote(note: Note)
fun deleteNote(note: Note)
fun toggleFavorite(note: Note)
fun clearErrorMessage()
```

#### PomodoroViewModel

**文件**: `app/src/main/java/com/sleeplife/app/ui/viewmodels/PomodoroViewModel.kt`

**UiState**:
```kotlin
data class PomodoroUiState(
    val isRunning: Boolean = false,
    val currentSessionId: Long? = null,
    val currentTaskName: String = "",
    val sessionType: SessionType = SessionType.WORK,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val showStartDialog: Boolean = false,
    val sessionCompleted: Boolean = false,
    val todayFocusMinutes: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

**公共 API**:
```kotlin
val uiState: StateFlow<PomodoroUiState>
val todaySessions: StateFlow<List<PomodoroSession>>

fun startSession(taskName: String, duration: Int)
fun pauseSession()
fun resumeSession()
fun stopSession()
fun clearErrorMessage()
```

---

## 🧪 测试 API

### Repository 测试

**位置**: `app/src/test/java/com/sleeplife/app/data/repository/`

**测试文件**:
- `SleepRepositoryTest.kt` (8 测试方法)
- `HabitRepositoryTest.kt` (11 测试方法)
- `NoteRepositoryTest.kt` (12 测试方法)
- `PomodoroRepositoryTest.kt` (12 测试方法)

**测试覆盖**:
- ✅ 插入操作验证
- ✅ 更新操作验证
- ✅ 删除操作验证
- ✅ 查询操作（Flow）
- ✅ 验证失败场景
- ✅ 数据库异常处理

**示例**:
```kotlin
@Test
fun `insertSleepRecordWithValidation - valid record - returns success`() = runTest {
    val record = SleepRecord(...)
    val result = repository.insertSleepRecordWithValidation(record)
    
    assertThat(result).isInstanceOf(Result.Success::class.java)
}
```

---

## 📖 代码示例

### 完整的数据流示例

```kotlin
// 1. UI 层调用 ViewModel
@Composable
fun SleepScreen(viewModel: SleepViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Button(onClick = { viewModel.startSleepTracking() }) {
        Text("开始睡眠")
    }
    
    uiState.errorMessage?.let { error ->
        Snackbar { Text(error) }
    }
}

// 2. ViewModel 处理业务逻辑
@HiltViewModel
class SleepViewModel @Inject constructor(
    private val sleepRepository: SleepRepository
) : ViewModel() {
    fun startSleepTracking() {
        viewModelScope.launch {
            try {
                val record = SleepRecord(...)
                val result = sleepRepository.insertSleepRecordWithValidation(record)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isTracking = true) }
                        Timber.d("Sleep tracking started")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(errorMessage = result.exception.message) }
                        Timber.w("Failed: ${result.exception.message}")
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "未知错误: ${e.message}") }
                Timber.e(e, "Exception in startSleepTracking")
            }
        }
    }
}

// 3. Repository 执行数据操作
@Singleton
class SleepRepository @Inject constructor(
    private val sleepDao: SleepDao
) {
    suspend fun insertSleepRecordWithValidation(record: SleepRecord): Result<Long> {
        return try {
            // 验证
            val validationResult = SleepValidator.validateSleepRecord(record)
            if (validationResult is ValidationResult.Error) {
                Timber.w("Validation failed: ${validationResult.message}")
                return Result.Error(AppException.ValidationException(validationResult.message))
            }
            
            // 数据库操作
            val id = sleepDao.insertSleep(record)
            Timber.d("Sleep record inserted: ID=$id")
            Result.Success(id)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert sleep record")
            Result.Error(AppException.DatabaseException("保存失败: ${e.message}", e))
        }
    }
}

// 4. DAO 执行数据库操作
@Dao
interface SleepDao {
    @Insert
    suspend fun insertSleep(record: SleepRecord): Long
}
```

---

## 🔗 相关资源

### 官方文档

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

### 代码风格

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)

---

**文档维护**: 本文档与代码同步更新  
**生成工具**: KDoc + 手动维护  
**最后更新**: 2026-02-28
