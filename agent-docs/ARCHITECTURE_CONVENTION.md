# アーキテクチャ詳細

## モジュール依存関係フロー

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
└── shared（Koin DI集約＆iOSエクスポート）
```

## コアモジュール詳細

| モジュール | 役割 | 主要クラス |
|-----------|------|-----------|
| core:model | ドメインエンティティ | `Article`, `NewsCategory`, `NewsflowError` |
| core:network | HTTPクライアント | `NewsApiService`, `HttpClient` |
| core:data | リポジトリ実装 | `NewsRepositoryImpl`, Mappers |
| core:domain | ユースケース | `FetchTopHeadlineArticlesUseCase`, `GetArticleByIdUseCase` |
| core:mvi | MVIフレームワーク | `StatefulBaseViewModel`, `StatelessBaseViewModel` |
| core:logger | ログ出力 | expect/actual実装 |

## フィーチャーモジュール

| モジュール | 役割 |
|-----------|------|
| feature:home | カテゴリ別ニュースフィード表示 |
| feature:search | 記事検索機能 |
| feature:viewer | 記事詳細表示（SavedStateHandle経由で引数受け取り） |

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

エラー型定義: `core/model/src/commonMain/kotlin/.../NewsflowError.kt:3`

```kotlin
sealed class NewsflowError(message: String) : Exception(message) {
    sealed class NetworkError(message: String) : NewsflowError(message) {
        data class Unauthorized(...)
        data class RateLimitExceeded(...)
        data class BadRequest(...)
        data class ServerError(...)
        data class NetworkFailure(...)
    }

    sealed class InternalError(message: String) : NewsflowError(message) {
        data class ArticleNotFound(...)
        data class InvalidParameter(...)
    }
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