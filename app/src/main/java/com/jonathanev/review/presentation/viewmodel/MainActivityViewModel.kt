package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.CreateFoldersUseCase
import com.jonathanev.review.domain.MoveNonFolderFilesToOtrosUseCase
import com.jonathanev.review.presentation.state.FoldersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val moveNonFolderFilesToOtrosUseCase: MoveNonFolderFilesToOtrosUseCase,
    private val createFoldersUseCase: CreateFoldersUseCase,
) : ViewModel() {
    private val _shouldRequestPermission = MutableLiveData<Boolean>()
    val shouldRequestPermission: LiveData<Boolean> get() = _shouldRequestPermission

    fun movingFilesToOtros() {
        viewModelScope.launch {
            moveNonFolderFilesToOtrosUseCase.invoke()
        }
    }

    fun createFolders() = createFoldersUseCase.invoke()

    fun checkIfNeedsPermission(hasPermission: Boolean) {
        if (!hasPermission) {
            _shouldRequestPermission.value = true
        }
    }
}