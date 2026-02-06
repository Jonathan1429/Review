package com.jonathanev.review.presentation.folders.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.DeleteFolderUseCase
import com.jonathanev.review.domain.GetFolderPosicionUseCase
import com.jonathanev.review.domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.presentation.event.FolderActionEvent
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.FolderResultUi
import com.jonathanev.review.presentation.state.FoldersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoldersListViewModel @Inject constructor(
    private val getFolderPosicionUseCase: GetFolderPosicionUseCase,
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
) : ViewModel() {
    private var _foldersUiState = MutableStateFlow(FoldersUiState())
    val foldersUiState = _foldersUiState.asStateFlow()

    private var cachedFolders: List<FolderDomainModel> = emptyList()

    private val _eventsMessages = MutableSharedFlow<FolderActionEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    fun getAllFolders() {
        viewModelScope.launch {
            // 1. marcar loading
            _foldersUiState.value = _foldersUiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val folderDomain =
                    getFoldersWithNumGuidesUseCase.invoke().sortedBy { it.folder.name }
                cachedFolders = folderDomain

                val folderUi = folderDomain.map { it.toUi() }
                // 2. actualizar con la lista resultante
                _foldersUiState.value = _foldersUiState.value.copy(
                    isLoading = false,
                    folders = folderUi
                )
            } catch (e: Exception) {
                _foldersUiState.value = _foldersUiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun getFolderSelected(position: Int): FolderResultUi {
        val folderResultDomain = getFolderPosicionUseCase.invoke(position, cachedFolders)
        return folderResultDomain.toUi()
    }

    fun deleteFiles(nameFolder: String) {
        viewModelScope.launch {
            val message = deleteFolderUseCase.invoke(nameFolder)
            _eventsMessages.emit(
                if (message) {
                    FolderActionEvent.DeleteFolderSuccess
                } else {
                    FolderActionEvent.ShowMessage("No se pudo borrar la carpeta correctamente")
                }
            )
        }
    }

    fun moveFileCancel() {
        viewModelScope.launch {
            _eventsMovingFiles.emit(UIMovingEvent.ShowMessage("Se ha cancelado la acción"))
        }
    }
}