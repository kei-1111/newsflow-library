# newsflow-library Kotlin Multiplatform Style Guide

# Introduction
This style guide outlines the coding conventions for Kotlin Multiplatform (KMP) code in the newsflow-library project.
It follows Clean Architecture principles with strict module separation and MVI pattern to ensure consistency across the codebase.

**All review comments must be written in Japanese.**

# Key Principles
* **Clean Architecture:** Strict layer separation with unidirectional dependency flow.
* **MVI Pattern:** Model-View-Intent pattern for ViewModels with clear state management.
* **Testability:** All business logic must be testable with Fake implementations.
* **Multiplatform:** Code should work seamlessly on Android and iOS.

# Architecture

## Module Structure
* **Layered Modular Architecture:** Clear dependency flow between layers.
* **Dependency Flow:** `feature/*` → `core:domain` → `core:data` → `core:network` / `core:model`
* **No Cross-Feature Dependencies:** Feature modules must never depend on each other.

## Layer Responsibilities
* **core:model:** Domain entities only. No dependencies on other project modules.
* **core:network:** HTTP client and API services. Platform-specific engines (OkHttp/Darwin).
* **core:data:** Repository implementations with caching. Depends on model and network.
* **core:domain:** Use cases (business logic). Depends on data and model.
* **feature/*:** Presentation layer with MVI ViewModels. Depends on domain, model, mvi, logger.
* **shared:** Koin DI aggregation and iOS XCFramework export.

## Convention Plugins
* **Core Module:** Use `newsflow.library.kmp.library` plugin.
* **Feature Module:** Use `newsflow.library.kmp.feature` plugin.
* **Static Analysis:** Use `newsflow.library.detekt` plugin.

# MVI Pattern

## File Structure
Each feature module should have the following files:
```
feature/xxx/src/commonMain/kotlin/.../
├── XxxViewModel.kt       # ViewModel implementation
├── XxxState.kt           # Public UI state (sealed interface)
├── XxxIntent.kt          # User intents
├── XxxEffect.kt          # One-time side effects
├── XxxViewModelState.kt  # Internal mutable state (data class)
└── XxxModule.kt          # Koin module definition
```

## ViewModel Implementation
* **Inherit from StatefulBaseViewModel:** Use `StatefulBaseViewModel<ViewModelState, State, Intent, Effect>`.
* **Override Factory Methods:** Override `createInitialViewModelState()` and `createInitialState()`.
* **Internal State Management:** Use `updateViewModelState { copy(...) }` for state updates.
* **One-time Events:** Use `sendEffect()` for navigation and toast events.
* **Minimum Loading Time:** Use `ensureMinimumLoadingTime()` for consistent UX.

```kotlin
class XxxViewModel(
    private val useCase: XxxUseCase,
) : StatefulBaseViewModel<XxxViewModelState, XxxState, XxxIntent, XxxEffect>() {

    override fun createInitialViewModelState(): XxxViewModelState = XxxViewModelState()
    override fun createInitialState(): XxxState = XxxState.Stable()

    override fun onIntent(intent: XxxIntent) {
        when (intent) {
            is XxxIntent.Load -> loadData()
        }
    }
}
```

## ViewModelState
* **Implement ViewModelState Interface:** Must implement `ViewModelState<State>`.
* **Pure toState() Function:** The `toState()` method must be a pure function with no side effects.
* **Derive State:** State should be derived from ViewModelState via `toState()`.

```kotlin
data class XxxViewModelState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: NewsflowError? = null,
) : ViewModelState<XxxState> {
    override fun toState(): XxxState = when {
        error != null -> XxxState.Error(error)
        else -> XxxState.Stable(isLoading, data)
    }
}
```

## State
* **Sealed Interface:** Use sealed interface for State.
* **Immutable:** State must be immutable.
* **Stable State:** Include loading flag in Stable state for partial loading.

```kotlin
sealed interface XxxState : State {
    data class Stable(
        val isLoading: Boolean,
        val data: List<Item>,
    ) : XxxState

    data class Error(
        val error: NewsflowError,
    ) : XxxState
}
```

# Use Cases

## Implementation Pattern
* **Single Public Function:** Use `operator fun invoke()` as the only public function.
* **Interface + Internal Implementation:** Public interface, internal implementation class.
* **Return Result Type:** Always return `Result<T>` to wrap errors.
* **Thin Wrapper:** Use cases should be thin wrappers delegating to repositories.

```kotlin
// Public interface
interface FetchArticlesUseCase {
    suspend operator fun invoke(
        category: String,
        forceRefresh: Boolean = false,
    ): Result<List<Article>>
}

// Internal implementation
internal class FetchArticlesUseCaseImpl(
    private val repository: NewsRepository,
) : FetchArticlesUseCase {
    override suspend fun invoke(
        category: String,
        forceRefresh: Boolean,
    ): Result<List<Article>> =
        repository.fetchArticles(category = category, forceRefresh = forceRefresh)
}
```

# Repositories

## Implementation Pattern
* **Interface + Internal Implementation:** Public interface, internal implementation class.
* **Thread-Safe Caching:** Use `Mutex` for thread-safe in-memory caching.
* **Return Result Type:** Always return `Result<T>` to wrap errors.

```kotlin
// Public interface
interface NewsRepository {
    suspend fun fetchArticles(
        category: String,
        forceRefresh: Boolean = false,
    ): Result<List<Article>>
}

// Internal implementation with Mutex for thread safety
internal class NewsRepositoryImpl(
    private val apiService: NewsApiService,
) : NewsRepository {
    private val mutex = Mutex()
    private val cache = mutableMapOf<String, List<Article>>()

    override suspend fun fetchArticles(
        category: String,
        forceRefresh: Boolean,
    ): Result<List<Article>> = mutex.withLock {
        // Implementation with caching
    }
}
```

# Error Handling

## NewsflowError
* **Nested Sealed Classes:** Use `NewsflowError` with nested `NetworkError` and `InternalError` categories.
* **Wrap in Result:** Return errors wrapped in `Result.failure()`.
* **No Exceptions:** Do not throw exceptions from data/domain layers.

```kotlin
sealed class NewsflowError(message: String) : Exception(message) {
    sealed class NetworkError(message: String) : NewsflowError(message) {
        data class Unauthorized(override val message: String = "Invalid API key") : NetworkError(message)
        data class RateLimitExceeded(override val message: String = "Rate limit exceeded") : NetworkError(message)
        data class BadRequest(override val message: String) : NetworkError(message)
        data class ServerError(override val message: String) : NetworkError(message)
        data class NetworkFailure(override val message: String) : NetworkError(message)
    }

    sealed class InternalError(message: String) : NewsflowError(message) {
        data class ArticleNotFound(override val message: String = "Article not found") : InternalError(message)
        data class InvalidParameter(override val message: String) : InternalError(message)
    }
}
```

## Result Usage
```kotlin
// Repository/UseCase returns Result
suspend fun fetchArticles(): Result<List<Article>>

// ViewModel consumes Result
useCase().fold(
    onSuccess = { articles ->
        updateViewModelState { copy(data = articles, isLoading = false) }
    },
    onFailure = { error ->
        updateViewModelState { copy(error = error as? NewsflowError, isLoading = false) }
    }
)
```

# Dependency Injection (Koin)

## Module Definition
* **Use singleOf/viewModelOf:** Prefer DSL functions over lambda definitions.
* **Interface Binding:** Use `bind` to bind implementation to interface.

```kotlin
val myModule = module {
    // Singleton with interface binding
    singleOf(::MyRepositoryImpl) bind MyRepository::class
    singleOf(::MyUseCaseImpl) bind MyUseCase::class

    // ViewModel scope
    viewModelOf(::MyViewModel)
}
```

## Visibility
* **Internal Implementation:** Implementation classes must be `internal`.
* **Public Interface:** Interfaces must be `public` for Koin binding.

# Naming Conventions

## Classes
* **ViewModel:** `XxxViewModel` (e.g., `HomeViewModel`, `ViewerViewModel`)
* **State:** `XxxState` (e.g., `HomeState`)
* **Intent:** `XxxIntent` (e.g., `HomeIntent`)
* **Effect:** `XxxEffect` (e.g., `HomeEffect`)
* **ViewModelState:** `XxxViewModelState` (e.g., `HomeViewModelState`)
* **UseCase:** `VerbNounUseCase` (e.g., `FetchTopHeadlineArticlesUseCase`, `GetArticleByIdUseCase`)
* **Repository:** `XxxRepository` (e.g., `NewsRepository`)
* **Koin Module:** `xxxModule` (e.g., `homeModule`, `networkModule`)

## Functions
* **UseCase Invoke:** `operator fun invoke()`
* **Repository Methods:** `fetchXxx()`, `getXxx()`, `saveXxx()`, `deleteXxx()`
* **ViewModel Actions:** `onIntent()`, `loadXxx()`, `refreshXxx()`

## Intent Naming
* **Pattern:** Intent-based naming (what you want to do), NOT operation-based (what you clicked)
* **Navigation:** `NavigateViewer`, `NavigateBack`
* **Change:** `ChangeCategory`, `ChangeSearchQuery`
* **Actions:** `RetryLoad`, `ShareArticle`, `CopyUrl`

```kotlin
sealed interface HomeIntent : Intent {
    data class NavigateViewer(val article: Article) : HomeIntent
    data class ChangeCategory(val category: NewsCategory) : HomeIntent
    data object RetryLoad : HomeIntent
}
```

## Parameters
* **Boolean Parameters:** Use descriptive names: `forceRefresh`, `isLoading`, `includeCache`
* **ID Parameters:** Use specific names: `articleId`, `categoryId` (not just `id`)

## Version Catalog (libs.versions.toml)
* **Versions:** Use camelCase: `androidGradlePlugin`, `kotlinVersion`
* **Libraries:** Use kebab-case: `androidx-core-ktx`, `koin-core`
* **Plugins:** Use kebab-case: `android-application`, `kotlin-serialization`

# Testing

## Test Location
* **commonTest Only:** All tests must be in `src/commonTest/kotlin/`.
* **No Platform-Specific Tests:** Do not create platform-specific test directories.

## Test Pattern
* **Use Fake Implementations:** Use Fakes from `core:test` module.
* **Use Turbine:** Use Turbine for Flow/StateFlow assertions.
* **Use StandardTestDispatcher:** Set main dispatcher in @BeforeTest.

```kotlin
class XxxViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUseCase: FakeXxxUseCase
    private lateinit var viewModel: XxxViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeUseCase = FakeXxxUseCase()
        viewModel = XxxViewModel(fakeUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when load succeeds then state is stable with data`() = runTest {
        fakeUseCase.setResult(Result.success(testData))

        viewModel.state.test {
            viewModel.onIntent(XxxIntent.Load)
            // Assertions
        }
    }
}
```

## Test Coverage
* **Success Path:** Test successful data loading.
* **Error Path:** Test error handling for each NewsflowError type.
* **Edge Cases:** Test empty data, cache behavior, refresh scenarios.

# Code Quality

## Static Analysis
* **Detekt:** Run `./gradlew detekt` before creating PRs.
* **Zero Issues:** All Detekt issues must be resolved.

## Best Practices
* **No Unused Code:** Remove unused imports, variables, and functions.
* **No Magic Numbers:** Define constants for numeric values.
* **Early Return:** Prefer early returns to reduce nesting.
* **Minimal Visibility:** Use `private` or `internal` by default.
* **No Hardcoded Dispatchers:** Inject dispatchers via constructor.

## Forbidden Patterns
* **No Cross-Feature Dependencies:** Feature modules must not depend on each other.
* **No Direct Repository in ViewModel:** Always use UseCase.
* **No Mutable State:** State must be immutable; use ViewModelState for mutability.
* **No Exceptions from Data/Domain:** Return Result type instead.

# Tooling
* **Static Analyzer:** Detekt - Enforces code quality and style violations.
* **Build System:** Gradle with Convention Plugins for consistent module configuration.
* **Test Framework:** kotlin-test with Turbine for Flow testing.

# Example

## Complete Feature Implementation

### State
```kotlin
sealed interface HomeState : State {
    data class Stable(
        val isLoading: Boolean,
        val currentCategory: NewsCategory,
        val articles: List<Article>,
    ) : HomeState

    data class Error(
        val error: NewsflowError,
    ) : HomeState
}
```

### Intent
```kotlin
sealed interface HomeIntent : Intent {
    data class NavigateViewer(val article: Article) : HomeIntent
    data class ChangeCategory(val category: NewsCategory) : HomeIntent
    data object RetryLoad : HomeIntent
}
```

### Effect
```kotlin
sealed interface HomeEffect : Effect {
    data class NavigateViewer(val id: String) : HomeEffect
    data class ShowError(val message: String) : HomeEffect
}
```

### ViewModelState
```kotlin
data class HomeViewModelState(
    val isLoading: Boolean = false,
    val currentCategory: NewsCategory = NewsCategory.GENERAL,
    val articles: List<Article> = emptyList(),
    val error: NewsflowError? = null,
) : ViewModelState<HomeState> {
    override fun toState(): HomeState = when {
        error != null -> HomeState.Error(error)
        else -> HomeState.Stable(isLoading, currentCategory, articles)
    }
}
```

### ViewModel
```kotlin
class HomeViewModel(
    private val fetchArticlesUseCase: FetchTopHeadlineArticlesUseCase,
) : StatefulBaseViewModel<HomeViewModelState, HomeState, HomeIntent, HomeEffect>() {

    override fun createInitialViewModelState(): HomeViewModelState = HomeViewModelState()
    override fun createInitialState(): HomeState = HomeState.Stable()

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.RetryLoad -> loadArticles(forceRefresh = true)
            is HomeIntent.NavigateViewer -> navigateToViewer(intent.article.id)
            is HomeIntent.ChangeCategory -> changeCategory(intent.category)
        }
    }

    private fun loadArticles(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            updateViewModelState { copy(isLoading = true, error = null) }

            fetchArticlesUseCase(
                category = _viewModelState.value.currentCategory.value,
                forceRefresh = forceRefresh,
            ).fold(
                onSuccess = { articles ->
                    updateViewModelState { copy(articles = articles, isLoading = false) }
                },
                onFailure = { error ->
                    updateViewModelState {
                        copy(error = error as? NewsflowError, isLoading = false)
                    }
                }
            )
        }
    }

    private fun navigateToViewer(articleId: String) {
        sendEffect(HomeEffect.NavigateViewer(articleId))
    }

    private fun changeCategory(category: NewsCategory) {
        updateViewModelState { copy(currentCategory = category) }
        loadArticles()
    }
}
```

### Koin Module
```kotlin
val homeModule = module {
    viewModelOf(::HomeViewModel)
}
```