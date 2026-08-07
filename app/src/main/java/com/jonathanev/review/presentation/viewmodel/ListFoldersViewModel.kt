package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.ClearGuideMoveUseCase
import com.jonathanev.review.domain.DeleteFolderUseCase
import com.jonathanev.review.domain.GetFolderPosicionUseCase
import com.jonathanev.review.domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.domain.GetGuideContextUseCase
import com.jonathanev.review.domain.NextNavigationUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.result.FolderResultDomain
import com.jonathanev.review.presentation.event.FolderActionEvent
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.FileInteractionMode
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
    private val resetNavigationUseCase: ResetNavigationUseCase,
    private val getGuideContextUseCase: GetGuideContextUseCase,
    private val clearGuideMoveUseCase: ClearGuideMoveUseCase
) : ViewModel() {
    val uiState: StateFlow<FoldersUiState> = getFoldersWithNumGuidesUseCase.invoke()
        .map { list ->
            if (list.isEmpty()) FoldersUiState.Empty
            else FoldersUiState.Success(list.map { it.toUi() })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FoldersUiState.Loading
        )
    private val _dialogState =
        MutableStateFlow<ActionDialogState<FolderUiModel>>(ActionDialogState.Hidden)
    val dialogState: StateFlow<ActionDialogState<FolderUiModel>> = _dialogState.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<FolderActionEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    val interactionMode: StateFlow<FileInteractionMode> = getGuideContextUseCase()
        .map { activeMoving ->
            if (activeMoving is GuideContext.Moving) FileInteractionMode.MovingItem else FileInteractionMode.Default
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FileInteractionMode.Default
        )

    fun onCancelMove() {
        viewModelScope.launch {
            clearGuideMoveUseCase.invoke()
        }
    }

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
            onDismissDialog()
            val name = item.folder.name
            nextNavigationUseCase.invoke(name)
            _eventsMessages.emit(FolderActionEvent.OpenFolder)
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
            resetNavigationUseCase.invoke()
            if (message) {
                _eventsMessages.emit(FolderActionEvent.DeleteFolderSuccess)
            } else {
                FolderActionEvent.ShowMessage("No se pudo borrar la carpeta correctamente")
            }
        }
    }

    fun onDismissDialog() {
        _dialogState.value = ActionDialogState.Hidden
    }

    fun onRequestDelete(guide: FolderUiModel) {
        _dialogState.value = ActionDialogState.ConfirmDelete(guide)
    }
}