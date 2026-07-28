package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.domain.InitializeGuideStorageUseCase
import com.jonathanev.review.domain.NextNavigationUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.presentation.event.MainUiEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.FolderUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val initializeGuideStorageUseCase: InitializeGuideStorageUseCase,
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase,
    private val resetNavigationUseCase: ResetNavigationUseCase,
    private val nextNavigationUseCase: NextNavigationUseCase
) : ViewModel() {
    /*private val _shouldRequestPermission = MutableLiveData<Boolean>()
    val shouldRequestPermission: LiveData<Boolean> get() = _shouldRequestPermission*/

    private val _folders = MutableStateFlow<List<FolderUiModel>>(emptyList())
    val folders: StateFlow<List<FolderUiModel>> = _folders.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MainUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    companion object {
        private const val KEY_GUIDES_PATH = "guides_path"
    }

    fun createFolders() {
        val isSuccess = initializeGuideStorageUseCase.invoke()
        if (!isSuccess){
            emitEvent(MainUiEvent.ShowCreateFoldersError)
        }
    }

    fun getAllFolders() {
        val foldersDomainModel = getFoldersWithNumGuidesUseCase.invoke()
        val foldersUiModel = foldersDomainModel.map { it.toUi() }
        _folders.value = foldersUiModel
    }

    private fun emitEvent(event: MainUiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    fun setMainPath() {
        viewModelScope.launch {
            resetNavigationUseCase.invoke()
        }
    }

    fun next(folder: String) {
        viewModelScope.launch {
            nextNavigationUseCase.invoke(folder)
        }
    }

    /*fun checkIfNeedsPermission(hasPermission: Boolean) {
        if (!hasPermission) {
            _shouldRequestPermission.value = true
        }
    }*/
}