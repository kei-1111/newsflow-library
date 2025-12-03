# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Top-Level Rules

- To maximize efficiency, **if you need to execute multiple independent processes, invoke those tools concurrently, not sequentially**.
- **You must think exclusively in English**. However, you are required to **respond in Japanese**.

## プロジェクト概要

newsflow-libraryは、Android・iOS向けのKotlin Multiplatform (KMP)ニュース配信ライブラリです。Clean Architectureの原則に基づき、厳格なモジュール分離とMVIパターンを採用しています。

## クイックリファレンス

### よく使うコマンド

| コマンド | 説明 |
|---------|------|
| `./gradlew allTests` | 全モジュールのテスト実行 |
| `./gradlew :core:domain:allTests` | 特定モジュールのテスト |
| `./gradlew detekt` | 静的解析（コード品質チェック） |
| `./gradlew clean build` | クリーン＆ビルド |
| `./gradlew assemble` | 全XCFrameworkビルド |
| `./gradlew koverHtmlReport` | カバレッジレポート生成（HTML） |
| `./gradlew koverXmlReport` | カバレッジレポート生成（XML） |

### 重要ファイル

| パス | 役割 |
|------|------|
| `core/mvi/src/.../StatefulBaseViewModel.kt` | MVI基底クラス |
| `core/model/src/.../NewsflowError.kt` | エラー型定義 |
| `core/model/src/.../Article.kt` | 記事エンティティ |
| `core/test/src/.../Fake*.kt` | テスト用Fake実装 |
| `core/test/src/.../TestDataBuilder.kt` | テストデータ生成 |
| `shared/src/.../Koin.kt` | DI初期化 |
| `build-logic/convention/src/...` | Convention Plugins |

### よく使うユーティリティ

```kotlin
// テストデータ生成
createTestArticle()
createTestArticles(n)

// ViewModel内部ステート更新
updateViewModelState { copy(...) }

// 一度きりのイベント送信
sendEffect(MyEffect.Navigate(...))
```

## ⚠️ 警告・注意事項

### 絶対に守るべきルール

1. **フィーチャーモジュール間の依存は絶対に作らない** - feature:homeからfeature:viewerへの依存など禁止
2. **実装クラスは`internal`、インターフェースは`public`** - Koinバインディングのため必須
3. **テストは`src/commonTest/kotlin/`のみ** - プラットフォーム固有テストは作成しない
4. **ViewModelStateの`toState()`は純粋関数** - 副作用禁止

### よくある間違い

- ❌ フィーチャーからcore:dataやcore:networkを直接参照 → ✅ core:domain経由のみ
- ❌ ViewModelでリポジトリを直接使用 → ✅ UseCaseを経由
- ❌ Stateをミュータブルにする → ✅ ViewModelStateでミュータブル管理、Stateはイミュータブル
- ❌ Dispatcherをハードコード → ✅ コンストラクタ注入で設定

## 開発環境セットアップ

### 必要条件

- JDK 21
- Android Studio with KMP plugin
- Xcode（iOS開発時）

### 初回セットアップ

```bash
# 依存関係のダウンロードとビルド確認
./gradlew build

# テスト実行で環境確認
./gradlew allTests
```

## コードスタイルガイドライン

### Kotlin規約

- **命名**: camelCase（関数・変数）、PascalCase（クラス・インターフェース）
- **インポート**: ワイルドカード禁止、アルファベット順
- **可視性**: 最小限の公開範囲を維持（デフォルトはprivate/internal）

### プロジェクト固有規約

```kotlin
// ✅ Good: UseCaseは単一のpublic invoke関数
interface FetchArticlesUseCase {
    suspend operator fun invoke(category: String, forceRefresh: Boolean = false): Result<List<Article>>
}

// ✅ Good: ViewModelStateからStateを導出
data class HomeViewModelState(...) : ViewModelState<HomeState> {
    override fun toState(): HomeState = when { ... }
}

// ✅ Good: Result型でエラーをラップ
suspend fun fetchArticles(): Result<List<Article>>

// ❌ Bad: 例外をスロー
suspend fun fetchArticles(): List<Article> // throws Exception
```

### Detektルール

- 長いメソッドや複雑な式は分割
- マジックナンバー禁止（定数化）
- ネスト深度は最大4レベル

## 推奨ワークフロー

### 新機能実装時

1. **探索**: 関連コードを読んで既存パターンを理解
2. **計画**: 実装ステップを整理（必要に応じてTodoWriteを使用）
3. **テスト作成**: Fakeを使った失敗テストを先に書く
4. **実装**: テストを通すコードを実装
5. **検証**: `./gradlew allTests detekt`で品質確認

### デバッグ時

1. エラーメッセージから`NewsflowError`の型を特定
2. 関連するRepository/UseCaseのテストを確認
3. Fakeの設定が正しいか検証

## アーキテクチャ概要

### モジュール依存関係フロー

```
基盤層（他のプロジェクトモジュールに依存しない）:
├── core:model（ドメインエンティティ）
└── core:network（HTTPクライアント、APIサービス）
    ↓
core:data（リポジトリパターン＋キャッシング）
    → core:model, core:network に依存
    ↓
core:domain（ユースケース - ビジネスロジック）
    → core:data, core:model に依存
    ↓
features（プレゼンテーション層＋MVI）
    → core:domain, core:model, core:mvi, core:logger に依存

独立モジュール:
├── core:mvi（ViewModel用MVIフレームワーク）
├── core:logger（プラットフォーム非依存のログ）
├── core:test（テストユーティリティとFake実装）
└── shared（Koin DI集約＆iOSエクスポート）
```

### コアモジュール詳細

| モジュール | 役割 | 主要クラス |
|-----------|------|-----------|
| core:model | ドメインエンティティ | `Article`, `NewsCategory`, `NewsflowError` |
| core:network | HTTPクライアント | `NewsApiService`, `HttpClient` |
| core:data | リポジトリ実装 | `NewsRepositoryImpl`, Mappers |
| core:domain | ユースケース | `FetchTopHeadlineArticlesUseCase`, `GetArticleByIdUseCase` |
| core:mvi | MVIフレームワーク | `StatefulBaseViewModel`, `StatelessBaseViewModel` |
| core:logger | ログ出力 | expect/actual実装 |
| core:test | テストユーティリティ | `FakeNewsRepository`, `FakeNewsApiService` |

### フィーチャーモジュール

| モジュール | 役割 |
|-----------|------|
| feature:home | カテゴリ別ニュースフィード表示 |
| feature:viewer | 記事詳細表示（SavedStateHandle経由で引数受け取り） |

## MVI実装パターン

### ファイル構成

```
feature/xxx/src/commonMain/kotlin/.../
├── XxxViewModel.kt       # ViewModel実装
├── XxxState.kt          # UI公開用ステート（sealed interface）
├── XxxIntent.kt         # ユーザーの意図
├── XxxEffect.kt         # 一度きりの副作用
├── XxxViewModelState.kt # 内部ステート（data class）
└── XxxModule.kt         # Koinモジュール
```

### 実装テンプレート

```kotlin
// ViewModelState定義
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

// ViewModel実装
class XxxViewModel(
    private val useCase: XxxUseCase,
) : StatefulBaseViewModel<XxxViewModelState, XxxState, XxxIntent, XxxEffect>(
    initialViewModelState = XxxViewModelState(),
) {
    override fun onIntent(intent: XxxIntent) {
        when (intent) {
            is XxxIntent.Load -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            updateViewModelState { copy(isLoading = true) }
            useCase().fold(
                onSuccess = { updateViewModelState { copy(data = it, isLoading = false) } },
                onFailure = { updateViewModelState { copy(error = it as? NewsflowError, isLoading = false) } }
            )
        }
    }
}
```

## 依存性注入（Koin）

### モジュール構成

```kotlin
// 各レイヤーのモジュール
networkModule: HttpClient, NewsApiService (singleton)
dataModule: NewsRepository (singleton)
domainModule: ユースケース (singleton)
homeModule: HomeViewModel (viewModel scope)
viewerModule: ViewerViewModel (viewModel scope)
```

### バインディングパターン

```kotlin
val myModule = module {
    // インターフェースバインディング
    singleOf(::MyRepositoryImpl) bind MyRepository::class
    singleOf(::MyUseCaseImpl) bind MyUseCase::class

    // ViewModelスコープ
    viewModelOf(::MyViewModel)
}
```

## テスト戦略

### テストパターン

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
    fun `success case`() = runTest {
        fakeUseCase.setResult(Result.success(testData))

        viewModel.state.test {
            viewModel.onIntent(XxxIntent.Load)
            // アサーション
        }
    }
}
```

### テストユーティリティ

- `FakeNewsApiService`: 制御可能なAPIレスポンス
- `FakeNewsRepository`: インメモリリポジトリ
- `FakeFetchTopHeadlineArticlesUseCase`: 設定可能なユースケース
- `createTestArticle()`, `createTestArticles(n)`: テストデータビルダー

## Convention Plugins

| Plugin | 用途 | 適用対象 |
|--------|------|---------|
| `newsflow.library.kmp.library` | KMPライブラリ基本設定 | coreモジュール, shared |
| `newsflow.library.kmp.feature` | フィーチャーモジュール設定 | feature/* |
| `newsflow.library.detekt` | 静的解析設定 | 全モジュール |
| `newsflow.library.kover` | テストカバレッジ設定 | 全モジュール（自動適用） |
| `newsflow.library.maven.publish` | Maven公開設定 | 公開対象モジュール |

**新規モジュール作成時**:
- coreモジュール: `alias(libs.plugins.newsflow.library.kmp.library)`
- フィーチャーモジュール: `alias(libs.plugins.newsflow.library.kmp.feature)`

## エラーハンドリング

### NewsflowError型

```kotlin
sealed class NewsflowError : Exception() {
    data object Unauthorized : NewsflowError()          // APIキー無効
    data object RateLimitExceeded : NewsflowError()     // レート制限超過
    data object BadRequest : NewsflowError()            // 不正リクエスト
    data object ServerError : NewsflowError()           // サーバーエラー
    data class NetworkFailure(val cause: Throwable?) : NewsflowError()
    data class ArticleNotFound(val id: String) : NewsflowError()
    data class InvalidParameter(val message: String) : NewsflowError()
}
```

### Result型の使用

data/domainレイヤーからは`Result<T>`を返す：
```kotlin
suspend fun fetchArticles(): Result<List<Article>>

// 使用側
useCase().fold(
    onSuccess = { articles -> ... },
    onFailure = { error -> (error as? NewsflowError)?.let { ... } }
)
```

## バージョン情報

主要バージョン（`gradle/libs.versions.toml`参照）：

| ライブラリ | バージョン |
|-----------|-----------|
| Kotlin | 2.2.20 |
| AGP | 8.11.1 |
| Koin | 4.1.1 |
| Ktor | 3.3.2 |
| Coroutines | 1.10.2 |
| Kover | 0.9.3 |
| Turbine | 1.2.1 |
| AndroidX Lifecycle | 2.9.1 |
| Target SDK | 36 |
| Min SDK | 29 |
| JVM Target | 21 |