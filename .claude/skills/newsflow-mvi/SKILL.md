---
name: newsflow-mvi
description: newsflow-libraryのMVIパターン実装規約を適用します。ViewModel、UiState、UiAction、UiEffect、ViewModelStateファイルの作成・編集時に使用してください。
---

# Newsflow MVI Rules

## 必須ファイル構成（feature毎）

```
feature/{name}/src/commonMain/.../feature/{name}/
├── {Name}ViewModel.kt
├── {Name}ViewModelState.kt
├── {Name}UiState.kt
├── {Name}UiAction.kt
├── {Name}UiEffect.kt
└── di/{Name}Module.kt
```

## データフロー

```
UI → UiAction → ViewModel.onUiAction() → updateViewModelState { copy(...) }
                                                    ↓
                                            ViewModelState.toState()
                                                    ↓
                                                 UiState → UI

ViewModel → sendUiEffect() → UiEffect → UI（一度きりの処理）
```

## 実装チェックリスト

### UiState作成時
- [ ] `sealed interface {Name}UiState : UiState` で定義
- [ ] `Stable` と `Error` の2状態を最低限用意
- [ ] Errorは `error: NewsflowError` を持つ

### ViewModelState作成時
- [ ] `data class` + `ViewModelState<{Name}UiState>` 実装
- [ ] `StatusType` enumで状態管理（STABLE, ERROR）
- [ ] `toState()` で UiState への変換ロジックを集約
- [ ] ERROR時は `requireNotNull(error)` で非null保証

### ViewModel作成時
- [ ] `StatefulBaseViewModel<VS, S, A, E>` 継承
- [ ] `createInitialViewModelState()` と `createInitialUiState()` 実装
- [ ] 全UiActionを `onUiAction()` のwhenで処理
- [ ] 非同期処理は `viewModelScope.launch` + `ensureMinimumLoadingTime()`
- [ ] エラーは `Logger.e()` でログ出力

## 重要パターン

```kotlin
// ステート更新（常にcopy使用）
updateViewModelState { copy(isLoading = true) }

// 一度きりのイベント（ナビゲーション等）
sendUiEffect({Name}UiEffect.NavigateToDetail(id))

// 最小ローディング時間（500msデフォルト）
val startMark = TimeSource.Monotonic.markNow()
// ... 処理 ...
ensureMinimumLoadingTime(startMark)
```

## 禁止パターン

- UiStateにナビゲーションフラグを持たせない（UiEffect使用）
- _viewModelState.value を直接変更しない（updateViewModelState使用）
- ViewModelでRepository直接使用しない（UseCase経由）