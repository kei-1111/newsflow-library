package io.github.kei_1111.newsflow.library.core.mvi.stateful

interface ViewModelState<S : UiState> {
    fun toState(): S
}
