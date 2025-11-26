# New UseCase Generator

新規ユースケースを `core:domain` モジュールに作成します。

## 引数
- $ARGUMENTS: ユースケース名（例: `SearchArticles`, `BookmarkArticle`, `GetUserSettings`）

## 実行手順

1. **引数の検証**: `$ARGUMENTS` が空の場合、ユーザーにユースケース名を質問してください。命名規則は動詞+名詞（例: `FetchArticles`, `SaveBookmark`）

2. **必要な情報の確認**: ユーザーに以下を質問してください
   - 入力パラメータ（例: `category: String`, `articleId: String`）
   - 戻り値の型（例: `Result<List<Article>>`, `Result<Article>`, `Result<Unit>`）
   - 依存するリポジトリ（例: `NewsRepository`, `BookmarkRepository`）

3. **ファイルの生成**: 以下のファイルを作成してください

### ディレクトリ構造
```
core/domain/src/
├── commonMain/kotlin/io/github/kei_1111/newsflow/library/core/domain/usecase/
│   ├── {UseCaseName}UseCase.kt          # インターフェース（public）
│   └── {UseCaseName}UseCaseImpl.kt      # 実装（internal）
└── commonTest/kotlin/io/github/kei_1111/newsflow/library/core/domain/usecase/
    └── {UseCaseName}UseCaseImplTest.kt  # テスト
```

### 各ファイルのテンプレート

#### {UseCaseName}UseCase.kt（インターフェース）
```kotlin
package io.github.kei_1111.newsflow.library.core.domain.usecase

// 必要なimportを追加

interface {UseCaseName}UseCase {
    suspend operator fun invoke(
        // パラメータをここに
    ): Result</* 戻り値の型 */>
}
```

#### {UseCaseName}UseCaseImpl.kt（実装）
```kotlin
package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.data.repository.{Repository}
// 必要なimportを追加

internal class {UseCaseName}UseCaseImpl(
    private val repository: {Repository},
) : {UseCaseName}UseCase {
    override suspend operator fun invoke(
        // パラメータをここに
    ): Result</* 戻り値の型 */> =
        repository.{methodName}(/* パラメータ */)
}
```

#### {UseCaseName}UseCaseImplTest.kt（テスト）
```kotlin
package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.test.repository.Fake{Repository}
// 必要なimportを追加
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class {UseCaseName}UseCaseImplTest {

    private lateinit var repository: Fake{Repository}
    private lateinit var useCase: {UseCaseName}UseCaseImpl

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        // Arrange
        repository = Fake{Repository}()
        useCase = {UseCaseName}UseCaseImpl(repository)
        // repository.setResult(Result.success(...))

        // Act
        val result = useCase.invoke(/* パラメータ */)

        // Assert
        assertTrue(result.isSuccess)
        // assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        // Arrange
        repository = Fake{Repository}()
        useCase = {UseCaseName}UseCaseImpl(repository)
        // val error = NewsflowError.NetworkError.NetworkFailure("Error")
        // repository.setResult(Result.failure(error))

        // Act
        val result = useCase.invoke(/* パラメータ */)

        // Assert
        assertTrue(result.isFailure)
    }
}
```

4. **DomainModuleへの登録**: `core/domain/src/commonMain/kotlin/io/github/kei_1111/newsflow/library/core/domain/di/DomainModule.kt` に追加
```kotlin
singleOf(::{UseCaseName}UseCaseImpl) bind {UseCaseName}UseCase::class
```
   - importに以下を追加:
     - `import io.github.kei_1111.newsflow.library.core.domain.usecase.{UseCaseName}UseCase`
     - `import io.github.kei_1111.newsflow.library.core.domain.usecase.{UseCaseName}UseCaseImpl`

5. **Fakeクラスの作成案内**: テスト用のFakeクラスが必要な場合、`core:test` モジュールへの追加を案内
```
core/test/src/commonMain/kotlin/io/github/kei_1111/newsflow/library/core/test/usecase/
└── Fake{UseCaseName}UseCase.kt
```

6. **ビルド確認**: `./gradlew :core:domain:allTests` を実行してテストが成功することを確認