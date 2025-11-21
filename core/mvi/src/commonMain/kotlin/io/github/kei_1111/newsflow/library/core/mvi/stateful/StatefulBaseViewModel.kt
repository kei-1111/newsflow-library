package io.github.kei_1111.newsflow.library.core.mvi.stateful

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.core.mvi.UiAction
import io.github.kei_1111.newsflow.library.core.mvi.UiEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@Suppress("VariableNaming")
abstract class StatefulBaseViewModel<VS : ViewModelState<S>, S : UiState, A : UiAction, E : UiEffect> : ViewModel() {

    protected val _viewModelState = MutableStateFlow<VS>(createInitialViewModelState())
    val uiState: StateFlow<S> = _viewModelState
        .map(ViewModelState<S>::toState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
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

    protected suspend fun ensureMinimumLoadingTime(
        startMark: TimeSource.Monotonic.ValueTimeMark,
        minimumLoadingTime: Duration = DEFAULT_MIN_LOADING_TIME
    ) {
        val elapsed = startMark.elapsedNow()
        if (elapsed < minimumLoadingTime) {
            delay(minimumLoadingTime - elapsed)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        val DEFAULT_MIN_LOADING_TIME = 500.milliseconds
    }
}
