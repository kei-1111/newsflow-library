# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Guidelines

This document defines the project's rules, objectives, and progress management methods. Please proceed with the project according to the following content.

### Top-Level Rules

- To maximize efficiency, **if you need to execute multiple independent processes, invoke those tools concurrently, not sequentially**.
- **You must think exclusively in English**. However, you are required to **respond in Japanese**.

## プロジェクト概要

newsflow-libraryは、Android・iOS向けのKotlin Multiplatform (KMP)ニュース配信ライブラリです。Clean Architectureの原則に基づき、厳格なモジュール分離とMVIパターンを採用しています。

## ビルドコマンド

### テストの実行

```bash
# 全モジュールのテストを実行
./gradlew allTests

# 特定モジュールのテストを実行
./gradlew :core:domain:allTests
./gradlew :feature:home:allTests

# クリーン＆ビルド
./gradlew clean build
```

### コード品質チェック

```bash
# 全モジュールでdetekt静的解析を実行
./gradlew detekt
```

### XCFrameworkのビルド（iOS向け）

```bash
# 特定モジュールのXCFrameworkをビルド
./gradlew assembleSharedXCFramework
./gradlew assembleHomeXCFramework

# 全XCFrameworkをビルド
./gradlew assemble
```

## アーキテクチャ概要

### モジュール構造

レイヤードモジュラーアーキテクチャで、明確な依存関係フローを持ちます：

```
基盤層（他のプロジェクトモジュールに依存しない）:
- core:model（ドメインエンティティ）
- core:network（HTTPクライアント、APIサービス）

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
- core:mvi（ViewModel用MVIフレームワーク）
- core:logger（プラットフォーム非依存のログ）
- core:test（テストユーティリティとFake実装）
- shared（Koin DI集約＆iOSエクスポート）
```

### コアモジュール

- **core:model**: ドメインエンティティ（`Article`、`NewsCategory`、`NewsflowError`）。依存なし。
- **core:network**: KtorベースのHTTPクライアント（プラットフォーム別エンジン：OkHttp/Darwin）。`NewsApiService`とBuildKonfigによるAPI設定を含む。
- **core:data**: `NewsRepository`の実装。Mutexを使ったスレッドセーフなインメモリキャッシング。DTO→ドメインモデル変換のマッパーを含む。
- **core:domain**: リポジトリに委譲する薄いユースケースラッパー（`FetchTopHeadlineArticlesUseCase`、`GetArticleByIdUseCase`）。
- **core:mvi**: ステートフル/ステートレスViewModel用ベースクラス。単方向データフロー。内部`ViewModelState`と公開`UiState`を分離。androidx-lifecycle-viewmodelに依存。
- **core:logger**: プラットフォーム固有ログのexpect/actual実装。
- **core:test**: テスト用Fake実装（`FakeNewsRepository`、`FakeNewsApiService`、`FakeFetchTopHeadlineArticlesUseCase`）とテストデータビルダー。

### フィーチャーモジュール

フィーチャーモジュールはMVIパターンに従います：
- **ViewModel**: `StatefulBaseViewModel<ViewModelState, UiState, UiAction, UiEffect>`を継承
- **UiState**: UI公開用のイミュータブルなステート（sealed interface）
- **ViewModelState**: 内部ミュータブルなステート（data class）
- **UiAction**: ユーザーインタラクション
- **UiEffect**: 一度きりの副作用（ナビゲーション、トーストなど）をChannelで実装

現在のフィーチャー:
- **feature:home**: カテゴリ別ニュースフィード。カテゴリごとの記事キャッシング。
- **feature:viewer**: 記事詳細表示。SavedStateHandleでナビゲーション引数を受け取る。

### Sharedモジュール

`shared`モジュールは統合ポイントとして機能：
- 全Koinモジュールを集約（networkModule、dataModule、domainModule、各フィーチャーモジュール）
- プラットフォーム固有のコンテキスト処理を含む`initKoin()`を提供（expect/actual）
- `api()`依存でフィーチャーモジュールをiOS向けにXCFrameworkとしてエクスポート

## 依存性注入（Koin）

全依存関係はKoinでモジュラー管理：

```kotlin
// 各レイヤーが独自のモジュールを持つ
networkModule: HttpClient (singleton)、NewsApiService (singleton)
dataModule: NewsRepository (singleton)
domainModule: ユースケース (singleton)
homeModule: HomeViewModel (viewModel scope)
viewerModule: ViewerViewModel (viewModel scope)
```

**パターン**: `singleOf(::Implementation) bind Interface::class`でインターフェースバインディング。実装クラスは`internal`、インターフェースは`public`。

**初期化**: プラットフォーム固有で`shared.initKoin()`経由 - AndroidのApplicationクラスまたはiOSアプリエントリーポイントから呼び出す。

## テスト戦略

### テストファイルの配置場所
全テストは`src/commonTest/kotlin/`に配置してプラットフォーム非依存実行。

### テストパターン

**ViewModelテスト**:
```kotlin
// core:testのFakeユースケースを使用
val fakeUseCase = FakeFetchTopHeadlineArticlesUseCase()
fakeUseCase.setResult(Result.success(testArticles))

// TurbineでFlowアサーション
viewModel.uiState.test {
    val state = awaitItem()
    assertIs<HomeUiState.Stable>(state)
}

// StandardTestDispatcherでコルーチン制御
@BeforeTest
fun setup() {
    Dispatchers.setMain(StandardTestDispatcher())
}
```

**Repository/UseCaseテスト**:
- `core:test`のFake実装を使用
- キャッシング動作とエラーハンドリングを検証

**Networkテスト**:
- Ktor MockEngineでHTTPモック

### テストユーティリティ（core:test）
- `FakeNewsApiService`: 制御可能なAPIレスポンス
- `FakeNewsRepository`: テスト用インメモリリポジトリ
- `FakeFetchTopHeadlineArticlesUseCase`: 設定可能な結果を返すユースケース
- `createTestArticle()`、`createTestArticles(n)`: テストデータビルダー

## Convention Plugins

ビルド設定は`build-logic/convention`で一元管理：

1. **KmpLibraryConventionPlugin** (`newsflow.library.kmp.library`): coreモジュール・sharedモジュールに適用
   - Android/iOSターゲット設定（iosX64、iosArm64、iosSimulatorArm64）
   - XCFramework生成設定（staticリンク）
   - DetektとMaven publishingを自動適用
   - JVM 21ターゲット

2. **KmpFeatureConventionPlugin** (`newsflow.library.kmp.feature`): フィーチャーモジュールに適用
   - KmpLibraryConventionPluginを拡張
   - 自動インクルード: core:mvi、core:model（API）、core:domain、core:logger（implementation）
   - 自動インクルード: Koin依存（koin-core、koin-compose-viewmodel）
   - テスト依存: core:test、koin-test、kotlin-test、kotlinx-coroutines-test、turbine

3. **DetektConventionPlugin** (`newsflow.library.detekt`): 静的解析設定

4. **MavenPublishConventionPlugin** (`newsflow.library.maven.publish`): Maven公開設定

新規モジュール作成時：
- coreモジュール: `alias(libs.plugins.newsflow.library.kmp.library)`を適用
- フィーチャーモジュール: `alias(libs.plugins.newsflow.library.kmp.feature)`を適用（標準依存が自動インクルード）

## 開発ガイドライン

### モジュール依存関係
- フィーチャーモジュール間の依存は**絶対に作らない**
- coreモジュールは厳格なレイヤリング:
  - 基盤層: core:model、core:network（プロジェクト依存なし）
  - データ層: core:data → core:model, core:network
  - ドメイン層: core:domain → core:data, core:model
- フィーチャーはcore:domain、core:model、core:mvi、core:loggerのみに依存

### 新規フィーチャーの追加
1. `feature/`ディレクトリ下にモジュール作成
2. build.gradle.ktsで`alias(libs.plugins.newsflow.library.kmp.feature)`を適用
3. UiState/UiAction/UiEffectを持つStatefulBaseViewModel継承ViewModelを作成
4. `viewModelOf(::YourViewModel)`でKoinモジュール作成
5. `shared`モジュールのKoin初期化に追加（Android/iOS両方）

### MVI実装
- `ViewModelState`インターフェースを実装したdata classを定義（`toState()`メソッドで`UiState`を導出）
- 内部ステート更新は`updateViewModelState { copy(...) }`を使用
- ナビゲーションや一度きりのイベントには`sendUiEffect()`を使用
- ローディング状態はUX一貫性のため最小表示時間を使用（`ensureMinimumLoadingTime()`）

### テスト要件
- `src/commonTest/kotlin/`にテストを記述してプラットフォーム非依存実行
- 分離のため`core:test`のFake実装を使用
- StateFlow/FlowテストにはTurbineを使用
- @BeforeTestで`Dispatchers.setMain(StandardTestDispatcher())`を設定
- 成功パスとエラーパス両方を検証

### キャッシング戦略
リポジトリはスレッドセーフな`Mutex`付きインメモリキャッシングを使用：
- キャッシュは`Map<String, List<Article>>`（カテゴリ文字列をキーとして使用）
- エラー時にキャッシュを無効化（該当カテゴリのみ）
- キャッシュバイパスには`forceRefresh = true`を使用

### エラーハンドリング
型付きエラーには`NewsflowError` sealed classを使用：
- `Unauthorized`: APIキー無効
- `RateLimitExceeded`: レート制限超過
- `BadRequest`: 不正なリクエスト
- `ServerError`: サーバーエラー
- `NetworkFailure`: ネットワーク接続失敗
- `ArticleNotFound`: 記事が見つからない
- `InvalidParameter`: パラメータ不正

data/domainレイヤーからは標準Kotlin `Result<T>`を返す（失敗時は`NewsflowError`をラップ）。

## 共通パターン

### ユースケース実装
```kotlin
internal class FetchArticlesUseCaseImpl(
    private val repository: NewsRepository
) : FetchArticlesUseCase {
    override suspend fun invoke(category: String, forceRefresh: Boolean): Result<List<Article>> =
        repository.fetchArticles(category = category, forceRefresh = forceRefresh)
}
```

### Koinモジュール定義
```kotlin
val myModule = module {
    singleOf(::MyRepositoryImpl) bind MyRepository::class
    singleOf(::MyUseCaseImpl) bind MyUseCase::class
}
```

### ViewModelステート更新
```kotlin
// ViewModelState定義（toState()でUiStateを導出）
data class MyViewModelState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val error: NewsflowError? = null,
) : ViewModelState<MyUiState> {
    override fun toState(): MyUiState = when {
        error != null -> MyUiState.Error(error)
        else -> MyUiState.Stable(isLoading, articles)
    }
}

// 内部ステート更新
updateViewModelState {
    copy(articles = newArticles, isLoading = false)
}

// 一度きりのイベント送信
sendUiEffect(MyUiEffect.NavigateToDetail(articleId))
```

## API設定

### NewsAPI Key設定
`local.properties`に以下を追加：
```properties
NEWS_API_KEY=your_api_key_here
```
BuildKonfigが自動的にこの値を`core:network`モジュールに注入します。

## バージョン情報

主要バージョン（`gradle/libs.versions.toml`参照）：
- Kotlin: 2.2.20
- AGP: 8.11.1
- Koin: 4.1.1
- Ktor: 3.3.2
- Coroutines: 1.10.2
- Detekt: 1.23.8
- Turbine: 1.2.1
- AndroidX Lifecycle: 2.9.1
- Target SDK: 36、Min SDK: 29
- JVM Target: 21
- ライブラリバージョン: 0.2.0
