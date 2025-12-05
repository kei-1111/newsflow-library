# 命名規則

## 参照ファイル

- 詳細ドキュメント: `docs/NAMING_CONVENTION.md`
- Intent例: `feature/home/src/commonMain/kotlin/.../HomeIntent.kt`
- Effect例: `feature/home/src/commonMain/kotlin/.../HomeEffect.kt`

## ケース規則

| 対象 | ケース | 例 |
|-----|-------|-----|
| クラス・インターフェース | PascalCase | `HomeViewModel`, `NewsRepository` |
| 関数・変数 | camelCase | `fetchArticles`, `isLoading` |
| 定数 | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT` |
| パッケージ | 小文字ドット区切り | `io.github.kei_1111.newsflow.library` |
| モジュール | ケバブケース | `core:model`, `feature:home` |

## MVI要素

| 種類 | パターン | 例 |
|-----|---------|-----|
| ViewModel | `{Feature}ViewModel` | `HomeViewModel` |
| State | `{Feature}State` | `HomeState` |
| ViewModelState | `{Feature}ViewModelState` | `HomeViewModelState` |
| Intent | `{Feature}Intent` | `HomeIntent` |
| Effect | `{Feature}Effect` | `HomeEffect` |

## レイヤー別

| 種類 | パターン | 例 |
|-----|---------|-----|
| UseCase (Interface) | `{動詞}{対象}UseCase` | `FetchTopHeadlineArticlesUseCase` |
| UseCase (Impl) | `{動詞}{対象}UseCaseImpl` | `FetchTopHeadlineArticlesUseCaseImpl` |
| Repository (Interface) | `{Domain}Repository` | `NewsRepository` |
| Repository (Impl) | `{Domain}RepositoryImpl` | `NewsRepositoryImpl` |
| テストクラス | `{TargetClass}Test` | `HomeViewModelTest` |

## Intent命名（重要）

**基本パターン**: `動詞 + 対象`（意図ベース＝「何をしたいか」）

```kotlin
// Good: 意図ベース（何をしたいか）
NavigateViewer, ChangeCategory, RetryLoad, ShareArticle, RefreshArticles

// Bad: 操作ベース（禁止）
OnClickArticleCard, OnSwipeCategory, OnLongPressArticle

// Bad: 選択ベース（何をしたいかではない）
SelectArticle, Refresh
```

**理由**:
- 操作ベースだと同じ処理を行う異なる操作（スワイプ/タップ）で重複が発生
- `SelectArticle`より`NavigateViewer`の方が「ビューアに遷移したい」という意図が明確

### 状態更新系Intent

**パターン**: `Update{対象}`

```kotlin
UpdateQuery, UpdateSortBy, UpdateDateRange, UpdateLanguage
```

### UI表示制御系Intent

**パターン**: `Show{対象}` / `Dismiss{対象}`

```kotlin
ShowArticleOverview, DismissArticleOverview, ShowOptionsSheet, DismissOptionsSheet
```

### ナビゲーション系Intent

**パターン**: `Navigate{目的地}` / `NavigateBack`

```kotlin
NavigateSearch, NavigateBack
```

## Effect命名

**パターン**: `動詞（命令形） + 目的語`

```kotlin
NavigateViewer, CopyUrl, ShareArticle, ShowToast
```

**よく使う動詞**: Navigate, Show, Hide, Open, Copy

## UseCase動詞選択

| 動詞 | 用途 |
|-----|------|
| `Fetch` | リモートからデータ取得 |
| `Get` | ローカル/キャッシュから取得 |
| `Save` | データ保存 |
| `Delete` | データ削除 |
| `Update` | データ更新 |
| `Search` | 検索 |

## テストメソッド命名

**パターン**: バッククォート + 説明文

```kotlin
`should return success when articles are fetched successfully`
`should return error when network fails`
```