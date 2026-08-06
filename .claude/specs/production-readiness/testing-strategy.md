# Testing Strategy Specification

> **Version**: 1.0  
> **Status**: Active  
> **Last Updated**: 2026-02-28

## Overview

This specification defines the comprehensive testing strategy for the SleepLife application, covering unit tests, integration tests, and UI tests to achieve 80%+ code coverage and ensure production readiness.

## Testing Pyramid

```
          /\
         /  \  UI Tests (10%)
        /    \  - Critical user flows
       /------\  - Happy paths
      /        \ Integration Tests (20%)
     /          \ - Multi-layer interactions
    /            \ - End-to-end scenarios
   /--------------\ Unit Tests (70%)
   \              / - Repository logic
    \            /  - ViewModel logic
     \          /   - Validators
      \        /    - Data transformations
       \------/
```

## Coverage Goals

| Layer | Target Coverage | Priority |
|-------|----------------|----------|
| **Repository** | ≥ 85% | P0 |
| **ViewModel** | ≥ 80% | P0 |
| **Validators** | 100% | P0 |
| **UI Components** | ≥ 60% | P1 |
| **Overall** | ≥ 80% | P0 |

## Unit Testing

### Repository Tests

**File Location**: `app/src/test/java/com/sleeplife/app/data/repository/`

**Test Structure**:
```kotlin
class SleepRepositoryTest : RepositoryTestBase() {
    
    private lateinit var repository: SleepRepository
    private lateinit var validator: SleepValidator
    
    @Before
    override fun setUp() {
        super.setUp()
        validator = SleepValidator
        repository = SleepRepository(sleepDao, validator)
    }
    
    @Test
    fun `insertSleepRecord with valid data returns Success`() = runTest {
        // Arrange
        val record = TestDataFactory.createSleepRecord()
        
        // Act
        val result = repository.insertSleepRecord(record)
        
        // Assert
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val id = (result as Result.Success).data
        assertThat(id).isGreaterThan(0)
    }
    
    @Test
    fun `insertSleepRecord with invalid data returns Error`() = runTest {
        // Arrange
        val invalidRecord = TestDataFactory.InvalidData.sleepRecordEndBeforeStart
        
        // Act
        val result = repository.insertSleepRecord(invalidRecord)
        
        // Assert
        assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = (result as Result.Error).exception
        assertThat(error).isInstanceOf(AppException.ValidationException::class.java)
    }
}
```

**Test Scenarios**:

| Test Case | Description | Priority |
|-----------|-------------|----------|
| Happy path | All CRUD operations with valid data | P0 |
| Validation errors | Invalid data rejected with correct error | P0 |
| Database exceptions | Handle SQLite errors gracefully | P0 |
| Edge cases | Empty results, large datasets | P1 |
| Concurrent access | Multiple operations simultaneously | P2 |

**Per Repository**:
- SleepRepositoryTest: 15 test cases
- HabitRepositoryTest: 18 test cases (includes check-ins)
- NoteRepositoryTest: 15 test cases
- PomodoroRepositoryTest: 12 test cases

### ViewModel Tests

**File Location**: `app/src/test/java/com/sleeplife/app/ui/viewmodels/`

**Test Structure**:
```kotlin
@ExperimentalCoroutinesApi
class SleepViewModelTest : TestBase() {
    
    private lateinit var viewModel: SleepViewModel
    private lateinit var mockRepository: SleepRepository
    
    @Before
    override fun setUp() {
        super.setUp()
        mockRepository = mockk()
        viewModel = SleepViewModel(mockRepository)
    }
    
    @Test
    fun `loadSleepRecords success sets Success state`() = runTest {
        // Arrange
        val records = TestDataFactory.createSleepRecords(5)
        coEvery { mockRepository.getAllSleepRecords() } returns flowOf(Result.Success(records))
        
        // Act
        viewModel.loadSleepRecords()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(UiState.Success::class.java)
            assertThat((state as UiState.Success).data).hasSize(5)
        }
    }
    
    @Test
    fun `loadSleepRecords error sets Error state with retry`() = runTest {
        // Arrange
        val error = AppException.DatabaseException("DB Error")
        coEvery { mockRepository.getAllSleepRecords() } returns flowOf(Result.Error(error))
        
        // Act
        viewModel.loadSleepRecords()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(UiState.Error::class.java)
            assertThat((state as UiState.Error).message).isNotEmpty()
            assertThat(state.action).isNotNull()
        }
    }
}
```

**Test Scenarios**:

| Test Case | Description | Priority |
|-----------|-------------|----------|
| State transitions | Idle → Loading → Success | P0 |
| Error handling | Repository errors → UI Error state | P0 |
| Empty state | Empty results → Empty state | P0 |
| Retry mechanism | Error state action retries operation | P0 |
| Error messages | Transient errors emit to errorMessage flow | P0 |
| Form validation | Save with invalid data shows validation error | P0 |

**Per ViewModel**:
- SleepViewModelTest: 12 test cases
- HabitsViewModelTest: 15 test cases
- NotesViewModelTest: 12 test cases
- PomodoroViewModelTest: 10 test cases

### Validator Tests

**File Location**: `app/src/test/java/com/sleeplife/app/data/validation/`

**Test Structure**:
```kotlin
class SleepValidatorTest {
    
    @Test
    fun `valid sleep record passes validation`() {
        val record = TestDataFactory.createSleepRecord()
        val result = SleepValidator.validateSleepRecord(record)
        assertThat(result).isEqualTo(ValidationResult.Valid)
    }
    
    @Test
    fun `sleep record with end before start fails validation`() {
        val record = TestDataFactory.InvalidData.sleepRecordEndBeforeStart
        val result = SleepValidator.validateSleepRecord(record)
        assertThat(result).isInstanceOf(ValidationResult.Error::class.java)
        assertThat((result as ValidationResult.Error).message).contains("结束时间必须晚于开始时间")
    }
}
```

**Test Coverage**: 100% of all validation rules

**Per Validator**:
- SleepValidatorTest: 8 test cases
- HabitValidatorTest: 10 test cases
- NoteValidatorTest: 10 test cases
- PomodoroValidatorTest: 8 test cases

## Integration Testing

### End-to-End Flow Tests

**File Location**: `app/src/androidTest/java/com/sleeplife/app/integration/`

**Test Structure**:
```kotlin
@HiltAndroidTest
class SleepTrackingFlowTest : ComposeTestBase() {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Before
    fun setup() {
        hiltRule.inject()
        composeTestRule.setContent {
            SleepLifeTheme {
                SleepScreen()
            }
        }
    }
    
    @Test
    fun completeSleeTrackingFlow() {
        // Start sleep
        composeTestRule.onNodeWithText("开始睡眠").performClick()
        
        // Verify running state
        composeTestRule.onNodeWithText("正在睡眠...").assertExists()
        
        // Stop sleep
        composeTestRule.onNodeWithText("结束睡眠").performClick()
        
        // Rate quality
        composeTestRule.onNodeWithText("好").performClick()
        
        // Add notes
        composeTestRule.onNodeWithTag("sleep_notes_input")
            .performTextInput("Test sleep notes")
        
        // Save
        composeTestRule.onNodeWithText("保存").performClick()
        
        // Verify saved
        composeTestRule.onNodeWithText("Test sleep notes").assertExists()
    }
}
```

**Test Flows**:

| Flow | Description | Priority |
|------|-------------|----------|
| Sleep tracking | Complete sleep record creation | P0 |
| Habit management | Create habit, check-in, view progress | P0 |
| Note CRUD | Create, edit, delete, favorite note | P0 |
| Pomodoro session | Start, complete, view statistics | P0 |
| Error recovery | Handle errors and retry | P1 |

## UI Testing

### Component Tests

**File Location**: `app/src/androidTest/java/com/sleeplife/app/ui/screens/`

**Test Structure**:
```kotlin
class SleepScreenTest : ComposeTestBase() {
    
    @Test
    fun loadingStateDisplaysProgressIndicator() {
        composeTestRule.setContent {
            SleepLifeTheme {
                // Mock Loading state
            }
        }
        
        composeTestRule.onNodeWithTag("loading_indicator").assertExists()
    }
    
    @Test
    fun errorStateDisplaysErrorMessage() {
        composeTestRule.setContent {
            SleepLifeTheme {
                ErrorView(
                    message = "加载失败",
                    onRetry = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("加载失败").assertExists()
        composeTestRule.onNodeWithText("重试").assertExists()
    }
}
```

**Test Scenarios**:

| Test Case | Description | Priority |
|-----------|-------------|----------|
| Loading state | Shows progress indicator | P0 |
| Success state | Displays data correctly | P0 |
| Error state | Shows error message and retry | P0 |
| Empty state | Shows empty message | P0 |
| User interactions | Buttons, inputs work correctly | P0 |
| Validation errors | Inline errors display | P0 |

## Test Tools & Libraries

| Tool | Version | Purpose |
|------|---------|---------|
| JUnit | 4.13.2 | Test framework |
| MockK | 1.13.8 | Kotlin mocking |
| Turbine | 1.0.0 | Flow testing |
| Coroutines Test | 1.7.3 | Coroutine testing |
| Room Testing | 2.6.1 | In-memory database |
| Truth | 1.1.5 | Assertions |
| Compose UI Test | - | Compose testing |
| Hilt Testing | 2.48 | DI testing |

## Test Execution

### Local Development

```bash
# Run all unit tests
./gradlew test

# Run tests with coverage
./gradlew testDebugUnitTestCoverage

# Run specific test class
./gradlew test --tests SleepRepositoryTest

# Run UI tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### Coverage Reports

```bash
# Generate coverage report
./gradlew testDebugUnitTestCoverage

# View report
open app/build/reports/coverage/test/debug/index.html
```

### CI/CD Integration

```yaml
# GitHub Actions example
- name: Run Unit Tests
  run: ./gradlew test
  
- name: Generate Coverage
  run: ./gradlew testDebugUnitTestCoverage
  
- name: Upload Coverage
  uses: codecov/codecov-action@v3
  with:
    files: app/build/reports/coverage/test/debug/report.xml
```

## Test Data Management

### TestDataFactory

- Centralized test data creation
- Consistent test data across tests
- Valid and invalid data variants
- Easy to maintain and extend

### Database State

- Each test starts with clean database (in-memory)
- No shared state between tests
- Tests are independent and rerunnable

## Acceptance Criteria

### Unit Tests
- [ ] All Repositories have ≥ 85% coverage
- [ ] All ViewModels have ≥ 80% coverage
- [ ] All Validators have 100% coverage
- [ ] All unit tests pass

### Integration Tests
- [ ] 4 end-to-end flow tests implemented
- [ ] All integration tests pass

### UI Tests
- [ ] 4 screen tests implemented
- [ ] Critical user paths covered
- [ ] All UI tests pass

### Overall
- [ ] Total coverage ≥ 80%
- [ ] No flaky tests
- [ ] Test execution < 5 minutes
- [ ] Coverage report generated

## References

- [Android Testing Codelab](https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics)
- [Testing Kotlin Flows](https://developer.android.com/kotlin/flow/test)
- [Compose UI Testing](https://developer.android.com/jetpack/compose/testing)
- [MockK Documentation](https://mockk.io/)
