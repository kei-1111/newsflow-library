# テスト戦略

## 参照ファイル

- テスト例: `feature/home/src/commonTest/kotlin/.../HomeViewModelTest.kt:28`
- UseCase テスト例: `core/domain/src/commonTest/kotlin/.../FetchTopHeadlineArticlesUseCaseImplTest.kt`

## テストパターン（Mokkery使用）

### UseCase テスト

```kotlin
class XxxUseCaseImplTest {

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val repository = mock<XxxRepository>()
        val testData = listOf(createTestArticle(1))
        everySuspend { repository.fetchData(any()) } returns Result.success(testData)
        val useCase = XxxUseCaseImpl(repository)

        val result = useCase("param")

        assertTrue(result.isSuccess)
        assertEquals(testData, result.getOrNull())
        verifySuspend { repository.fetchData("param") }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val repository = mock<XxxRepository>()
        val error = NewsflowError.NetworkError.NetworkFailure("Error")
        everySuspend { repository.fetchData(any()) } returns Result.failure(error)
        val useCase = XxxUseCaseImpl(repository)

        val result = useCase("param")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.NetworkError.NetworkFailure>(result.exceptionOrNull())
    }
}
```

### ViewModel テスト

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class XxxViewModelTest {
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
    fun `test state transition`() = runTest {
        val useCase = mock<XxxUseCase>()
        everySuspend { useCase(any(), any()) } returns Result.success(testData)
        val viewModel = XxxViewModel(useCase)

        viewModel.state.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertIs<XxxState.Stable>(state)
        }
    }
}
```

## Mokkeryユーティリティ

| 関数 | 用途 |
|------|------|
| `mock<T>()` | インターフェースのモック生成 |
| `everySuspend { ... } returns ...` | suspend関数のスタブ設定 |
| `verifySuspend { ... }` | 呼び出し検証 |
| `verifySuspend(exactly(n)) { ... }` | 呼び出し回数検証 |
| `any()` | 任意の引数マッチャー |

## テストデータ生成

各テストファイル内でprivate関数として定義：

```kotlin
private fun createTestArticle(index: Int, prefix: String = "Test") = Article(
    id = "$index",
    source = "$prefix Source $index",
    author = "$prefix Author $index",
    title = "$prefix Title $index",
    description = "$prefix Description $index",
    url = "https://example.com/$prefix-$index",
    imageUrl = "https://example.com/image-$index.jpg",
    publishedAt = 1234567890000L + index,
)

private fun createTestArticles(count: Int, prefix: String = "Test") =
    List(count) { createTestArticle(it + 1, prefix) }
```

## テスト配置

- テストは `src/commonTest/kotlin/` のみに配置
- プラットフォーム固有テストは作成しない