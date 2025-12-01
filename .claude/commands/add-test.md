# Add Test Generator

指定したクラスに対するテストファイルを生成します。

## 引数
- $ARGUMENTS: テスト対象のクラス名またはファイルパス（例: `HomeViewModel`, `feature/home/.../HomeViewModel.kt`）

## 実行手順

1. **引数の検証**: `$ARGUMENTS` が空の場合、ユーザーにテスト対象を質問してください

2. **対象ファイルの特定**:
   - クラス名のみの場合: `Glob` や `Grep` でファイルを検索
   - ファイルパスの場合: 直接読み込み

3. **対象ファイルの分析**: ファイルを読み込み、以下を把握
   - パッケージ名
   - クラス名
   - 依存関係（コンストラクタ引数）
   - public メソッド
   - クラスの種類（ViewModel, UseCase, Repository など）

4. **テストファイルの生成**: 適切なテストテンプレートを使用

### ViewModelの場合

```kotlin
package {package}

import app.cash.turbine.test
import io.github.kei_1111.newsflow.library.core.test.model.createTestArticle
import io.github.kei_1111.newsflow.library.core.test.model.createTestArticles
// 依存するFakeクラスをimport
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class {ClassName}Test {

    private val testDispatcher = StandardTestDispatcher()
    // Fake依存関係をここに宣言

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Fakeの初期化
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        // Arrange
        val viewModel = {ClassName}(/* 依存関係 */)

        // Assert
        viewModel.state.test {
            val state = awaitItem()
            // 初期状態のアサーション
        }
    }

    // 各Intentに対するテストケースを生成
    // 成功パスと失敗パスの両方を含める
}
```

### UseCaseの場合

```kotlin
package {package}

import io.github.kei_1111.newsflow.library.core.test.repository.Fake{Repository}
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class {ClassName}Test {

    private lateinit var repository: Fake{Repository}
    private lateinit var useCase: {ClassName}

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        // Arrange
        repository = Fake{Repository}()
        useCase = {ClassName}(repository)
        // repositoryの戻り値を設定

        // Act
        val result = useCase.invoke(/* パラメータ */)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        // Arrange
        repository = Fake{Repository}()
        useCase = {ClassName}(repository)
        val error = NewsflowError.NetworkError.NetworkFailure("Error")
        // repository.setResult(Result.failure(error))

        // Act
        val result = useCase.invoke(/* パラメータ */)

        // Assert
        assertTrue(result.isFailure)
    }
}
```

### Repositoryの場合

```kotlin
package {package}

import io.github.kei_1111.newsflow.library.core.test.network.FakeNewsApiService
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class {ClassName}Test {

    private lateinit var apiService: FakeNewsApiService
    private lateinit var repository: {ClassName}

    @Test
    fun `fetch returns success and caches result`() = runTest {
        // Arrange
        apiService = FakeNewsApiService()
        repository = {ClassName}(apiService)
        // apiServiceの戻り値を設定

        // Act
        val result = repository.fetch(/* パラメータ */)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `fetch returns cached data on second call`() = runTest {
        // キャッシュの検証
    }

    @Test
    fun `fetch with forceRefresh bypasses cache`() = runTest {
        // forceRefreshの検証
    }
}
```

5. **テストファイルの配置**:
   - `src/commonTest/kotlin/{package}/{ClassName}Test.kt` に配置
   - 既存のテストファイルがある場合は警告

6. **Fakeクラスの確認**:
   - 必要なFakeクラスが `core:test` に存在するか確認
   - 存在しない場合は作成を提案

7. **ビルド確認**: 該当モジュールのテストを実行
   - `./gradlew :{module}:allTests`

## テスト命名規則

- バッククォートでメソッド名を囲む: `` `メソッドの動作を説明` ``
- 形式: `{action} {expected result} when {condition}`
- 例:
  - `` `initialization fetches articles successfully` ``
  - `` `invoke returns failure when repository fails` ``
  - `` `onClickRetryButton refetches current category` ``