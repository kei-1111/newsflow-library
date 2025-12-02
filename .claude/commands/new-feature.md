# New Feature Module Generator

新規フィーチャーモジュールを作成します。

## 引数
- $ARGUMENTS: フィーチャー名（例: `search`, `bookmark`, `settings`）

## 実行手順

1. **引数の検証**: `$ARGUMENTS` が空の場合、ユーザーにフィーチャー名を質問してください

2. **モジュール構造の作成**: 以下のファイルを生成してください

### ディレクトリ構造
```
feature/{name}/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/io/github/kei_1111/newsflow/library/feature/{name}/
    │   ├── {Name}ViewModel.kt
    │   ├── {Name}ViewModelState.kt
    │   ├── {Name}State.kt
    │   ├── {Name}Intent.kt
    │   ├── {Name}Effect.kt
    │   └── di/
    │       └── {Name}Module.kt
    └── commonTest/kotlin/io/github/kei_1111/newsflow/library/feature/{name}/
        └── {Name}ViewModelTest.kt
```

### 各ファイルのテンプレート

#### build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.newsflow.library.kmp.feature)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kei_1111.newsflow.library.feature.{name}"
    }
}
```

#### {Name}State.kt
```kotlin
package io.github.kei_1111.newsflow.library.feature.{name}

import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.State

sealed interface {Name}State : State {
    data class Stable(
        val isLoading: Boolean = false,
    ) : {Name}State

    data class Error(
        val error: NewsflowError,
    ) : {Name}State
}
```

#### {Name}ViewModelState.kt
```kotlin
package io.github.kei_1111.newsflow.library.feature.{name}

import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.ViewModelState

data class {Name}ViewModelState(
    val statusType: StatusType = StatusType.STABLE,
    val isLoading: Boolean = false,
    val error: NewsflowError? = null,
) : ViewModelState<{Name}State> {
    enum class StatusType { STABLE, ERROR }

    override fun toState(): {Name}State = when (statusType) {
        StatusType.STABLE -> {Name}State.Stable(
            isLoading = isLoading,
        )
        StatusType.ERROR -> {Name}State.Error(
            error = requireNotNull(error) { "Error must not be null when statusType is ERROR" }
        )
    }
}
```

#### {Name}Intent.kt
```kotlin
package io.github.kei_1111.newsflow.library.feature.{name}

import io.github.kei_1111.newsflow.library.core.mvi.Intent

sealed interface {Name}Intent : Intent {
    // TODO: Add intents
}
```

#### {Name}Effect.kt
```kotlin
package io.github.kei_1111.newsflow.library.feature.{name}

import io.github.kei_1111.newsflow.library.core.mvi.Effect

sealed interface {Name}Effect : Effect {
    data object NavigateBack : {Name}Effect
}
```

#### {Name}ViewModel.kt
```kotlin
package io.github.kei_1111.newsflow.library.feature.{name}

import io.github.kei_1111.newsflow.library.core.mvi.stateful.StatefulBaseViewModel

class {Name}ViewModel : StatefulBaseViewModel<{Name}ViewModelState, {Name}State, {Name}Intent, {Name}Effect>() {

    override fun createInitialViewModelState(): {Name}ViewModelState = {Name}ViewModelState()
    override fun createInitialState(): {Name}State = {Name}State.Stable()

    override fun onIntent(intent: {Name}Intent) {
        when (intent) {
            // TODO: Handle intents
        }
    }

    private companion object {
        const val TAG = "{Name}ViewModel"
    }
}
```

#### di/{Name}Module.kt
```kotlin
package io.github.kei_1111.newsflow.library.feature.{name}.di

import io.github.kei_1111.newsflow.library.feature.{name}.{Name}ViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val {name}Module = module {
    viewModelOf(::{Name}ViewModel)
}
```

#### {Name}ViewModelTest.kt
```kotlin
package io.github.kei_1111.newsflow.library.feature.{name}

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class {Name}ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Stable`() = runTest {
        val viewModel = {Name}ViewModel()

        viewModel.state.test {
            val initialState = awaitItem()
            assertIs<{Name}State.Stable>(initialState)
        }
    }
}
```

3. **settings.gradle.ktsへの追加**: モジュールをsettings.gradle.ktsに追加してください
   ```kotlin
   include(":feature:{name}")
   ```

4. **sharedモジュールへの統合案内**: 以下の手順をユーザーに案内してください
   - `shared/src/androidMain/kotlin/io/github/kei_1111/newsflow/library/shared/Koin.kt` の `modules()` に `{name}Module` を追加
   - `shared/src/iosMain/kotlin/io/github/kei_1111/newsflow/library/shared/Koin.kt` の `modules()` にも追加
   - importに `io.github.kei_1111.newsflow.library.feature.{name}.di.{name}Module` を追加

5. **ビルド確認**: `./gradlew :feature:{name}:allTests` を実行してビルドが成功することを確認