package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.InitializeGuideStorageUseCase
import com.jonathanev.review.presentation.event.MainUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val initializeGuideStorageUseCase: InitializeGuideStorageUseCase,
) : ViewModel() {
    /*private val _shouldRequestPermission = MutableLiveData<Boolean>()
    val shouldRequestPermission: LiveData<Boolean> get() = _shouldRequestPermission*/

    private val _uiEvent = MutableSharedFlow<MainUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

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

    /*fun checkIfNeedsPermission(hasPermission: Boolean) {
        if (!hasPermission) {
            _shouldRequestPermission.value = true
        }
    }*/
}