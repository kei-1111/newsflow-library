package io.github.kei_1111.newsflow.library.stateful

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.UiAction
import io.github.kei_1111.newsflow.library.UiEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Suppress("VariableNaming")
abstract class StatefulBaseViewModel<VS : ViewModelState<S>, S : UiState, A : UiAction, E : UiEffect> : ViewModel() {

    protected val _viewModelState = MutableStateFlow<VS>(createInitialViewModelState())
    val state: StateFlow<S> = _viewModelState
        .map(ViewModelState<S>::toState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = createInitialUiState(),
        )

    protected val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    protected abstract fun createInitialViewModelState(): VS
    protected abstract fun createInitialUiState(): S

    abstract fun onAction(action: A)

    protected fun updateViewModelState(update: VS.() -> VS) {
        _viewModelState.update { update(it) }
    }

    protected fun sendEffect(effect: E) {
        _effect.trySend(effect)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
