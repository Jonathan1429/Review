package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.FolderResult
import com.jonathanev.review.Data.Model.FoldersUiState
import com.jonathanev.review.Data.Model.prueba.FolderUI
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Data.repository.FileHelperImpl
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.DeleteContentGuidesUseCase
import com.jonathanev.review.Domain.GetAllFoldersUseCase
import com.jonathanev.review.Domain.GetFoldersCreatedUseCase
import com.jonathanev.review.Domain.GetFolderPosicionUseCase
import com.jonathanev.review.Domain.GetFoldersWithNumGuidesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragDialListarFoldersViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val guiaProvider: GuiaProvider,
    private val getFolderPosicionUseCase: GetFolderPosicionUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val filePathsProvider: FilePathsProvider,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val getFoldersCreatedUseCase: GetFoldersCreatedUseCase,
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase,
    private val deleteContentGuidesUseCase: DeleteContentGuidesUseCase,
    private val fileHelperImpl: FileHelperImpl,
) : ViewModel() {
    private var _foldersUiState = MutableStateFlow(FoldersUiState())
    val foldersUiState = _foldersUiState.asStateFlow()

    private var _file = MutableLiveData<File>()
    val file: MutableLiveData<File> get() = _file

    private var cachedFolders: List<FolderUI> = emptyList()

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
                val folders = getFoldersWithNumGuidesUseCase.invoke().sortedBy { it.folderModel.nameFolder }
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
        val newPath = filePathsProvider.buildFolder(File(fileRepositoryImpl.getCurrentPath()), folderName).toString()

        fileRepositoryImpl.setCurrentPath(newPath)
        //val currentPath = File(fileRepositoryImpl.getCurrentPath())
        //guiaRepository.getGuias(currentPath)

        /*lastPath = "${lastPath}.xml"
        // hace esto si es carpeta
        _guias.postValue(guiaRepository.getGuias(File(lastPath)))
        _file.postValue(File(lastPath))*/
    }

    fun getCurrentPath(): String {
        return fileRepositoryImpl.getCurrentPath()
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

    fun deleteFiles(folderResult: FolderUI): String {
        val currentPath = filePathsProvider.buildFolder(File(getCurrentPath()), folderResult.folderModel.nameFolder)
        val response = deleteContentGuidesUseCase.invoke(currentPath)

        var msgResponse = "Error al eliminar la carpeta"

        if (response) {
            guiaRepository.getFolders()
            //getAllGuides(filePathsProvider.fileGuides)
            msgResponse = "¡Exitosamente se ha eliminado la carpeta"
        }

        return msgResponse
    }

    fun existFolder(fileName: String): Boolean {
        val currentPath = filePathsProvider.buildFolder(File(getCurrentPath()), fileName)
        return fileHelperImpl.exists(currentPath.toString())
    }
}