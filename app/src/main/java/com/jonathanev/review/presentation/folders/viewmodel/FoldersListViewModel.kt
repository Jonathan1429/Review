package com.jonathanev.review.presentation.folders.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.ChangeFilePathUseCase
import com.jonathanev.review.domain.DeleteFolderUseCase
import com.jonathanev.review.domain.GetFolderPosicionUseCase
import com.jonathanev.review.domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.domain.SetCurrentPathUseCase
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.folders.model.FolderResult
import com.jonathanev.review.presentation.folders.model.FolderUiModel
import com.jonathanev.review.presentation.mapper.toDomainWithFolders
import com.jonathanev.review.presentation.mapper.toUi
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
    private val pathProvider: PathProvider,
    private val getFolderPosicionUseCase: GetFolderPosicionUseCase,
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase,
    private val setCurrentPathUseCase: SetCurrentPathUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val changeFilePathUseCase: ChangeFilePathUseCase
) : ViewModel() {
    private var _foldersUiState = MutableStateFlow(FoldersUiState())
    val foldersUiState = _foldersUiState.asStateFlow()

    private var cachedFolders: List<FolderUiModel> = emptyList()

    private val _eventsMessages = MutableSharedFlow<UIStopEvent>()
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
                val folderUi = folderDomain.map { it.toUi() }

                cachedFolders = folderUi

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

    // Actualizar todas las guias tampoco me convence VER ESTE MÉTODO
    /*private fun getAllGuides() {
        val currentPath = fileRepositoryImpl.getCurrentPath()

        guiaRepository.getGuias(currentPath)
        //_guias.postValue()
        getAllGuias()
    }*/

    fun changeFilePath(folderName: String) {
        changeFilePathUseCase.invoke(folderName)
    }

    fun getCurrentPath(): String {
        return pathProvider.getCurrentPath()
    }

    fun getFolderSelected(position: Int): FolderResult {
        val foldersDomain = cachedFolders.map { it.toDomainWithFolders() }
        return getFolderPosicionUseCase.invoke(position, foldersDomain)
    }

    fun deleteFiles(folderResult: FolderUiModel) {
        val message = deleteFolderUseCase.invoke(folderResult)

        viewModelScope.launch {
            if (message is UIStopEvent.DeleteGuideSuccess) {
                setCurrentPathUseCase.invoke()
            }

            _eventsMessages.emit(
                message
            )
        }
    }

    fun moveFileCancel() {
        viewModelScope.launch {
            _eventsMovingFiles.emit(UIMovingEvent.ShowMessage("Se ha cancelado la acción"))
        }
    }
}