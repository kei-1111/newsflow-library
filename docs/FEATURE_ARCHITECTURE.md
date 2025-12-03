# Feature Architecture

本プロジェクトではMVI (Model-View-Intent) をベースにしたアーキテクチャを採用しています。

## MVIパターンの基本概念

画面上の状態を`State`、ViewModelが内部で持つ状態を`ViewModelState`、UIからViewModelへの通知を`Intent`、UIで処理すべきものを`Effect`とする。

### アーキテクチャ概要図

```
┌─────────────────────────────────────────────────────────────────┐
│                           UI Layer                              │
│  ┌─────────────┐                          ┌─────────────┐       │
│  │   Intent    │                          │   Effect    │       │
│  │  (すべての   │                          │ (UIでの処理が │       │
│  │   アクション) │                          │  必要なもの) │       │
│  └─────────────┘                          └─────────────┘       │
│         │                                        ▲              │
└─────────┼────────────────────────────────────────┼──────────────┘
          │                                        │
          ▼                                        │
┌─────────────────────────────────────────────────────────────────┐
│                        ViewModel                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  Intent受信 → 状態更新 and/or Effect発行                   │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### 基本原則

すべてのアクションはIntentとしてViewModelに送信し、ViewModelが状態更新やEffect発行を判断する。

### フロー例

- `Intent.SelectArticle(article)` → State更新のみ
- `Intent.NavigateViewer(article)` → `Effect.NavigateViewer(id)` を発行
- `Intent.ShareArticle` → `Effect.ShareArticle(title, url)` を発行

---

## 各要素の詳細

### State

画面上の状態。ScreenComposableで収集し画面全体の状態を持つ。`sealed interface`で画面の状態に応じて`data class`をもち場合分けする。
`Init`, `Loading`, `Stable`, `Error`など（完全なUIの出し分けをしないのであればStableだけでいい）。

```kotlin
sealed interface HomeState {
    data object Init : HomeState
    data object Loading : HomeState
    data class Stable(
        val articles: List<Article>,
        val selectedArticle: Article?
    ) : HomeState
    data class Error(val error: NewsflowError) : HomeState
}
```

#### 命名規則

基本的に名詞を使用する。

**例**: `userSetting`, `error`, `selectedArticle`, `articles`

---

### ViewModelState

ViewModelが内部で持っている状態。UIに公開しない内部状態で、Stateに変換される前の生データやフラグを保持する。`data class`ですべて一括で持つ。
現在がどの状態なのかを判断するための内部で定義したフラグ`statusType`や、初期状態（画面では使わない）を持ちそことの変更があるかどうかでボタンを押せるかどうかを判定など。

```kotlin
data class HomeViewModelState(
   val statusType: StatusType = StatusType.STABLE,
   val isLoading: Boolean = false,
   val selectedArticle: Article? = null,
   val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
   val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
   val initialUserSetting: UserSetting? = null,
   val currentUserSetting: UserSetting? = null,
   val error: NewsflowError? = null,
) : ViewModelState<HomeState> {
   enum class StatusType { STABLE, ERROR }

   override fun toState(): HomeState = when (statusType) {
      StatusType.STABLE -> HomeState.Stable(
         isLoading = isLoading,
         isSaveButtonEnabled = initialUserSetting != currentUserSetting,
         selectedArticle = selectedArticle,
         currentNewsCategory = currentNewsCategory,
         articlesByCategory = articlesByCategory,
         currentUserSetting = currentUserSetting,
      )

      StatusType.ERROR -> HomeState.Error(
         error = requireNotNull(error) { "Error must not be null when statusType is ERROR" }
      )
   }
}
```

#### 命名規則

基本的に名詞（Stateと基本的に変わりなし）。

**例**: `userSetting`, `error`, `selectedArticle`, `statusType`

---

### Intent

UIからViewModelへの通知。「ViewModelにしてほしいこと」を伝える。
ViewModelはこれを受け取り、状態の更新やEffectの発行を判断する。ユーザー操作だけでなく、システムイベント（WebViewロード完了等）も含む。

```kotlin
sealed interface HomeIntent {
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

#### 命名規則

`動詞 + 対象` の形式を使用する。

**例**: `SelectArticle`, `StartWebViewLoad`, `ShareArticle`, `NavigateViewer`, `DismissDialog`

#### 設計の経緯

Intentを「ユーザー操作によるAction」ではなく「UIからの通知」と定義した理由:

- WebViewのロード完了など、ユーザー操作ではないイベントも扱う必要がある
- 異なる操作（スワイプとタブタップ）で同じ処理を行う場合、重複を避けられる
- 「SwipeDown」「ClickOutside」のような操作ベースの命名は直感的でない
  → 「DismissBottomSheet」「DismissDialog」のような意図ベースの命名が明確

#### ❌ バッドケース：操作ベースの命名

以下のような「何をクリックしたか」「どの操作を行ったか」に基づく命名は**避ける**。

```kotlin
// ❌ Bad: 操作ベースの命名
sealed interface HomeIntent : Intent {
    data class OnClickArticleCard(val article: Article) : HomeIntent
    data class OnSwipeCategory(val category: NewsCategory) : HomeIntent
    data class OnClickCategoryTab(val category: NewsCategory) : HomeIntent
    data object OnClickRetryButton : HomeIntent
    data object OnClickShareButton : HomeIntent
    data object OnLongPressArticle : HomeIntent
    data object OnSwipeDownToRefresh : HomeIntent
}
```

**問題点**:

- **重複の発生**: `OnSwipeCategory`と`OnClickCategoryTab`が同じ処理を行う場合、2つのIntentを処理する必要がある
- **意図が不明確**: `OnLongPressArticle`が何をしたいのか（プレビュー表示？削除？共有？）が名前から分からない
- **UI実装への依存**: スワイプをタップに変更した場合、Intent名も変更が必要になる

#### ✅ グッドケース：意図ベースの命名

「何をしたいか」に基づく命名を使用します。

```kotlin
// ✅ Good: 意図ベースの命名
sealed interface HomeIntent : Intent {
    data class NavigateViewer(val article: Article) : HomeIntent
    data class ChangeCategory(val category: NewsCategory) : HomeIntent
    data object RetryLoad : HomeIntent
    data object ShareArticle : HomeIntent
    data class ShowArticleOverview(val article: Article) : HomeIntent
    data object RefreshArticles : HomeIntent
}
```

**利点**:

- **重複なし**: スワイプでもタップでも`ChangeCategory`を発行すればよい
- **意図が明確**: `ShowArticleOverview`で何がしたいか一目瞭然
- **UI非依存**: 操作方法が変わってもIntent名を変更する必要がない

---

### Effect

ViewModelがUIでの処理が必要と判断した場合にUI層に流すもの。UIでしか処理できないもの（Navigation、Toast、Clipboard、Share Intent等）。
大本のScreen ComposableのLaunchedEffectでcollectし、そこで処理を行う。

```kotlin
sealed interface HomeEffect {
    data class NavigateViewer(val id: String) : HomeEffect
    data class CopyUrl(val url: String) : HomeEffect
    data class ShareArticle(val title: String, val url: String) : HomeEffect
    data class ShowToast(val message: String) : HomeEffect
}
```

#### 命名規則

`動詞 + 対象` の形式を使用する。

**例**: `NavigateViewer`, `ShowToast`, `CopyUrl`, `ShareArticle`

---

## BaseViewModelの種類

画面の特性に応じて2種類のBaseViewModelを使い分けます。

### 1. StatefulBaseViewModel（状態管理が必要な画面用）

状態管理が必要な画面で使用します。ViewModelの内部状態（ViewModelState）とUIに公開される状態（State）を分離することで、ViewModelの実装詳細をUIから隠蔽します。

#### 定義

```kotlin
interface Intent
interface Effect
interface State

interface ViewModelState<S : State> {
    fun toState(): S
}

abstract class StatefulBaseViewModel<VS : ViewModelState<S>, S : State, I : Intent, E : Effect> : ViewModel() {

    protected val _viewModelState = MutableStateFlow<VS>(createInitialViewModelState())
    val state: StateFlow<S> = _viewModelState
        .map(ViewModelState<S>::toState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = createInitialState(),
        )

    protected val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    protected abstract fun createInitialViewModelState(): VS
    protected abstract fun createInitialState(): S

    abstract fun onIntent(intent: I)

    protected fun updateViewModelState(update: VS.() -> VS) {
        _viewModelState.update { update(it) }
    }

    protected fun sendEffect(effect: E) {
        _effect.trySend(effect)
    }
}
```

#### 特徴

- **内部状態と公開状態の分離**: `ViewModelState`（内部）と`State`（公開）を分けることで、実装詳細を隠蔽
- **toState()による変換**: `ViewModelState`から`State`への変換ロジックを集約
- **最小ローディング時間**: UX向上のため、ローディング表示の最小時間を保証

### 2. StatelessBaseViewModel（状態管理が不要な画面用）

状態管理が不要で、副作用（ナビゲーション等）のみを扱う画面で使用します。

#### 定義

```kotlin
abstract class StatelessBaseViewModel<I : Intent, E : Effect> : ViewModel() {

    protected val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    abstract fun onIntent(intent: I)

    protected fun sendEffect(effect: E) {
        _effect.trySend(effect)
    }
}
```

#### 特徴

- **状態管理なし**: `State`を持たず、IntentとEffectのみ
- **軽量**: 単純な画面遷移のみを行うシンプルな実装

---

## 実装例：:feature:home

:feature:home（ニュース一覧画面）を例に、各要素の実装を説明します。

### ViewModelStateとState

#### ViewModelState（内部状態）

ViewModelの実装詳細を含む内部状態です。`statusType`で画面の状態（STABLE/ERROR）を管理し、`toState()`メソッドでStateに変換します。

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
        StatusType.STABLE -> HomeState.Stable(
            isLoading = isLoading,
            currentNewsCategory = currentNewsCategory,
            articlesByCategory = articlesByCategory
        )
        StatusType.ERROR -> HomeState.Error(
            error = requireNotNull(error) { "Error must not be null when statusType is ERROR" }
        )
    }
}
```

**ポイント**:
- `statusType`で状態パターンを管理
- `toState()`で状態に応じた`State`を生成
- エラー状態では`error`の非null性を保証

#### State（UI公開状態）

UIに公開される状態です。sealed interfaceで定義し、画面の状態に応じた分岐を行います。

```kotlin
sealed interface HomeState : State {
    data class Stable(
        val isLoading: Boolean = false,
        val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
        val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    ) : HomeState

    data class Error(
        val error: NewsflowError,
    ) : HomeState
}
```

**ポイント**:
- sealed interfaceで状態パターンを定義
- 各状態が必要なデータのみを持つ
- イミュータブルなデータクラス

### Intent

UIからの通知を表現するインターフェースです。各操作を型安全に表現します。

```kotlin
sealed interface HomeIntent : Intent {
    data class NavigateViewer(val article: Article) : HomeIntent
    data class ChangeCategory(val newsCategory: NewsCategory) : HomeIntent
    data object RetryLoad : HomeIntent
}
```

**ポイント**:
- 各操作を個別の型として定義
- 必要なデータをプロパティとして持つ
- パラメータ不要な操作は`data object`

### Effect

UIでの処理が必要な副作用を表現します。

```kotlin
sealed interface HomeEffect : Effect {
    data class NavigateViewer(val id: String) : HomeEffect
}
```

**ポイント**:
- ナビゲーションなど、UIでしか処理できないもの
- UIが購読して実行する
- 状態ではなくイベントとして扱う

### ViewModel実装

各要素を統合したViewModelの実装例です。

```kotlin
class HomeViewModel(
    private val fetchTopHeadlineArticlesUseCase: FetchTopHeadlineArticlesUseCase
) : StatefulBaseViewModel<HomeViewModelState, HomeState, HomeIntent, HomeEffect>() {

    override fun createInitialViewModelState(): HomeViewModelState = HomeViewModelState()
    override fun createInitialState(): HomeState = HomeState.Stable()

    init {
        // 初期カテゴリの記事を取得
        fetchArticles(_viewModelState.value.currentNewsCategory)
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.NavigateViewer -> {
                sendEffect(HomeEffect.NavigateViewer(intent.article.id))
            }

            is HomeIntent.ChangeCategory -> {
                changeNewsCategory(intent.newsCategory)
            }

            is HomeIntent.RetryLoad -> {
                fetchArticles(_viewModelState.value.currentNewsCategory)
            }
        }
    }

    private fun changeNewsCategory(newCategory: NewsCategory) {
        updateViewModelState {
            copy(currentNewsCategory = newCategory)
        }
        // キャッシュがない場合のみ取得
        if (_viewModelState.value.articlesByCategory[newCategory] == null) {
            fetchArticles(newCategory)
        }
    }

    private fun fetchArticles(category: NewsCategory) {
        setLoadingState()
        viewModelScope.launch {
            fetchTopHeadlineArticlesUseCase.invoke(category.value)
                .onSuccess { data ->
                    handleFetchTopHeadlineArticlesSuccess(category, data)
                }
                .onFailure { error ->
                    handleFetchTopHeadlineArticlesError(error)
                }
        }
    }

    private fun setLoadingState() {
        updateViewModelState {
            copy(
                statusType = HomeViewModelState.StatusType.STABLE,
                isLoading = true,
            )
        }
    }

    private suspend fun handleFetchTopHeadlineArticlesSuccess(
        category: NewsCategory,
        data: List<Article>,
    ) {
        updateViewModelState {
            copy(
                isLoading = false,
                articlesByCategory = articlesByCategory + (category to data)
            )
        }
    }

    private suspend fun handleFetchTopHeadlineArticlesError(error: Throwable) {
        Logger.e("HomeViewModel", "Failed to fetch articles: ${error.message}", error)
        updateViewModelState {
            copy(
                statusType = HomeViewModelState.StatusType.ERROR,
                isLoading = false,
                error = error as? NewsflowError
            )
        }
    }
}
```

**実装のポイント**:

1. **onIntent()での一元管理**: 全ての通知を一箇所で処理
2. **カテゴリ別キャッシング**: `articlesByCategory`で取得済みデータを保持
3. **エラーハンドリング**: 型安全な`NewsflowError`での処理

### Koin DI設定

ViewModelはKoinで依存性注入されます。

```kotlin
val homeModule = module {
    viewModelOf(::HomeViewModel)
}
```

---

## データフロー詳細

実際のユーザー操作からUI更新までの流れを説明します。

### 例：記事カードをクリックして詳細画面へ遷移

1. **ユーザー操作**: 記事カードをクリック
2. **Intentの発行**: `onIntent(HomeIntent.NavigateViewer(article))`が実行される
3. **ViewModelでの処理**:
   ```kotlin
   override fun onIntent(intent: HomeIntent) {
       when (intent) {
           is HomeIntent.NavigateViewer -> {
               sendEffect(HomeEffect.NavigateViewer(intent.article.id))
           }
           // ...
       }
   }
   ```
4. **Effectの送信**: `NavigateViewer`がChannelに送信される
5. **UIでの購読と実行**: UIが`effect`を購読し、ナビゲーション処理を実行

### 例：カテゴリ切り替えで記事を取得

1. **ユーザー操作**: カテゴリタグをクリック
2. **Intentの発行**: `onIntent(HomeIntent.ChangeCategory(newsCategory))`
3. **ViewModelでの処理**:
   ```kotlin
   private fun changeNewsCategory(newCategory: NewsCategory) {
       // 内部状態を更新
       updateViewModelState {
           copy(currentNewsCategory = newCategory)
       }
       // キャッシュチェック
       if (_viewModelState.value.articlesByCategory[newCategory] == null) {
           fetchArticles(newCategory)
       }
   }
   ```
4. **状態更新**:
   - ViewModelStateの`currentNewsCategory`が更新される
   - `toState()`が呼ばれ、新しい`State`が生成される
5. **UI再描画**: `StateFlow<State>`の変更をUIが検知し、再描画

### 例：記事取得（非同期処理）

1. **取得開始**: `fetchArticles()`が呼ばれる
2. **ローディング状態**:
   ```kotlin
   updateViewModelState {
       copy(
           statusType = StatusType.STABLE,
           isLoading = true,
       )
   }
   ```
3. **UseCaseの実行**: `fetchTopHeadlineArticlesUseCase.invoke()`
4. **成功時の処理**:
   - 最小ローディング時間を保証
   - 記事データをキャッシュに追加
   - `isLoading = false`で完了
5. **失敗時の処理**:
   - エラーログ出力
   - `statusType = ERROR`に変更
   - `toState()`で`State.Error`が生成される

---

## アーキテクチャの利点

### 1. 単方向データフロー
- データの流れが予測可能
- デバッグが容易
- 状態管理が明確

### 2. テスタビリティ
- ViewModelStateはピュアなデータクラス
- UseCaseの注入により単体テスト可能
- Fakeの実装が容易（`core:test`モジュール）

### 3. 関心の分離
- ViewModel: ビジネスロジックと状態管理
- State: UI表示に必要な情報のみ
- ViewModelState: 実装詳細の隠蔽

### 4. 型安全性
- sealed interfaceによる状態パターン
- Kotlinの型システムを最大限活用
- コンパイル時エラー検出

### 5. スケーラビリティ
- 新しい状態・Intent・Effectの追加が容易
- モジュール間の依存関係が明確
- Convention Pluginによる一貫性

---

## ベストプラクティス

### ViewModelState設計
- `toState()`で状態変換ロジックを集約
- 必須エラー情報は`requireNotNull()`で保証
- enum classで状態パターンを管理

### Intent処理
- `onIntent()`ですべての通知を一元管理
- 複雑な処理は別メソッドに分離
- 副作用は`sendEffect()`で送信

### 非同期処理
- `viewModelScope.launch`で起動
- エラーハンドリングを忘れずに

### キャッシング
- 適切な粒度でデータを保持
- メモリ効率を考慮
- エラー時のキャッシュクリアを検討

### ログ出力
- `core:logger`の`Logger`を使用
- エラー時は必ずログ出力
- プラットフォーム非依存