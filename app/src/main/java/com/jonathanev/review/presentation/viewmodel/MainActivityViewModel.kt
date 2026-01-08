package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.CreateFoldersUseCase
import com.jonathanev.review.domain.GetAllFoldersUseCase
import com.jonathanev.review.domain.MoveNonFolderFilesToOtrosUseCase
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.presentation.state.FoldersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val pathProvider: PathProvider,
    private val moveNonFolderFilesToOtrosUseCase: MoveNonFolderFilesToOtrosUseCase,
    private val createFoldersUseCase: CreateFoldersUseCase,
    private val filePathsProvider: FilePathsProvider,
    private val getAllFoldersUseCase: GetAllFoldersUseCase
) : ViewModel() {
    private val _shouldRequestPermission = MutableLiveData<Boolean>()
    val shouldRequestPermission: LiveData<Boolean> get() = _shouldRequestPermission

    private var _foldersUiState = MutableLiveData(FoldersUiState())
    val foldersUiState: LiveData<FoldersUiState> get() = _foldersUiState

    fun getAllFolders() {
        viewModelScope.launch {
            moveNonFolderFilesToOtrosUseCase.invoke()
            //getAllFoldersUseCase.invoke()
        }
    }

    fun createFolders() = createFoldersUseCase.invoke()

    fun checkIfNeedsPermission(hasPermission: Boolean) {
        if (!hasPermission) {
            _shouldRequestPermission.value = true
        }
    }

    /*fun setCurrentPath() {
        pathProvider.setCurrentPath(filePathsProvider.fileGuides.toString())
    }

    fun getCurrentPath(): String {
        return pathProvider.getCurrentPath()
    }*/
}