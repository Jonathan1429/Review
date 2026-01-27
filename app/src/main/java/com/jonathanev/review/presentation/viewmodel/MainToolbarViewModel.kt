package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.presentation.model.ToolbarUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainToolbarViewModel @Inject constructor(): ViewModel() {
    private var _title = MutableLiveData<String>()
    val title: LiveData<String> get() = _title

    private val _uiState = MutableStateFlow(ToolbarUiState())
    val uiState = _uiState.asStateFlow()

    private val _onSave = MutableSharedFlow<Unit>(replay = 0)
    val onSave = _onSave.asSharedFlow()

    private val _onBefore = MutableSharedFlow<Unit>(replay = 0)
    val onBefore = _onBefore.asSharedFlow()

    private val _onSuccess = MutableSharedFlow<Unit>(replay = 0)
    val onSuccess = _onSuccess.asSharedFlow()

    private val _onCancel = MutableSharedFlow<Unit>(replay = 0)
    val onCancel = _onCancel.asSharedFlow()

    fun changeTitle(title: String){
        _title.value = title
    }

    fun isBtnCancelVisible(visible: Boolean){
        _uiState.update { state ->
            state.copy(showCancel = visible)
        }
    }

    fun isBtnSuccessVisible(visible: Boolean){
        _uiState.update { state ->
            state.copy(showSuccess = visible)
        }
    }

    fun isBtnBackVisible(visible: Boolean){
        _uiState.update { state ->
            state.copy(showBack = visible)
        }
    }

    fun isBtnSaveVisible(visible: Boolean){
        _uiState.update { state ->
            state.copy(showSave = visible)
        }
    }

    fun init() {
        changeTitle("Review")
        initButtons()
    }

    fun initButtons() {
        _uiState.value = ToolbarUiState(
            showBack = false,
            showSave = false,
            showSuccess = false,
            showCancel = false
        )
    }

    fun btnSaveText(){
        viewModelScope.launch {
            _onSave.emit(Unit)
        }
    }

    fun btnBefore(){
        viewModelScope.launch {
            _onBefore.emit(Unit)
        }
    }

    fun btnSuccess(){
        viewModelScope.launch {
            _onSuccess.emit(Unit)
        }
    }

    fun btnCancel(){
        viewModelScope.launch {
            _onCancel.emit(Unit)
        }
    }
}