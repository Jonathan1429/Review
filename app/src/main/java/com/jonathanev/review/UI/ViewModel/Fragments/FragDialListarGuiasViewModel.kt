package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.GuiaResult
import com.jonathanev.review.Data.Model.FoldersUiState
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Data.repository.FileHelperImpl
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.DeleteContentGuidesUseCase
import com.jonathanev.review.Domain.GetAllFoldersUseCase
import com.jonathanev.review.Domain.GetGuiaPosicionUseCase
import com.jonathanev.review.Domain.GetFoldersCreatedUseCase
import com.jonathanev.review.Domain.GetNumGuidesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragDialListarGuiasViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val guiaProvider: GuiaProvider,
    private val getGuiaPosicionUseCase: GetGuiaPosicionUseCase,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val filePathsProvider: FilePathsProvider,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val getFoldersCreatedUseCase: GetFoldersCreatedUseCase,
    private val getNumGuidesUseCase: GetNumGuidesUseCase,
    private val deleteContentGuidesUseCase: DeleteContentGuidesUseCase,
    private val fileHelperImpl: FileHelperImpl,
) : ViewModel() {
    private var _foldersUiState = MutableLiveData(FoldersUiState())
    val foldersUiState: LiveData<FoldersUiState> get() = _foldersUiState

    private var _file = MutableLiveData<File>()
    val file: MutableLiveData<File> get() = _file

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

    fun getAllGuias() {
        //_guias.postValue(guiaProvider.guias)
        _foldersUiState.value = _foldersUiState.value?.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val guias = guiaProvider.guias
                val nums = getNumGuidesUseCase.invoke()

                val uiList = guias.mapIndexed { index, guia ->
                    val num = nums.getOrNull(index) ?: 0
                    guia.copy(num = num)
                }

                _foldersUiState.postValue(
                    _foldersUiState.value?.copy(
                        isLoading = false,
                        folders = uiList
                    )
                )
            } catch (e: Exception) {
                _foldersUiState.postValue(
                    _foldersUiState.value?.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                )
            }
        }
    }

    // Actualizar todas las guias tampoco me convence VER ESTE MÉTODO
    private fun getAllUpdatedGuides(file: File) {
        guiaRepository.getGuias(file)
        //_guias.postValue()
        getAllGuias()
    }

    fun changeFilePath(folderName: String) {
        var lastPath = filePathsProvider.buildFolder(File(fileRepositoryImpl.getCurrentPath()), folderName).toString()

        // Si es archivo completamos la ruta con .xml
        if (File("$lastPath.xml").exists()) {
            lastPath = "${lastPath}.xml"
        }

        fileRepositoryImpl.setCurrentPath(lastPath)
        getAllUpdatedGuides(File(lastPath))
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

    fun getGuia(position: Int): GuiaResult {
        return getGuiaPosicionUseCase.invoke(position, guiaProvider.guias)
    }

    fun getFoldersCreated(): Array<String> {
        return getFoldersCreatedUseCase.invoke()
    }

    fun deleteFiles(guiaModel: GuiaModel): String {
        val currentPath = filePathsProvider.buildFolder(File(getCurrentPath()), guiaModel.nombreGuia)
        val response = deleteContentGuidesUseCase.invoke(currentPath)
        val type = if(guiaModel.carpeta) "archivo" else "folder"

        var msgResponse = "Error al eliminar la $type"

        if (response) {
            getAllUpdatedGuides(filePathsProvider.fileGuides)
            msgResponse = "¡Exitosamente elminado el $type!"
        }

        return msgResponse
    }

    fun existFolder(fileName: String): Boolean {
        val currentPath = filePathsProvider.buildFolder(File(getCurrentPath()), fileName)
        return fileHelperImpl.exists(currentPath.toString())
    }
}