# MVI実装パターン

## 参照ファイル

- 基底クラス: `core/mvi/src/commonMain/kotlin/.../StatefulBaseViewModel.kt:19`
- 実装例: `feature/home/src/commonMain/kotlin/.../HomeViewModel.kt:12`

## ファイル構成

```
feature/xxx/src/commonMain/kotlin/.../
├── XxxViewModel.kt       # ViewModel実装
├── XxxState.kt          # UI公開用ステート（sealed interface）
├── XxxIntent.kt         # ユーザーの意図
├── XxxEffect.kt         # 一度きりの副作用
├── XxxViewModelState.kt # 内部ステート（data class）
└── di/XxxModule.kt      # Koinモジュール
```

## 実装テンプレート

### ViewModelState定義

```kotlin
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
```

### ViewModel実装

```kotlin
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

## 主要ユーティリティ

```kotlin
// ViewModel内部ステート更新
updateViewModelState { copy(...) }

// 一度きりのイベント送信
sendEffect(MyEffect.Navigate(...))
```

## State vs ViewModelState

| 種類 | 役割 | 可変性 |
|------|------|--------|
| ViewModelState | 内部管理用、全プロパティ保持 | data class (copy可) |
| State | UI公開用、表示に必要な情報のみ | sealed interface (イミュータブル) |

`toState()`は純粋関数として実装し、副作用を含めないこと。