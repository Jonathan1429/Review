package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.InitializeGuideStorageUseCase
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.presentation.event.MainUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val initializeGuideStorageUseCase: InitializeGuideStorageUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val navigationPathRepository: NavigationPathRepository
) : ViewModel() {
    /*private val _shouldRequestPermission = MutableLiveData<Boolean>()
    val shouldRequestPermission: LiveData<Boolean> get() = _shouldRequestPermission*/

    private val _uiEvent = MutableSharedFlow<MainUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    companion object {
        private const val KEY_GUIDES_PATH = "guides_path"
    }

    private val _guidesPath =
        MutableStateFlow(
            savedStateHandle[KEY_GUIDES_PATH]
                ?: ""
        )
    val guidesPath: StateFlow<String> = _guidesPath
    fun createFolders() {
        val isSuccess = initializeGuideStorageUseCase.invoke()
        if (!isSuccess){
            emitEvent(MainUiEvent.ShowCreateFoldersError)
        }
    }

    private fun emitEvent(event: MainUiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    fun setMainPath() {
        _guidesPath.value = ""
        savedStateHandle[KEY_GUIDES_PATH] = guidesPath
    }

    fun next(folder: String) {
        val nextGuides =
            navigationPathRepository.next(RelativeGuidePath(guidesPath.value), folder)

        _guidesPath.value = nextGuides.value

        savedStateHandle[KEY_GUIDES_PATH] = nextGuides.value
    }

    fun back() {
        val backGuides =
            navigationPathRepository.back(RelativeGuidePath(guidesPath.value))
        _guidesPath.value = backGuides.value

        savedStateHandle[KEY_GUIDES_PATH] = backGuides.value
    }
    /*fun checkIfNeedsPermission(hasPermission: Boolean) {
        if (!hasPermission) {
            _shouldRequestPermission.value = true
        }
    }*/
}