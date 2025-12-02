# 命名規則 (Naming Conventions)

このドキュメントは、newsflow-libraryプロジェクト固有の命名規則を定義します。

---

## Convention Plugin

自作プラグインはプロジェクト固有の命名であるため、他のプラグインとの差別化を図るために `ConventionPlugin` を Suffix につける。

**命名パターン**: `<Type>ConventionPlugin`

**例**:
- `KmpLibraryConventionPlugin`
- `KmpFeatureConventionPlugin`
- `DetektConventionPlugin`
- `KoverConventionPlugin`
- `MavenPublishConventionPlugin`

**Plugin ID 命名パターン**: `newsflow.library.<type>[.<subtype>]`

**例**:
- `newsflow.library.kmp.library`
- `newsflow.library.kmp.feature`
- `newsflow.library.detekt`
- `newsflow.library.kover`
- `newsflow.library.maven.publish`

---

## パッケージ名

**ベースパッケージ**: `io.github.kei_1111.newsflow.library`

**サブパッケージ構造**:
```
io.github.kei_1111.newsflow.library
├── core
│   ├── model          # ドメインエンティティ
│   ├── network        # HTTPクライアント、APIサービス
│   │   ├── api
│   │   ├── di
│   │   └── model
│   ├── data           # リポジトリ実装
│   │   ├── di
│   │   ├── mapper
│   │   ├── repository
│   │   └── util
│   ├── domain         # ユースケース
│   │   ├── di
│   │   └── usecase
│   ├── mvi            # MVIフレームワーク
│   │   ├── stateful
│   │   └── stateless
│   └── logger         # ロギング
├── feature
│   ├── home           # ホーム画面
│   │   └── di
│   └── viewer         # 記事詳細画面
│       └── di
└── shared             # DI集約 & iOSエクスポート
```

---

## モジュール命名

**Core モジュールパターン**: `core:<module-name>`
- 例: `core:model`, `core:network`, `core:data`, `core:domain`, `core:mvi`, `core:logger`

**Feature モジュールパターン**: `feature:<feature-name>`
- 例: `feature:home`, `feature:viewer`

**Shared モジュール**: `shared`

**settings.gradle.kts での記述例**:
```kotlin
include(":core:model")
include(":core:network")
include(":core:data")
include(":core:domain")
include(":core:mvi")
include(":core:logger")
include(":feature:home")
include(":feature:viewer")
include(":shared")
```

---

## バージョンカタログ

- `versions` はキャメルケース
- `libraries` はケバブケース
- `plugins` はケバブケース

**例**:
```toml
[versions]
kotlin = "2.2.20"
androidGradlePlugin = "8.11.1"
koin = "4.1.1"

[libraries]
koin-core = { group = "io.insert-koin", name = "koin-core", version.ref = "koin" }
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
newsflow-library-kmp-library = { id = "newsflow.library.kmp.library", version = "unspecified" }
```

---

## クラス・ファイル命名

### MVI 関連

| 種類 | 命名パターン | 例 |
|------|-------------|-----|
| ViewModel | `<Feature>ViewModel` | `HomeViewModel`, `ViewerViewModel` |
| State | `<Feature>State` | `HomeState`, `ViewerState` |
| Intent | `<Feature>Intent` | `HomeIntent`, `ViewerIntent` |
| Effect | `<Feature>Effect` | `HomeEffect`, `ViewerEffect` |
| ViewModelState | `<Feature>ViewModelState` | `HomeViewModelState`, `ViewerViewModelState` |

### レイヤー別

| 種類 | 命名パターン | 例 |
|------|-------------|-----|
| UseCase (Interface) | `<動詞><対象>UseCase` | `FetchTopHeadlineArticlesUseCase`, `GetArticleByIdUseCase` |
| UseCase (Impl) | `<動詞><対象>UseCaseImpl` | `FetchTopHeadlineArticlesUseCaseImpl` |
| Repository (Interface) | `<Domain>Repository` | `NewsRepository` |
| Repository (Impl) | `<Domain>RepositoryImpl` | `NewsRepositoryImpl` |
| Service (Interface) | `<Domain>ApiService` | `NewsApiService` |
| Service (Impl) | `<Domain>ApiServiceImpl` | `NewsApiServiceImpl` |
| Mapper | `<Source>Mapper` | `NewsResponseMapper` |
| Koin Module | `<Layer/Feature>Module` | `HomeModule`, `DataModule`, `NetworkModule` |

### エンティティ・モデル

| 種類 | 命名パターン | 例 |
|------|-------------|-----|
| ドメインエンティティ | 単数形の名詞 | `Article`, `NewsCategory` |
| エラー型 | `<Project>Error` | `NewsflowError` |
| API レスポンス | `<Domain>Response` | `NewsResponse` |
| API リクエスト | `<Domain>Request` | - |

### テスト

| 種類 | 命名パターン | 例 |
|------|-------------|-----|
| 単体テスト | `<TargetClass>Test` | `HomeViewModelTest`, `NewsRepositoryImplTest` |
| Fake 実装 | `Fake<Interface>` | `FakeNewsRepository`, `FakeNewsApiService` |

---

## Intent 命名

UIからViewModelへの通知を表現する。「何が起きたか」または「何が起きてほしいか」を伝える。
ユーザー操作だけでなく、システムイベント（WebViewロード完了等）も含む。

**命名パターン**: `動詞 + 対象`

**例**:
```kotlin
sealed interface HomeIntent : Intent {
    data class NavigateViewer(val article: Article) : HomeIntent
    data class ChangeCategory(val category: NewsCategory) : HomeIntent
    data class ShowArticleOverview(val article: Article) : HomeIntent
    data object DismissArticleOverview : HomeIntent
    data object CopyArticleUrl : HomeIntent
    data object ShareArticle : HomeIntent
    data object RetryLoad : HomeIntent
    data object StartWebViewLoading : HomeIntent
    data object FinishWebViewLoading : HomeIntent
}
```

**ポイント**:
- パラメータが不要な場合は `data object` を使用
- パラメータが必要な場合は `data class` を使用
- 「SwipeDown」「ClickOutside」のような操作ベースではなく、「DismissDialog」のような意図ベースの命名を使用

**設計の経緯**:
- WebViewのロード完了など、ユーザー操作ではないイベントも扱う必要がある
- 異なる操作（スワイプとタブタップ）で同じ処理を行う場合、重複を避けられる

---

## Effect 命名

ViewModelがUIでの処理が必要と判断した場合にUI層に流すもの。UIでしか処理できないもの（Navigation、Toast、Clipboard、Share Intent等）。

**命名パターン**: `動詞（命令形） + 目的語`

**例**:
```kotlin
sealed interface HomeEffect : Effect {
    data class NavigateViewer(val id: String) : HomeEffect
    data class CopyUrl(val url: String) : HomeEffect
    data class ShareArticle(val title: String, val url: String) : HomeEffect
    data class ShowToast(val message: String) : HomeEffect
}
```

**よく使う動詞**:
- `Navigate` - 画面遷移
- `Show` - UI 要素の表示
- `Hide` - UI 要素の非表示
- `Open` - 外部リソースを開く
- `Copy` - クリップボードにコピー

---

## State 命名

画面上の状態。ScreenComposableで収集し画面全体の状態を持つ。`sealed interface`で画面の状態に応じて場合分けする。

**sealed interface のサブタイプ命名**: 状態を表す名詞または形容詞

**例**:
```kotlin
sealed interface HomeState : State {
    data object Init : HomeState
    data object Loading : HomeState
    data class Stable(
        val isLoading: Boolean = false,
        val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
        val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    ) : HomeState
    data class Error(val error: NewsflowError) : HomeState
}
```

**よく使う状態名**:
- `Init` - 初期状態
- `Loading` - ローディング専用状態
- `Stable` - 通常の安定状態
- `Error` - エラー状態

**プロパティ命名**:
- `is<State>` - Boolean 型（例: `isLoading`, `isRefreshing`）
- `current<Item>` - 現在選択中の項目（例: `currentNewsCategory`）
- `<items>By<Key>` - Map 型（例: `articlesByCategory`）

---

## ViewModelState 命名

ViewModelが内部で持っている状態。UIに公開しない内部状態で、Stateに変換される前の生データやフラグを保持する。

**命名パターン**: `<Feature>ViewModelState`

**内部 enum 命名**: `StatusType`

**例**:
```kotlin
data class HomeViewModelState(
    val statusType: StatusType = StatusType.STABLE,
    val isLoading: Boolean = false,
    val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
    val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    val error: NewsflowError? = null,
) : ViewModelState<HomeState> {

    enum class StatusType { STABLE, ERROR }

    override fun toState(): HomeState = when (statusType) {
        StatusType.STABLE -> HomeState.Stable(...)
        StatusType.ERROR -> HomeState.Error(...)
    }
}
```

---

## UseCase 命名

**命名パターン**: `<動詞><対象>UseCase`

**動詞の選択基準**:

| 動詞 | 用途 | 例 |
|------|------|-----|
| `Fetch` | リモートからデータ取得 | `FetchTopHeadlineArticlesUseCase` |
| `Get` | ローカル/キャッシュからデータ取得 | `GetArticleByIdUseCase` |
| `Save` | データ保存 | `SaveArticleUseCase` |
| `Delete` | データ削除 | `DeleteArticleUseCase` |
| `Update` | データ更新 | `UpdateSettingsUseCase` |
| `Search` | 検索 | `SearchArticlesUseCase` |
| `Validate` | バリデーション | `ValidateInputUseCase` |

---

## エラー型命名

**命名パターン**: sealed class で階層化

```kotlin
sealed class NewsflowError(message: String) : Exception(message) {
    // ネットワーク関連エラー
    sealed class NetworkError(message: String) : NewsflowError(message) {
        data class Unauthorized(...) : NetworkError(...)
        data class RateLimitExceeded(...) : NetworkError(...)
        data class BadRequest(...) : NetworkError(...)
        data class ServerError(...) : NetworkError(...)
        data class NetworkFailure(...) : NetworkError(...)
    }

    // 内部エラー
    sealed class InternalError(message: String) : NewsflowError(message) {
        data class ArticleNotFound(...) : InternalError(...)
        data class InvalidParameter(...) : InternalError(...)
    }
}
```

---

## テストメソッド命名

**命名パターン**: バッククォートで囲んだ説明的な文

```kotlin
@Test
fun `should return success when articles are fetched successfully`() { ... }

@Test
fun `should return error when network fails`() { ... }

@Test
fun `should update state when action is received`() { ... }
```

**構造**: `should <期待結果> when <条件>`

---

## 定数命名

**命名パターン**: SCREAMING_SNAKE_CASE

```kotlin
companion object {
    private const val DEFAULT_TIMEOUT_MS = 30_000L
    private const val MAX_RETRY_COUNT = 3
    private const val CACHE_DURATION_MINUTES = 5
}
```

---

## 拡張関数ファイル命名

**命名パターン**: `<対象クラス>Extensions.kt`

**例**:
- `ResultExtensions.kt`
- `FlowExtensions.kt`

---

## まとめ

| カテゴリ | 規則 |
|---------|------|
| パッケージ | 小文字、ドット区切り |
| クラス・インターフェース | PascalCase |
| 関数・変数 | camelCase |
| 定数 | SCREAMING_SNAKE_CASE |
| ファイル | クラス名と同一 |
| テストメソッド | バッククォート + 説明文 |
| モジュール | ケバブケース（`core:model`, `feature:home`） |