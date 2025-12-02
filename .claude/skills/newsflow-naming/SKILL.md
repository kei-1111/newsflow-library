---
name: newsflow-naming
description: newsflow-libraryの命名規則を適用します。クラス、インターフェース、関数、変数、ファイル、モジュール、パッケージの命名時に使用してください。Intent、Effect、State、UseCase、Repository、ViewModel等の新規作成・リネーム時に特に重要です。
---

# Newsflow Naming Rules

命名に迷った場合は `docs/NAMING_CONVENTION.md` を参照。

## クイックリファレンス

### ケース規則

| 対象 | ケース | 例 |
|-----|-------|-----|
| クラス・インターフェース | PascalCase | `HomeViewModel`, `NewsRepository` |
| 関数・変数 | camelCase | `fetchArticles`, `isLoading` |
| 定数 | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT` |
| パッケージ | 小文字ドット区切り | `io.github.kei_1111.newsflow.library` |
| モジュール | ケバブケース | `core:model`, `feature:home` |

### MVI要素

| 種類 | パターン | 例 |
|-----|---------|-----|
| ViewModel | `{Feature}ViewModel` | `HomeViewModel` |
| State | `{Feature}State` | `HomeState` |
| ViewModelState | `{Feature}ViewModelState` | `HomeViewModelState` |
| Intent | `{Feature}Intent` | `HomeIntent` |
| Effect | `{Feature}Effect` | `HomeEffect` |

### レイヤー別

| 種類 | パターン | 例 |
|-----|---------|-----|
| UseCase (Interface) | `{動詞}{対象}UseCase` | `FetchTopHeadlineArticlesUseCase` |
| UseCase (Impl) | `{動詞}{対象}UseCaseImpl` | `FetchTopHeadlineArticlesUseCaseImpl` |
| Repository (Interface) | `{Domain}Repository` | `NewsRepository` |
| Repository (Impl) | `{Domain}RepositoryImpl` | `NewsRepositoryImpl` |
| Fake | `Fake{Interface}` | `FakeNewsRepository` |

## Intent命名（重要）

**パターン**: `動詞 + 対象`

```kotlin
// ✅ Good: 意図ベース
NavigateViewer, ChangeCategory, RetryLoad, ShareArticle

// ❌ Bad: 操作ベース（禁止）
OnClickArticleCard, OnSwipeCategory, OnLongPressArticle
```

**理由**: 操作ベースだと同じ処理を行う異なる操作（スワイプ/タップ）で重複が発生。

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

## State プロパティ命名

```kotlin
// Boolean型
isLoading, isRefreshing, isEnabled

// 現在選択中
currentNewsCategory, currentUser

// Map型
articlesByCategory, usersByRole
```

## テストメソッド命名

**パターン**: バッククォート + `should {期待} when {条件}`

```kotlin
`should return success when articles are fetched successfully`
`should return error when network fails`
```

## 決定フローチャート

### 新規ファイル作成時

```
何を作る？
├─ ViewModel関連 → {Feature} + (ViewModel|State|Intent|Effect|ViewModelState)
├─ UseCase → {動詞}{対象}UseCase
├─ Repository → {Domain}Repository(Impl)
├─ テスト → {TargetClass}Test
└─ Fake → Fake{Interface}
```

### 関数命名時

```
何をする関数？
├─ データ取得 → fetch{Remote} / get{Local}
├─ 状態更新 → update{State} / set{Property}
├─ 検証 → validate{Target} / is{Condition}
└─ 変換 → to{Target} / map{Source}To{Target}
```