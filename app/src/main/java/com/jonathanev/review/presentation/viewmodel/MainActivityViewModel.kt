package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.domain.InitializeGuideStorageUseCase
import com.jonathanev.review.domain.NextNavigationUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.presentation.event.UIMainEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.state.CreateFoldersState
import com.jonathanev.review.presentation.state.FoldersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val initializeGuideStorageUseCase: InitializeGuideStorageUseCase,
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase,
    private val resetNavigationUseCase: ResetNavigationUseCase,
    private val nextNavigationUseCase: NextNavigationUseCase
) : ViewModel() {
    val uiState: StateFlow<FoldersUiState> = getFoldersWithNumGuidesUseCase.invoke()
        //.take(1)
        .map { list ->
            if (list.isEmpty()) FoldersUiState.Empty
            else {
                val foldersUiModel = list.map { it.toUi() }
                FoldersUiState.Success(foldersUiModel)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FoldersUiState.Loading
        )

    private val _foldersState = MutableStateFlow<CreateFoldersState>(CreateFoldersState.Idle)
    val foldersState: StateFlow<CreateFoldersState> = _foldersState.asStateFlow()

    private val _eventsMain = MutableSharedFlow<UIMainEvent>()
    val eventsMain = _eventsMain.asSharedFlow()

    fun createFolders() {
        viewModelScope.launch {
            _foldersState.value = CreateFoldersState.Loading

            val isSuccess = initializeGuideStorageUseCase.invoke()
            if (isSuccess) {
                _foldersState.value = CreateFoldersState.Idle
            } else {
                _foldersState.value = CreateFoldersState.Error
            }
        }
    }

    fun onDismissErrorDialog() {
        _foldersState.value = CreateFoldersState.Idle
    }

    fun setMainPath() {
        viewModelScope.launch {
            try {
                resetNavigationUseCase.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                eventMain(e.message ?: "Ocurrió un error inesperado")
            }
        }
    }

    private fun eventMain(message: String) {
        viewModelScope.launch {
            _eventsMain.emit(UIMainEvent.ErrorMessage(message))
        }
    }


    fun next(folder: String) {
        viewModelScope.launch {
            try {
                nextNavigationUseCase.invoke(folder)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                eventMain(e.message ?: "Ocurrió un error inesperado")
            }
        }
    }
}