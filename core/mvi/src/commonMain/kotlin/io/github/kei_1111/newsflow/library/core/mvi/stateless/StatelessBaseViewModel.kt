package io.github.kei_1111.newsflow.library.core.mvi.stateless

import androidx.lifecycle.ViewModel
import io.github.kei_1111.newsflow.library.core.mvi.Effect
import io.github.kei_1111.newsflow.library.core.mvi.Intent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("VariableNaming")
abstract class StatelessBaseViewModel<I : Intent, E : Effect> : ViewModel() {

    protected val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    abstract fun onIntent(intent: I)

    protected fun sendEffect(effect: E) {
        _effect.trySend(effect)
    }
}