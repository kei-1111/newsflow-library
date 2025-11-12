package io.github.kei_1111.newsflow.library.stateful

interface ViewModelState<S : UiState> {
    fun toState(): S
}
