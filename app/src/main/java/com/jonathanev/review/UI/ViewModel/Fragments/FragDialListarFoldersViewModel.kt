package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.data.FolderResult
import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.Model.FoldersUiState
import com.jonathanev.review.data.Model.prueba.FolderUI
import com.jonathanev.review.data.Model.prueba.UIMovingEvent
import com.jonathanev.review.data.Model.prueba.UIStopEvent
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.provider.GuiaProvider
import com.jonathanev.review.data.repository.FileHelperImpl
import com.jonathanev.review.Domain.DeleteContentGuidesUseCase
import com.jonathanev.review.Domain.DeleteFolderUseCase
import com.jonathanev.review.Domain.GetAllFoldersUseCase
import com.jonathanev.review.Domain.GetFolderPosicionUseCase
import com.jonathanev.review.Domain.GetFoldersCreatedUseCase
import com.jonathanev.review.Domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragDialListarFoldersViewModel @Inject constructor(
    //private val guiaRepositoryImpl: GuiaRepositoryImpl,
    private val guiaRepository: GuiaRepository,
    private val fileRepository: FileRepository,
    private val guiaProvider: GuiaProvider,
    private val getFolderPosicionUseCase: GetFolderPosicionUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val filePathsProvider: FilePathsProvider,
    //private val fileRepositoryImpl: FileRepositoryImpl,
    private val getFoldersCreatedUseCase: GetFoldersCreatedUseCase,
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase,
    private val deleteContentGuidesUseCase: DeleteContentGuidesUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val fileHelperImpl: FileHelperImpl,
) : ViewModel() {
    private var _foldersUiState = MutableStateFlow(FoldersUiState())
    val foldersUiState = _foldersUiState.asStateFlow()

    private var _file = MutableLiveData<File>()
    val file: MutableLiveData<File> get() = _file

    private var cachedFolders: List<FolderUI> = emptyList()

    private val _eventsMessages = MutableSharedFlow<UIStopEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    /*fun getAllGuides() {
        _guias.postValue(fileRepositoryImpl.getFilesInCurrentPath())
    }*/

    /*fun getAllFolders(): List<String>{
        val currentPath = File(getCurrentPath())
        return getAllFoldersUseCase.invoke(currentPath)
    }

    private fun getNumGuides(){
        getNumGuidesUseCase.invoke()
    }*/

    fun getAllFolders() {
        viewModelScope.launch {
            // 1. marcar loading
            _foldersUiState.value = _foldersUiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val folders =
                    getFoldersWithNumGuidesUseCase.invoke().sortedBy { it.folderModel.name }
                cachedFolders = folders

                // 2. actualizar con la lista resultante
                _foldersUiState.value = _foldersUiState.value.copy(
                    isLoading = false,
                    folders = folders
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
        val newPath =
            filePathsProvider.buildFolder(File(fileRepository.getCurrentPath()), folderName)
                .toString()

        fileRepository.setCurrentPath(newPath)
    }

    fun getCurrentPath(): String {
        return fileRepository.getCurrentPath()
    }

    fun getFirstPath() {
        _file.postValue(filePathsProvider.fileGuides)
    }

    fun getFolderSelected(position: Int): FolderResult {
        return getFolderPosicionUseCase.invoke(position, cachedFolders)
    }

    fun getFoldersCreated(): Array<String> {
        return getFoldersCreatedUseCase.invoke()
    }

    fun deleteFiles(folderResult: FolderUI) {
        val currentPath =
            filePathsProvider.buildFolder(File(getCurrentPath()), folderResult.folderModel.name)

        val message = deleteFolderUseCase.invoke(currentPath)

        viewModelScope.launch {
            if (message is UIStopEvent.DeleteGuideSuccess) {
                fileRepository.setCurrentPath(filePathsProvider.fileGuides.path)
            }

            _eventsMessages.emit(
                message
            )
        }
    }

    fun existFolder(fileName: String): Boolean {
        val currentPath = filePathsProvider.buildFolder(File(getCurrentPath()), fileName)
        return fileHelperImpl.exists(currentPath.toString())
    }

    fun moveFileCancel() {
        viewModelScope.launch {
            _eventsMovingFiles.emit(UIMovingEvent.ShowMessage("Se ha cancelado la acción"))
        }
    }
}