---
name: newsflow-testing
description: newsflow-libraryのテスト作成規約を適用します。*Test.ktファイル作成、Fake実装作成、テストパターンの質問時に使用してください。
---

# Newsflow Testing Rules

## 配置ルール

全テストは `src/commonTest/kotlin/` に配置（androidTest/iosTest不使用）。

## 利用可能なFake（core:test）

```kotlin
// UseCase
io.github.kei_1111.newsflow.library.core.test.usecase.FakeFetchTopHeadlineArticlesUseCase
  → setResult(Result<List<Article>>)

// Repository
io.github.kei_1111.newsflow.library.core.test.repository.FakeNewsRepository
  → setFetchResult(Result<List<Article>>)
  → setGetByIdResult(Result<Article>)

// API Service
io.github.kei_1111.newsflow.library.core.test.network.FakeNewsApiService

// テストデータ
createTestArticle(id: Int): Article
createTestArticles(count: Int): List<Article>
```

## ViewModelテスト構造

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class {Name}ViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUseCase: Fake{UseCase}

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeUseCase = Fake{UseCase}()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization succeeds`() = runTest {
        fakeUseCase.setResult(Result.success(createTestArticles(3)))
        val viewModel = {Name}ViewModel(fakeUseCase)

        viewModel.state.test {
            skipItems(2)
            testDispatcher.scheduler.advanceUntilIdle()
            assertIs<{Name}State.Stable>(awaitItem())
        }
    }
}
```

## 必須テストケース

### ViewModel
- [ ] 初期化成功 → Stable状態
- [ ] 初期化失敗 → Error状態
- [ ] 各Intentの処理
- [ ] Effect発行

### UseCase
- [ ] Repository成功時 → Result.success
- [ ] Repository失敗時 → Result.failure

### Repository
- [ ] API成功時 → キャッシュ保存
- [ ] 2回目呼び出し → キャッシュ使用
- [ ] forceRefresh=true → キャッシュバイパス

## 命名規則

```kotlin
`{action} {expected} when {condition}`

// 例
`initialization fetches articles successfully`
`invoke returns failure when repository fails`
```