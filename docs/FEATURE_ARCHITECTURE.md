# フィーチャーアーキテクチャ

本プロジェクトではMVI (Model-View-Intent) をベースにしたアーキテクチャを採用しています。

## MVIパターンの基本概念

データフローは単方向で、以下の要素で構成されます：

- **UiAction** - ユーザー操作をViewModelへ渡す入力
- **UiState** - UIに公開される画面描画用の状態（Immutable）
- **ViewModelState** - ViewModelの内部状態（Mutable、StatefulBaseViewModelのみ）
- **UiEffect** - ナビゲーション・Toastなど一度だけUIが実行する副作用

### データフロー図

```
UI → UiAction → ViewModel → ViewModelState更新
                               ↓
                          toState()で変換
                               ↓
                            UiState → UI再描画

ViewModel → UiEffect → UI（副作用実行）
```

## BaseViewModelの種類

画面の特性に応じて2種類のBaseViewModelを使い分けます。

### 1. StatefulBaseViewModel（状態管理が必要な画面用）

状態管理が必要な画面で使用します。ViewModelの内部状態（ViewModelState）とUIに公開される状態（UiState）を分離することで、ViewModelの実装詳細をUIから隠蔽します。

#### 定義

```kotlin
interface UiAction
interface UiEffect
interface UiState

interface ViewModelState<S : UiState> {
    fun toState(): S
}

abstract class StatefulBaseViewModel<VS : ViewModelState<S>, S : UiState, A : UiAction, E : UiEffect> : ViewModel() {

    protected val _viewModelState = MutableStateFlow<VS>(createInitialViewModelState())
    val uiState: StateFlow<S> = _viewModelState
        .map(ViewModelState<S>::toState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = createInitialUiState(),
        )

    protected val _uiEffect = Channel<E>(Channel.BUFFERED)
    val uiEffect: Flow<E> = _uiEffect.receiveAsFlow()

    protected abstract fun createInitialViewModelState(): VS
    protected abstract fun createInitialUiState(): S

    abstract fun onUiAction(uiAction: A)

    protected fun updateViewModelState(update: VS.() -> VS) {
        _viewModelState.update { update(it) }
    }

    protected fun sendUiEffect(uiEffect: E) {
        _uiEffect.trySend(uiEffect)
    }

    // ローディング時間を最小値以上に保つヘルパー
    protected suspend fun ensureMinimumLoadingTime(
        startMark: TimeSource.Monotonic.ValueTimeMark,
        minimumLoadingTime: Duration = 500.milliseconds
    ) {
        val elapsed = startMark.elapsedNow()
        if (elapsed < minimumLoadingTime) {
            delay(minimumLoadingTime - elapsed)
        }
    }
}
```

#### 特徴

- **内部状態と公開状態の分離**: `ViewModelState`（内部）と`UiState`（公開）を分けることで、実装詳細を隠蔽
- **toState()による変換**: `ViewModelState`から`UiState`への変換ロジックを集約
- **最小ローディング時間**: UX向上のため、ローディング表示の最小時間を保証

### 2. StatelessBaseViewModel（状態管理が不要な画面用）

状態管理が不要で、副作用（ナビゲーション等）のみを扱う画面で使用します。

#### 定義

```kotlin
abstract class StatelessBaseViewModel<A : UiAction, E : UiEffect> : ViewModel() {

    protected val _uiEffect = Channel<E>(Channel.BUFFERED)
    val uiEffect: Flow<E> = _uiEffect.receiveAsFlow()

    abstract fun onUiAction(uiAction: A)

    protected fun sendUiEffect(uiEffect: E) {
        _uiEffect.trySend(uiEffect)
    }
}
```

#### 特徴

- **状態管理なし**: `UiState`を持たず、アクションとエフェクトのみ
- **軽量**: 単純な画面遷移のみを行うシンプルな実装

## 実装例：:feature:home

:feature:home（ニュース一覧画面）を例に、各要素の実装を説明します。

### ViewModelStateとUiState

#### ViewModelState（内部状態）

ViewModelの実装詳細を含む内部状態です。`statusType`で画面の状態（STABLE/ERROR）を管理し、`toState()`メソッドでUiStateに変換します。

```kotlin
data class HomeViewModelState(
    val statusType: StatusType = StatusType.STABLE,
    val isLoading: Boolean = false,
    val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
    val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    val error: NewsflowError? = null,
) : ViewModelState<HomeUiState> {

    enum class StatusType { STABLE, ERROR }

    override fun toState(): HomeUiState = when (statusType) {
        StatusType.STABLE -> HomeUiState.Stable(
            isLoading = isLoading,
            currentNewsCategory = currentNewsCategory,
            articlesByCategory = articlesByCategory
        )
        StatusType.ERROR -> HomeUiState.Error(
            error = requireNotNull(error) { "Error must not be null when statusType is ERROR" }
        )
    }
}
```

**ポイント**:
- `statusType`で状態パターンを管理
- `toState()`で状態に応じた`UiState`を生成
- エラー状態では`error`の非null性を保証

#### UiState（UI公開状態）

UIに公開される状態です。sealed interfaceで定義し、画面の状態に応じた分岐を行います。

```kotlin
sealed interface HomeUiState : UiState {
    data class Stable(
        val isLoading: Boolean = false,
        val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
        val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    ) : HomeUiState

    data class Error(
        val error: NewsflowError,
    ) : HomeUiState
}
```

**ポイント**:
- sealed interfaceで状態パターンを定義
- 各状態が必要なデータのみを持つ
- イミュータブルなデータクラス

### UiAction

ユーザー操作を表現するインターフェースです。各操作を型安全に表現します。

```kotlin
sealed interface HomeUiAction : UiAction {
    data class OnClickArticleCard(val article: Article) : HomeUiAction

    data class OnSwipNewsCategoryPage(val newsCategory: NewsCategory) : HomeUiAction

    data class OnClickNewsCategoryTag(val newsCategory: NewsCategory) : HomeUiAction

    data object OnClickRetryButton : HomeUiAction
}
```

**ポイント**:
- 各ユーザー操作を個別の型として定義
- 必要なデータをプロパティとして持つ
- パラメータ不要な操作は`data object`

### UiEffect

一度だけ実行される副作用を表現します。

```kotlin
sealed interface HomeUiEffect : UiEffect {
    data class NavigateViewer(val id: String) : HomeUiEffect
}
```

**ポイント**:
- ナビゲーションなど、一度だけ実行される処理
- UIが購読して実行する
- 状態ではなくイベントとして扱う

### ViewModel実装

各要素を統合したViewModelの実装例です。

```kotlin
class HomeViewModel(
    private val fetchTopHeadlineArticlesUseCase: FetchTopHeadlineArticlesUseCase
) : StatefulBaseViewModel<HomeViewModelState, HomeUiState, HomeUiAction, HomeUiEffect>() {

    override fun createInitialViewModelState(): HomeViewModelState = HomeViewModelState()
    override fun createInitialUiState(): HomeUiState = HomeUiState.Stable()

    init {
        // 初期カテゴリの記事を取得
        fetchArticles(_viewModelState.value.currentNewsCategory)
    }

    override fun onUiAction(uiAction: HomeUiAction) {
        when (uiAction) {
            is HomeUiAction.OnClickArticleCard -> {
                sendUiEffect(HomeUiEffect.NavigateViewer(uiAction.article.id))
            }

            is HomeUiAction.OnSwipNewsCategoryPage -> {
                changeNewsCategory(uiAction.newsCategory)
            }

            is HomeUiAction.OnClickNewsCategoryTag -> {
                changeNewsCategory(uiAction.newsCategory)
            }

            is HomeUiAction.OnClickRetryButton -> {
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
            val startMark = TimeSource.Monotonic.markNow()

            fetchTopHeadlineArticlesUseCase.invoke(category.value)
                .onSuccess { data ->
                    handleFetchTopHeadlineArticlesSuccess(category, data, startMark)
                }
                .onFailure { error ->
                    handleFetchTopHeadlineArticlesError(error, startMark)
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
        startMark: TimeSource.Monotonic.ValueTimeMark
    ) {
        ensureMinimumLoadingTime(startMark)
        updateViewModelState {
            copy(
                isLoading = false,
                articlesByCategory = articlesByCategory + (category to data)
            )
        }
    }

    private suspend fun handleFetchTopHeadlineArticlesError(
        error: Throwable,
        startMark: TimeSource.Monotonic.ValueTimeMark
    ) {
        Logger.e("HomeViewModel", "Failed to fetch articles: ${error.message}", error)
        ensureMinimumLoadingTime(startMark)
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

1. **onUiAction()での一元管理**: 全てのユーザー操作を一箇所で処理
2. **カテゴリ別キャッシング**: `articlesByCategory`で取得済みデータを保持
3. **最小ローディング時間**: `ensureMinimumLoadingTime()`でUX向上
4. **エラーハンドリング**: 型安全な`NewsflowError`での処理

### Koin DI設定

ViewModelはKoinで依存性注入されます。

```kotlin
val homeModule = module {
    viewModelOf(::HomeViewModel)
}
```

## データフロー詳細

実際のユーザー操作からUI更新までの流れを説明します。

### 例：記事カードをクリックして詳細画面へ遷移

1. **ユーザー操作**: 記事カードをクリック
2. **UiActionの発行**: `onAction(HomeUiAction.OnClickArticleCard(article))`が実行される
3. **ViewModelでの処理**:
   ```kotlin
   override fun onUiAction(uiAction: HomeUiAction) {
       when (uiAction) {
           is HomeUiAction.OnClickArticleCard -> {
               sendUiEffect(HomeUiEffect.NavigateViewer(uiAction.article.id))
           }
           // ...
       }
   }
   ```
4. **UiEffectの送信**: `NavigateViewer`がChannelに送信される
5. **UIでの購読と実行**: UIが`uiEffect`を購読し、ナビゲーション処理を実行

### 例：カテゴリ切り替えで記事を取得

1. **ユーザー操作**: カテゴリタグをクリック
2. **UiActionの発行**: `onAction(HomeUiAction.OnClickNewsCategoryTag(newsCategory))`
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
   - `toState()`が呼ばれ、新しい`UiState`が生成される
5. **UI再描画**: `StateFlow<UiState>`の変更をUIが検知し、再描画

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
   - `toState()`で`UiState.Error`が生成される

## 命名規約

アーキテクチャ内の各要素には統一された命名規則があります。

### UiAction

**パターン**: `On + 動作 + 対象`

**例**:
- `OnClickArticleCard` - 記事カードクリック
- `OnSwipNewsCategoryPage` - カテゴリページスワイプ
- `OnClickNewsCategoryTag` - カテゴリタグクリック
- `OnClickRetryButton` - リトライボタンクリック

### UiEffect

**パターン**: `動詞（命令形） + 目的語`

**例**:
- `NavigateViewer` - Viewer画面へ遷移
- `ShowToast` - Toastを表示（一般的な例）
- `NavigateBack` - 前画面へ戻る（一般的な例）

### UiState

**パターン**: `名詞` または `形容詞`

**例**:
- `Stable` - 安定状態
- `Error` - エラー状態
- プロパティ例: `isLoading`, `currentNewsCategory`, `articlesByCategory`

### ViewModelState

**パターン**: `UiState`と同様だが、実装詳細を含む

**例**:
- `statusType` - 内部状態タイプ
- `error` - エラー情報（nullable）

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
- UiState: UI表示に必要な情報のみ
- ViewModelState: 実装詳細の隠蔽

### 4. 型安全性
- sealed interfaceによる状態パターン
- Kotlinの型システムを最大限活用
- コンパイル時エラー検出

### 5. スケーラビリティ
- 新しい状態・アクション・エフェクトの追加が容易
- モジュール間の依存関係が明確
- Convention Pluginによる一貫性

## ベストプラクティス

### ViewModelState設計
- `toState()`で状態変換ロジックを集約
- 必須エラー情報は`requireNotNull()`で保証
- enum classで状態パターンを管理

### UiAction処理
- `onUiAction()`ですべての操作を一元管理
- 複雑な処理は別メソッドに分離
- 副作用は`sendUiEffect()`で送信

### 非同期処理
- `viewModelScope.launch`で起動
- `ensureMinimumLoadingTime()`でUX向上
- エラーハンドリングを忘れずに

### キャッシング
- 適切な粒度でデータを保持
- メモリ効率を考慮
- エラー時のキャッシュクリアを検討

### ログ出力
- `core:logger`の`Logger`を使用
- エラー時は必ずログ出力
- プラットフォーム非依存