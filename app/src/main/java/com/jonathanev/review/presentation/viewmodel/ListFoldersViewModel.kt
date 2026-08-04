package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.DeleteFolderUseCase
import com.jonathanev.review.domain.GetFolderPosicionUseCase
import com.jonathanev.review.domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.domain.NextNavigationUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.domain.result.FolderResultDomain
import com.jonathanev.review.presentation.event.FolderActionEvent
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.FolderResultUi
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.state.ActionDialogState
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

@HiltViewModel
class ListFoldersViewModel @Inject constructor(
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase,
    private val getFolderPosicionUseCase: GetFolderPosicionUseCase,
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
    private val _dialogState =
        MutableStateFlow<ActionDialogState<FolderUiModel>>(ActionDialogState.Hidden)
    val dialogState: StateFlow<ActionDialogState<FolderUiModel>> = _dialogState.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<FolderActionEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    fun getFolderSelected(
        folders: List<FolderUiModel>,
        position: Int,
        isDarkTheme: Boolean
    ): FolderResultUi {
        val foldersDomainModel = folders.map { it.toDomain(isDarkTheme) }

        return when (val result =
            getFolderPosicionUseCase.invoke(position, foldersDomainModel)) {
            is FolderResultDomain.Error -> result.toUi()
            is FolderResultDomain.Success -> result.toUi()
        }
    }

    /*fun moveFileCancel() {
        viewModelScope.launch {
            _eventsMovingFiles.emit(UIMovingEvent.ShowMessage("Se ha cancelado la acción"))
        }
    }*/

    fun navigateToDirectory(item: FolderUiModel) {
        viewModelScope.launch {
            val name = item.folder.name
            nextNavigationUseCase.invoke(name)
        }
    }

    fun onOpenMenu(folder: FolderUiModel) {
        _dialogState.value = ActionDialogState.OptionsMenu(folder)
    }

    fun onConfirmDelete(folder: FolderUiModel) {
        viewModelScope.launch {
            onDismissDialog()
            val nameFolder = folder.folder.name
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

    fun onDismissDialog() {
        _dialogState.value = ActionDialogState.Hidden
    }

    fun onRequestDelete(guide: FolderUiModel) {
        _dialogState.value = ActionDialogState.ConfirmDelete(guide)
    }
}