package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.Model.FoldersUiState
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Domain.CreateFoldersUseCase
import com.jonathanev.review.Domain.MoveNonFolderFilesToOtrosUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val fileRepository: FileRepository,
    private val moveNonFolderFilesToOtrosUseCase: MoveNonFolderFilesToOtrosUseCase,
    private val createFoldersUseCase: CreateFoldersUseCase,
    private val filePathsProvider: FilePathsProvider
) : ViewModel() {
    private val _shouldRequestPermission = MutableLiveData<Boolean>()
    val shouldRequestPermission: LiveData<Boolean> get() = _shouldRequestPermission

    private var _foldersUiState = MutableLiveData(FoldersUiState())
    val foldersUiState: LiveData<FoldersUiState> get() = _foldersUiState

    fun getAllFolders() {
        viewModelScope.launch {
            // Mover archivos (espera a que termine)
            val currentPath = fileRepository.getCurrentPath()
            moveNonFolderFilesToOtrosUseCase.invoke()
            guiaRepository.getFolders(File(currentPath))
        }
    }

    fun createFolders() = createFoldersUseCase.invoke()

    fun checkIfNeedsPermission(hasPermission: Boolean) {
        if (!hasPermission) {
            _shouldRequestPermission.value = true
        }
    }

    fun setCurrentPath() {
        fileRepository.setCurrentPath(filePathsProvider.fileGuides.toString())
    }

    fun getCurrentPath(): String {
        return fileRepository.getCurrentPath()
    }
}