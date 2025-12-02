---
name: newsflow-mvi
description: newsflow-libraryのMVIパターン実装規約を適用します。ViewModel、State、Intent、Effect、ViewModelStateファイルの作成・編集時に使用してください。
---

# Newsflow MVI Rules

## 必須ファイル構成（feature毎）

```
feature/{name}/src/commonMain/.../feature/{name}/
├── {Name}ViewModel.kt
├── {Name}ViewModelState.kt
├── {Name}State.kt
├── {Name}Intent.kt
├── {Name}Effect.kt
└── di/{Name}Module.kt
```

## データフロー

```
UI → Intent → ViewModel.onIntent() → updateViewModelState { copy(...) }
                                                    ↓
                                            ViewModelState.toState()
                                                    ↓
                                                 State → UI

ViewModel → sendEffect() → Effect → UI（一度きりの処理）
```

## 実装チェックリスト

### State作成時
- [ ] `sealed interface {Name}State : State` で定義
- [ ] `Stable` と `Error` の2状態を最低限用意
- [ ] Errorは `error: NewsflowError` を持つ

### ViewModelState作成時
- [ ] `data class` + `ViewModelState<{Name}State>` 実装
- [ ] `StatusType` enumで状態管理（STABLE, ERROR）
- [ ] `toState()` で State への変換ロジックを集約
- [ ] ERROR時は `requireNotNull(error)` で非null保証

### ViewModel作成時
- [ ] `StatefulBaseViewModel<VS, S, I, E>` 継承
- [ ] `createInitialViewModelState()` と `createInitialState()` 実装
- [ ] 全Intentを `onIntent()` のwhenで処理
- [ ] 非同期処理は `viewModelScope.launch` + `ensureMinimumLoadingTime()`
- [ ] エラーは `Logger.e()` でログ出力

## 重要パターン

```kotlin
// ステート更新（常にcopy使用）
updateViewModelState { copy(isLoading = true) }

// 一度きりのイベント（ナビゲーション等）
sendEffect({Name}Effect.NavigateToDetail(id))

// 最小ローディング時間（500msデフォルト）
val startMark = TimeSource.Monotonic.markNow()
// ... 処理 ...
ensureMinimumLoadingTime(startMark)
```

## 禁止パターン

- Stateにナビゲーションフラグを持たせない（Effect使用）
- _viewModelState.value を直接変更しない（updateViewModelState使用）
- ViewModelでRepository直接使用しない（UseCase経由）