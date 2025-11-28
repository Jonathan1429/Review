package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.FoldersUiState
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.CreateFoldersUseCase
import com.jonathanev.review.Domain.MoveNonFolderFilesToOtrosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val moveNonFolderFilesToOtrosUseCase: MoveNonFolderFilesToOtrosUseCase,
    private val createFoldersUseCase: CreateFoldersUseCase,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val filePathsProvider: FilePathsProvider
) : ViewModel() {
    private val _shouldRequestPermission = MutableLiveData<Boolean>()
    val shouldRequestPermission: LiveData<Boolean> get() = _shouldRequestPermission

    private var _foldersUiState = MutableLiveData(FoldersUiState())
    val foldersUiState: LiveData<FoldersUiState> get() = _foldersUiState

    fun getAllFolders() {
        //return fileRepositoryImpl.getFilesInCurrentPath()
        /*val a = fileRepositoryImpl.getFilesInCurrentPath()
        Log.i("a", a.toString())
        _guias.postValue(a)*/

        viewModelScope.launch {
            // Mover archivos (espera a que termine)
            moveNonFolderFilesToOtrosUseCase.invoke()

            guiaRepository.getFolders()
        }
    }

    /*fun setGuiasInProvider(){
        fileRepositoryImpl.setFilesInCurrentPath()
        val a = fileRepositoryImpl.getFilesInCurrentPath()
        Log.i("a", a.toString())
        _guias.postValue(a)
    }*/

    fun createFolders() = createFoldersUseCase.invoke()

    /*fun foldersCreated(value: Boolean){
        _createdFolders.postValue(value)
    }

    fun getFoldersCreated(): Boolean{
        return _createdFolders.value == true
    }*/

    fun checkIfNeedsPermission(hasPermission: Boolean){
        if (!hasPermission){
            _shouldRequestPermission.value = true
        }
    }

    fun setCurrentPath(){
        fileRepositoryImpl.setCurrentPath(filePathsProvider.fileGuides.toString())
    }

    fun getCurrentPath(): String {
        return fileRepositoryImpl.getCurrentPath()
    }
}