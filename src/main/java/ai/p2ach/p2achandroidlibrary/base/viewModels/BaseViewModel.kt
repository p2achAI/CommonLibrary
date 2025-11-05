package ai.p2ach.p2achandroidlibrary.base.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.p2ach.p2achandroidlibrary.base.repos.BaseRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Empty : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val throwable: Throwable) : UiState<Nothing>
}

abstract class BaseViewModel<T> : ViewModel() {
    protected val _state = MutableStateFlow<UiState<T>>(UiState.Empty)
    val state: StateFlow<UiState<T>> = _state

    protected fun bind(source: Flow<T>) {
        viewModelScope.launch {
            source
                .onStart { _state.value = UiState.Loading }
                .distinctUntilChanged()
                .catch { _state.value = UiState.Error(it) }
                .collect { _state.value = UiState.Success(it) }
        }
    }

    protected fun load(block: suspend () -> BaseRepo.ApiResult<T>) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val r = block()) {
                is BaseRepo.ApiResult.Success -> _state.value = UiState.Success(r.data)
                is BaseRepo.ApiResult.NetworkError -> _state.value = UiState.Error(r.cause)
                is BaseRepo.ApiResult.HttpError -> _state.value = UiState.Error(IllegalStateException("HTTP ${r.code}"))
                is BaseRepo.ApiResult.UnknownError -> _state.value = UiState.Error(r.throwable)
            }
        }
    }
}