package io.github.kei_1111.newsflow.library.core.mvi.stateless

import androidx.lifecycle.ViewModel
import io.github.kei_1111.newsflow.library.core.mvi.UiAction
import io.github.kei_1111.newsflow.library.core.mvi.UiEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("VariableNaming")
abstract class StatelessBaseViewModel<A : UiAction, E : UiEffect> : ViewModel() {

    protected val _uiEffect = Channel<E>(Channel.BUFFERED)
    val uiEffect: Flow<E> = _uiEffect.receiveAsFlow()

    abstract fun onUiAction(uiAction: A)

    protected fun sendUiEffect(uiEffect: E) {
        _uiEffect.trySend(uiEffect)
    }
}
