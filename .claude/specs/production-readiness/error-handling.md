# Error Handling Specification

> **Version**: 1.0  
> **Status**: Active  
> **Last Updated**: 2026-02-28

## Overview

This specification defines the comprehensive error handling strategy for the SleepLife application, ensuring robust exception management, user-friendly error messages, and proper logging throughout all application layers.

## Goals

1. **Graceful Degradation**: Application never crashes from handled errors
2. **User-Friendly Messages**: Clear, actionable error messages for users
3. **Developer Visibility**: Comprehensive logging for debugging
4. **Type Safety**: Compile-time error handling verification using Result types

## Architecture

### Error Hierarchy

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()
    data class Loading : Result<Nothing>()
}

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class DatabaseException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class ValidationException(message: String) : AppException(message)
    class NetworkException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class UnknownException(message: String, cause: Throwable? = null) : AppException(message, cause)
}
```

## Repository Layer

### Requirements

1. **All public methods must return `Result<T>`**
2. **Catch all exceptions and wrap in appropriate AppException**
3. **Log errors using Timber before returning**
4. **Include contextual information in error messages**

### Implementation Pattern

```kotlin
suspend fun insertSleepRecord(record: SleepRecord): Result<Long> {
    return try {
        // Validation first
        when (val validation = validator.validateSleepRecord(record)) {
            is ValidationResult.Error -> {
                Timber.w("Sleep record validation failed: ${validation.message}")
                return Result.Error(AppException.ValidationException(validation.message))
            }
            is ValidationResult.Valid -> Unit
        }
        
        // Execute operation
        val id = sleepDao.insert(record)
        Timber.d("Inserted sleep record: $id")
        Result.Success(id)
        
    } catch (e: SQLiteException) {
        Timber.e(e, "Database error inserting sleep record")
        Result.Error(AppException.DatabaseException("无法保存睡眠记录", e))
    } catch (e: Exception) {
        Timber.e(e, "Unexpected error inserting sleep record")
        Result.Error(AppException.UnknownException("未知错误", e))
    }
}
```

### Test Requirements

- ✅ Test successful operations return `Result.Success`
- ✅ Test validation failures return `Result.Error` with `ValidationException`
- ✅ Test database exceptions return `Result.Error` with `DatabaseException`
- ✅ Verify Timber logs are called (using MockK)

## ViewModel Layer

### Requirements

1. **Consume `Result<T>` from repositories**
2. **Transform to UI-appropriate error messages**
3. **Maintain error state in StateFlow**
4. **Provide retry mechanisms where appropriate**

### Implementation Pattern

```kotlin
class SleepViewModel @Inject constructor(
    private val repository: SleepRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<SleepRecord>>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    
    fun loadSleepRecords() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            when (val result = repository.getAllSleepRecords().first()) {
                is Result.Success -> {
                    _uiState.value = if (result.data.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(result.data)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to load sleep records")
                    val message = when (result.exception) {
                        is AppException.DatabaseException -> "数据加载失败,请稍后重试"
                        is AppException.ValidationException -> result.exception.message ?: "数据验证失败"
                        else -> "未知错误,请稍后重试"
                    }
                    _uiState.value = UiState.Error(
                        message = message,
                        action = { loadSleepRecords() } // Retry action
                    )
                }
            }
        }
    }
    
    fun saveSleepRecord(record: SleepRecord) {
        viewModelScope.launch {
            when (val result = repository.insertSleepRecord(record)) {
                is Result.Success -> {
                    _errorMessage.value = null
                    loadSleepRecords()
                }
                is Result.Error -> {
                    val message = when (result.exception) {
                        is AppException.ValidationException -> result.exception.message ?: "验证失败"
                        is AppException.DatabaseException -> "保存失败,请重试"
                        else -> "操作失败,请重试"
                    }
                    _errorMessage.value = message
                    Timber.w("Save failed: ${result.exception.message}")
                }
            }
        }
    }
}
```

### Test Requirements

- ✅ Test successful repository response sets Success state
- ✅ Test error repository response sets Error state with correct message
- ✅ Test retry action in Error state
- ✅ Test error message flow emissions

## UI Layer

### Requirements

1. **Display error messages via Snackbar for transient errors**
2. **Display error views for load failures with retry button**
3. **Show inline validation errors for form fields**
4. **Never expose technical details to users**

### Implementation Pattern

```kotlin
@Composable
fun SleepScreen(viewModel: SleepViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show transient error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> LoadingView()
            is UiState.Success -> SleepRecordsList(state.data)
            is UiState.Error -> ErrorView(
                message = state.message,
                onRetry = state.action
            )
            is UiState.Empty -> EmptyView(stringResource(R.string.sleep_empty_message))
            is UiState.Idle -> Unit
        }
    }
}
```

### Error Message Guidelines

| Error Type | User Message (Chinese) | Action |
|-----------|----------------------|--------|
| Database Read Error | "数据加载失败,请稍后重试" | Retry button |
| Database Write Error | "保存失败,请重试" | Close |
| Validation Error | Specific field error | Inline display |
| Network Error (future) | "网络连接失败" | Retry button |
| Unknown Error | "操作失败,请稍后重试" | Close |

## Logging Strategy

### Log Levels

| Level | Usage | Example |
|-------|-------|---------|
| **VERBOSE** | Not used in production | - |
| **DEBUG** | Successful operations, flow tracking | "Inserted sleep record: 123" |
| **INFO** | Important milestones | "User logged in" |
| **WARN** | Validation failures, expected errors | "Invalid sleep duration" |
| **ERROR** | Exceptions, unexpected errors | Database exceptions, crashes |

### Logging Pattern

```kotlin
// DEBUG - Successful operations
Timber.d("Operation completed: $details")

// WARN - Expected errors
Timber.w("Validation failed: $reason")

// ERROR - Exceptions
Timber.e(exception, "Context about what failed")
```

## Acceptance Criteria

### Functional Requirements

- [ ] No unhandled exceptions crash the app
- [ ] All repository methods return Result<T>
- [ ] All ViewModels handle Error results
- [ ] All UI screens display errors appropriately
- [ ] Validation errors show inline
- [ ] Load errors show retry button
- [ ] Save/delete errors show snackbar

### Non-Functional Requirements

- [ ] Error messages are user-friendly (no technical jargon)
- [ ] All errors are logged with Timber
- [ ] Error logs include sufficient context for debugging
- [ ] Error handling adds < 50ms overhead to operations

### Test Coverage

- [ ] Repository error scenarios: 100%
- [ ] ViewModel error handling: 100%
- [ ] UI error display: Visual testing for each state

## Migration Notes

### Existing Code Impact

1. **Repositories**: All return types change from direct types to `Result<T>`
2. **ViewModels**: Must handle Result responses, may need new error state
3. **UI**: Must observe error state and display appropriately

### Migration Strategy

1. Create Result and AppException classes
2. Update one Repository at a time with tests
3. Update corresponding ViewModel with tests
4. Update corresponding UI screens
5. Verify end-to-end error flows
6. Repeat for remaining modules

## References

- [Kotlin Result Pattern](https://github.com/kittinunf/Result)
- [Android Error Handling Best Practices](https://developer.android.com/training/articles/user-data-ids)
- [Timber Logging Library](https://github.com/JakeWharton/timber)
