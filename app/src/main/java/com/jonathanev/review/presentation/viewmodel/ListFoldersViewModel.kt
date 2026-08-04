package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.DeleteFolderUseCase
import com.jonathanev.review.domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.domain.NextNavigationUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.presentation.event.FolderActionEvent
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.state.FoldersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListFoldersViewModel @Inject constructor(
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val nextNavigationUseCase: NextNavigationUseCase,
    private val resetNavigationUseCase: ResetNavigationUseCase
) : ViewModel() {
    val uiState: StateFlow<FoldersUiState> = getFoldersWithNumGuidesUseCase.invoke()
        .map { list ->
            if (list.isEmpty()) FoldersUiState.Empty
            else {
                val foldersUiModel = list.map { it.toUi() }
                FoldersUiState.Success(foldersUiModel)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoldersUiState.Loading
        )

    private val _eventsMessages = MutableSharedFlow<FolderActionEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    fun deleteFolderAndContent(nameFolder: String) {
        viewModelScope.launch {
            nextNavigationUseCase.invoke(nameFolder)
            val message = deleteFolderUseCase.invoke()
            _eventsMessages.emit(
                if (message) {
                    FolderActionEvent.DeleteFolderSuccess
                } else {
                    FolderActionEvent.ShowMessage("No se pudo borrar la carpeta correctamente")
                }
            )
            resetNavigationUseCase.invoke()
        }
    }

    /*fun moveFileCancel() {
        viewModelScope.launch {
            _eventsMovingFiles.emit(UIMovingEvent.ShowMessage("Se ha cancelado la acción"))
        }
    }*/

    fun navigateToDirectory(name: String) {
        viewModelScope.launch {
            nextNavigationUseCase.invoke(name)
        }
    }
}