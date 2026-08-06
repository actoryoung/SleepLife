# SleepLife 架构设计文档

**版本**: 1.0.0-alpha  
**更新日期**: 2026-02-28  
**作者**: Claude Code

---

## 目录

1. [架构概述](#架构概述)
2. [技术选型](#技术选型)
3. [分层架构](#分层架构)
4. [错误处理架构](#错误处理架构)
5. [数据流设计](#数据流设计)
6. [核心模块详解](#核心模块详解)
7. [设计模式](#设计模式)
8. [测试策略](#测试策略)
9. [性能优化](#性能优化)
10. [未来规划](#未来规划)

---

## 架构概述

SleepLife 采用 **MVVM (Model-View-ViewModel)** 架构模式 + **Repository Pattern**，结合 Clean Architecture 原则，实现关注点分离、可测试性和可维护性。

### 核心设计原则

1. **单一职责原则 (SRP)**：每个类只负责一个功能
2. **依赖倒置原则 (DIP)**：高层模块不依赖低层模块，都依赖抽象
3. **接口隔离原则 (ISP)**：使用接口分离关注点
4. **关注点分离 (SoC)**：UI、业务逻辑、数据访问严格分层
5. **可测试性优先**：所有层次支持单元测试和集成测试

### 架构图

```
┌─────────────────────────────────────────────────┐
│                 Presentation Layer              │
│  ┌───────────────────────────────────────────┐  │
│  │   Jetpack Compose Screens (UI)           │  │
│  │   - SleepScreen, HabitsScreen, etc.      │  │
│  └───────────────────────────────────────────┘  │
│                      ▲                          │
│                      │ UiState (StateFlow)      │
│                      ▼                          │
│  ┌───────────────────────────────────────────┐  │
│  │   ViewModels (MVVM)                       │  │
│  │   - SleepViewModel, HabitsViewModel       │  │
│  │   - Error Handling + Result Pattern       │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
                      ▲
                      │ Repository Interface
                      ▼
┌─────────────────────────────────────────────────┐
│                  Domain Layer                   │
│  ┌───────────────────────────────────────────┐  │
│  │   Repositories (Repository Pattern)       │  │
│  │   - SleepRepository, HabitRepository      │  │
│  │   - WithValidation methods (Result<T>)    │  │
│  └───────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────┐  │
│  │   Validators (Business Rules)             │  │
│  │   - SleepValidator, HabitValidator        │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
                      ▲
                      │ DAO Interface
                      ▼
┌─────────────────────────────────────────────────┐
│                   Data Layer                    │
│  ┌───────────────────────────────────────────┐  │
│  │   Room DAOs (Data Access Objects)         │  │
│  │   - SleepDao, HabitDao, etc.              │  │
│  └───────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────┐  │
│  │   Entities (Database Models)              │  │
│  │   - SleepRecord, Habit, Note, etc.        │  │
│  └───────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────┐  │
│  │   Room Database (SQLite)                  │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

---

## 技术选型

### 核心技术栈

| 技术 | 版本 | 用途 | 选型理由 |
|------|------|------|----------|
| **Kotlin** | 1.9.20 | 开发语言 | 简洁、安全、协程支持 |
| **Jetpack Compose** | 1.5.4 | UI 框架 | 声明式 UI、现代化、性能优秀 |
| **Material3** | 1.1.2 | UI 设计系统 | Google 最新设计语言 |
| **Hilt** | 2.48 | 依赖注入 | Android 官方推荐、简化 DI |
| **Room** | 2.6.0 | 本地数据库 | 类型安全、编译时校验、Flow 支持 |
| **Coroutines** | 1.7.3 | 异步处理 | 结构化并发、简洁的异步代码 |
| **Flow** | 1.7.3 | 响应式数据流 | 与 Compose 完美集成 |
| **kotlinx.datetime** | 0.4.1 | 日期时间 | 跨平台、类型安全 |
| **Timber** | 5.0.1 | 日志框架 | 简洁、可扩展 |
| **Navigation Compose** | 2.7.5 | 导航管理 | Compose 原生导航方案 |

### 测试技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **JUnit** | 4.13.2 | 单元测试框架 |
| **MockK** | 1.13.8 | Kotlin Mock 框架 |
| **Truth** | 1.1.5 | 断言库 |
| **Turbine** | 1.0.0 | Flow 测试 |
| **Hilt Testing** | 2.48 | 依赖注入测试 |
| **Compose UI Test** | 1.5.4 | UI 测试 |

---

## 分层架构

### 1. Presentation Layer（表现层）

**职责**：显示 UI、处理用户交互、展示数据

**组件**：
- **Screens (Composable)**：各功能页面（SleepScreen, HabitsScreen, etc.）
- **ViewModels**：管理 UI 状态、处理业务逻辑调用
- **Navigation**：页面导航配置
- **Theme**：Material3 主题定制
- **Components**：可复用 UI 组件

**关键设计**：
- **单向数据流 (UDF)**：UI 通过 ViewModel 的 `StateFlow<UiState>` 接收状态
- **事件处理**：UI 调用 ViewModel 方法触发操作
- **错误显示**：通过 `UiState.errorMessage` 显示错误信息

**示例**: SleepViewModel

```kotlin
@HiltViewModel
class SleepViewModel @Inject constructor(
    private val sleepRepository: SleepRepository
) : ViewModel() {
    
    // UI 状态（单一真相来源）
    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()
    
    // 用户操作
    fun startSleepTracking() {
        viewModelScope.launch {
            try {
                val result = sleepRepository.insertSleepRecordWithValidation(...)
                when (result) {
                    is Result.Success -> { /* 更新状态 */ }
                    is Result.Error -> { /* 显示错误 */ }
                }
            } catch (e: Exception) {
                // 异常处理
            }
        }
    }
}
```

### 2. Domain Layer（领域层）

**职责**：业务规则、Repository 接口、数据验证

**组件**：
- **Repositories**：数据访问抽象
- **Validators**：业务规则验证
- **Result<T>**：统一错误处理
- **AppException**：异常层次结构

**关键设计**：
- **Repository Pattern**：隔离数据源实现细节
- **Result 模式**：类型安全的成功/失败处理
- **验证器解耦**：业务规则独立于数据访问

**示例**: SleepRepository

```kotlin
@Singleton
class SleepRepository @Inject constructor(
    private val sleepDao: SleepDao
) {
    // 原始方法（用于 Flow 查询）
    fun getTodaySleepRecord(): Flow<SleepRecord?> = 
        sleepDao.getTodaySleep()
    
    // 带验证的方法（用于数据修改）
    suspend fun insertSleepRecordWithValidation(
        record: SleepRecord
    ): Result<Long> {
        return try {
            // 1. 验证
            val validationResult = SleepValidator.validateSleepRecord(record)
            if (validationResult is ValidationResult.Error) {
                Timber.w("Validation failed: ${validationResult.message}")
                return Result.Error(
                    AppException.ValidationException(validationResult.message)
                )
            }
            
            // 2. 数据库操作
            val id = sleepDao.insertSleep(record)
            Timber.d("Sleep record inserted: ID=$id")
            Result.Success(id)
            
        } catch (e: Exception) {
            // 3. 异常处理
            Timber.e(e, "Failed to insert sleep record")
            Result.Error(
                AppException.DatabaseException("插入失败: ${e.message}", e)
            )
        }
    }
}
```

### 3. Data Layer（数据层）

**职责**：数据持久化、数据源访问

**组件**：
- **Room Database**：SQLite 数据库封装
- **DAOs (Data Access Objects)**：数据库操作接口
- **Entities**：数据库表模型

**关键设计**：
- **Flow 响应式查询**：数据库变化自动通知 UI
- **Suspend 函数**：所有修改操作使用协程
- **关系映射**：@Relation 处理表关联（如 Habit + CheckIns）

**示例**: SleepDao

```kotlin
@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_records WHERE date(startTime) = date('now', 'localtime')")
    fun getTodaySleep(): Flow<SleepRecord?>
    
    @Insert
    suspend fun insertSleep(record: SleepRecord): Long
    
    @Update
    suspend fun updateSleep(record: SleepRecord)
    
    @Delete
    suspend fun deleteSleep(record: SleepRecord)
}
```

---

## 错误处理架构

### 核心组件

#### 1. Result<T> 封装

**定义**:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()
    data class Loading<T>(val data: T? = null) : Result<T>()
}
```

**用途**:
- 统一表示操作的三种状态：成功、失败、加载中
- 类型安全：编译时保证处理所有情况
- 可组合：提供 `map()`, `flatMap()`, `getOrNull()` 等工具方法

**使用场景**:
- Repository 层的 `*WithValidation` 方法返回值
- ViewModel 中匹配 Result 状态更新 UI

#### 2. AppException 层次结构

```kotlin
sealed class AppException(message: String, cause: Throwable? = null) : 
    Exception(message, cause) {
    
    // 业务验证失败（用户输入错误）
    class ValidationException(message: String) : AppException(message)
    
    // 数据库操作失败（系统错误）
    class DatabaseException(message: String, cause: Throwable? = null) : 
        AppException(message, cause)
    
    // 未预期的异常
    class UnknownException(message: String, cause: Throwable? = null) : 
        AppException(message, cause)
}
```

**分类原则**:
- **ValidationException**: 用户可纠正的错误（如输入格式错误）
- **DatabaseException**: 系统错误（如磁盘空间不足）
- **UnknownException**: 未预料的错误（如 NPE）

#### 3. 业务验证器

每个核心功能模块都有独立的验证器：

**SleepValidator**:
```kotlin
object SleepValidator {
    fun validateSleepRecord(record: SleepRecord): ValidationResult {
        // 睡眠时长验证（0-24小时）
        if (record.duration < 0 || record.duration > 24 * 60) {
            return ValidationResult.Error("睡眠时长必须在 0-24 小时之间")
        }
        
        // 睡眠质量验证（1-5分）
        if (record.quality !in 1..5) {
            return ValidationResult.Error("睡眠质量评分必须在 1-5 之间")
        }
        
        return ValidationResult.Success
    }
}
```

**验证规则汇总**:

| 模块 | 验证规则 |
|------|----------|
| **Sleep** | 时长 0-24h、质量 1-5 分 |
| **Habit** | 名称非空、目标天数 >0 |
| **Note** | 标题非空、内容 <10000 字符 |
| **Pomodoro** | 任务名非空、时长 1-120 分钟 |

### 错误处理流程

```
用户操作 (UI)
    ↓
ViewModel.someAction()
    ↓
try {
    Repository.insertWithValidation(data)
        ↓
    Validator.validate(data) → ValidationResult.Error?
        ↓ No (Valid)
    DAO.insert(data) → Success
        ↓
    Result.Success(id)
        ↓
    ViewModel: 更新 UiState (成功)
        ↓
    UI: 显示成功反馈
    
} catch (Exception) {
    ↓
    Result.Error(DatabaseException)
        ↓
    ViewModel: 更新 UiState.errorMessage
        ↓
    UI: Snackbar 显示错误
}
```

### 日志策略

使用 Timber 进行分级日志：

```kotlin
// DEBUG: 正常操作（开发环境）
Timber.d("Sleep record inserted: ID=$id")

// WARN: 业务验证失败（用户错误）
Timber.w("Validation failed: ${validationResult.message}")

// ERROR: 异常错误（系统错误）
Timber.e(e, "Failed to insert sleep record")
```

**生产环境配置**:
- DEBUG 日志关闭
- WARN/ERROR 日志上报到 Crashlytics（未来实现）

---

## 数据流设计

### 单向数据流 (UDF)

```
┌──────────────────────────────────────────────┐
│              UI (Composable)                 │
│  - 订阅 uiState: StateFlow<UiState>          │
│  - 调用 viewModel.action()                   │
└──────────────────────────────────────────────┘
                ▲              │
                │              │ Event
         State  │              ▼
                │    ┌────────────────────────┐
                │    │   ViewModel            │
                │───│   - _uiState.update()  │
                     │   - viewModelScope     │
                     └────────────────────────┘
                                │
                                │ Repository call
                                ▼
                     ┌────────────────────────┐
                     │   Repository           │
                     │   - validate()         │
                     │   - DAO operations     │
                     │   - Result<T>          │
                     └────────────────────────┘
                                │
                                │ Database query
                                ▼
                     ┌────────────────────────┐
                     │   Room Database        │
                     │   - Flow<Entity>       │
                     └────────────────────────┘
```

### 响应式数据流

**查询路径** (数据库 → UI 自动更新):
```
Room Database (Flow<Entity>)
    ↓
Repository (Flow<Entity>)
    ↓
ViewModel (StateFlow<List<Entity>>)
    ↓
Composable (collectAsState())
    ↓
UI 自动重组
```

**示例**: 睡眠记录列表实时更新

```kotlin
// Repository
fun getRecentSleepRecords(): Flow<List<SleepRecord>> = 
    sleepDao.getRecentSleep(7)

// ViewModel
val recentRecords = sleepRepository.getRecentSleepRecords()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

// UI
@Composable
fun SleepHistoryList(viewModel: SleepViewModel) {
    val records by viewModel.recentRecords.collectAsState()
    
    LazyColumn {
        items(records) { record ->
            SleepRecordCard(record)
        }
    }
}
```

**优势**:
- 数据库变化 → UI 自动更新（无需手动刷新）
- 内存高效：`WhileSubscribed(5000)` 策略在无订阅者时停止 Flow
- 线程安全：Room 保证主线程安全查询

---

## 核心模块详解

### 1. 睡眠追踪模块

**功能**:
- 开始/停止睡眠记录
- 记录睡眠质量（1-5分）
- 查看最近7天睡眠历史
- 今日睡眠状态显示

**数据模型**:
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

**业务规则**:
- 同一天只能有一条睡眠记录
- 睡眠时长：0-24 小时
- 质量评分：1-5 分

### 2. 习惯打卡模块

**功能**:
- 创建自定义习惯（名称 + 目标天数）
- 每日打卡记录
- 进度可视化（已打卡/目标天数）
- 撤销今日打卡

**数据模型**:
```kotlin
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetDays: Int,
    val createdAt: LocalDateTime
)

@Entity(
    tableName = "habit_checkins",
    foreignKeys = [ForeignKey(...)]
)
data class HabitCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val note: String = ""
)
```

**业务规则**:
- 习惯名称：非空
- 目标天数：>0
- 每天每个习惯只能打卡一次

**关系查询**:
```kotlin
data class HabitWithCheckIns(
    @Embedded val habit: Habit,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val checkIns: List<HabitCheckIn>
)
```

### 3. 笔记日记模块

**功能**:
- 快速记录想法和日记
- 心情标记（HAPPY/SAD/NEUTRAL/EXCITED/ANGRY）
- 标签分类
- 收藏功能
- 搜索（标题/内容/标签）

**数据模型**:
```kotlin
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val mood: NoteMood = NoteMood.NEUTRAL,
    val tags: String = "",         // 逗号分隔
    val isFavorite: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class NoteMood { HAPPY, SAD, NEUTRAL, EXCITED, ANGRY }
```

**业务规则**:
- 标题：非空
- 内容：<10000 字符

### 4. 番茄钟专注模块

**功能**:
- 自定义专注时长（1-120分钟）
- 任务名称记录
- 倒计时显示
- 暂停/继续/停止
- 今日专注统计
- 完成历史查看

**数据模型**:
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

**业务规则**:
- 任务名：非空
- 时长：1-120 分钟

---

## 设计模式

### 1. Repository Pattern

**目的**: 抽象数据源，提供统一的数据访问接口

**实现**:
```kotlin
// Repository 接口定义（隐式，通过类实现）
class SleepRepository @Inject constructor(
    private val sleepDao: SleepDao
) {
    // 查询方法：返回 Flow（响应式）
    fun getTodaySleepRecord(): Flow<SleepRecord?> = 
        sleepDao.getTodaySleep()
    
    // 修改方法：返回 Result<T>（错误处理）
    suspend fun insertSleepRecordWithValidation(
        record: SleepRecord
    ): Result<Long> { ... }
}
```

**优势**:
- ViewModel 不依赖具体数据源（可替换为网络 API、本地缓存等）
- 集中管理数据访问逻辑
- 便于测试（可 Mock Repository）

### 2. MVVM (Model-View-ViewModel)

**职责分离**:
- **View** (Composable): 纯 UI，无业务逻辑
- **ViewModel**: 管理 UI 状态、处理用户事件、调用 Repository
- **Model** (Repository + Entity): 数据访问和持久化

### 3. Dependency Injection (Hilt)

**配置**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SleepLifeDatabase {
        return Room.databaseBuilder(...)
            .build()
    }
    
    @Provides
    fun provideSleepDao(database: SleepLifeDatabase) = database.sleepDao()
}
```

**优势**:
- 自动管理依赖生命周期
- 便于测试（可替换实现）
- 减少样板代码

### 4. State Management (StateFlow)

**单一真相来源**:
```kotlin
// ViewModel
private val _uiState = MutableStateFlow(SleepUiState())
val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

// UI
val state by viewModel.uiState.collectAsState()
```

**UiState 设计**:
```kotlin
data class SleepUiState(
    val isTracking: Boolean = false,
    val todayRecord: SleepRecord? = null,
    val recentRecords: List<SleepRecord> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

---

## 测试策略

### 测试金字塔

```
        ┌────────────┐
        │  UI Tests  │  (10% - 端到端测试)
        └────────────┘
       ┌──────────────┐
       │ Integration  │  (20% - 集成测试)
       │    Tests     │
       └──────────────┘
    ┌────────────────────┐
    │   Unit Tests       │  (70% - 单元测试)
    └────────────────────┘
```

### 单元测试

**Repository 测试** (已创建，待执行):
```kotlin
@ExperimentalCoroutinesTest
class SleepRepositoryTest {
    @Test
    fun `insertSleepRecordWithValidation - valid record - returns success`() = runTest {
        // Given
        val record = SleepRecord(...)
        
        // When
        val result = repository.insertSleepRecordWithValidation(record)
        
        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
    }
    
    @Test
    fun `insertSleepRecordWithValidation - invalid duration - returns error`() = runTest {
        val record = SleepRecord(duration = -10)  // Invalid
        
        val result = repository.insertSleepRecordWithValidation(record)
        
        assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = (result as Result.Error).exception
        assertThat(error).isInstanceOf(AppException.ValidationException::class.java)
    }
}
```

**ViewModel 测试** (计划中):
```kotlin
@Test
fun `startSleepTracking - success - updates uiState correctly`() = runTest {
    // Given
    val repository = mockk<SleepRepository>()
    coEvery { repository.insertSleepRecordWithValidation(any()) } returns 
        Result.Success(1L)
    val viewModel = SleepViewModel(repository)
    
    // When
    viewModel.startSleepTracking()
    testScheduler.advanceUntilIdle()
    
    // Then
    assertThat(viewModel.uiState.value.isTracking).isTrue()
    assertThat(viewModel.uiState.value.errorMessage).isNull()
}
```

### 集成测试

**测试范围**:
- Repository + DAO + Database
- ViewModel + Repository

### UI 测试

**Compose UI Test**:
```kotlin
@Test
fun sleepScreen_startTracking_showsTrackingUI() {
    composeTestRule.setContent {
        SleepScreen(viewModel)
    }
    
    // 点击开始按钮
    composeTestRule.onNodeWithText("开始睡眠").performClick()
    
    // 验证 UI 更新
    composeTestRule.onNodeWithText("正在记录...").assertIsDisplayed()
}
```

### 测试覆盖率目标

| 层次 | 目标覆盖率 | 当前状态 |
|------|-----------|---------|
| Repository | 80%+ | ⏳ 测试已写，待执行 |
| ViewModel | 70%+ | ⏳ 计划中 |
| Validator | 90%+ | ⏳ 计划中 |
| UI | 30%+ | ⏳ 计划中 |
| **Overall** | **70%+** | **⏳ 目标** |

---

## 性能优化

### 数据库优化

1. **索引添加**:
```kotlin
@Entity(
    tableName = "sleep_records",
    indices = [Index(value = ["startTime"])]  // 按日期查询优化
)
```

2. **分页加载** (未来实现):
```kotlin
@Query("SELECT * FROM notes ORDER BY createdAt DESC")
fun getAllNotesPaged(): PagingSource<Int, Note>
```

### Flow 优化

**冷启动策略**:
```kotlin
val recentRecords = repository.getRecentSleepRecords()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),  // 5秒无订阅者后停止
        initialValue = emptyList()
    )
```

### Compose 优化

**稳定性标记**:
```kotlin
@Immutable
data class SleepUiState(...)

@Stable
interface SleepRepository { ... }
```

---

## 未来规划

### 短期目标（1个月）

- [ ] 完成所有单元测试和集成测试
- [ ] UI 错误提示实现（Snackbar）
- [ ] 深色模式完善
- [ ] 数据导出/导入功能

### 中期目标（3个月）

- [ ] 睡眠统计图表（MPAndroidChart / Vico）
- [ ] 习惯报告生成
- [ ] 通知提醒系统
- [ ] 小部件支持

### 长期目标（6个月+）

- [ ] 云端同步（Firebase / 自建后端）
- [ ] 多设备支持
- [ ] AI 睡眠建议
- [ ] 社区分享功能

---

## 参考资料

### 官方文档

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

### 代码风格

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Material Design 3](https://m3.material.io/)

---

**文档维护**: 本文档随架构演进持续更新  
**问题反馈**: 通过 GitHub Issues 提交  
**最后更新**: 2026-02-28
