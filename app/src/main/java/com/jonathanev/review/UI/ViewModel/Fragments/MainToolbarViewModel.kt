package com.jonathanev.review.UI.ViewModel.Fragments

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainToolbarViewModel @Inject constructor(): ViewModel() {
    private var _title = MutableLiveData<String>()
    val title: LiveData<String> get() = _title

    private val _isBackVisible = MutableLiveData<Int>()
    val isBackVisible: LiveData<Int> get() = _isBackVisible

    private val _isSaveVisible = MutableLiveData<Int>()
    val isSaveVisible: LiveData<Int> get() = _isSaveVisible

    private val _isCancelVisible = MutableLiveData<Int>()
    val isCancelVisible: LiveData<Int> get() = _isCancelVisible

    private val _isSuccessVisible = MutableLiveData<Int>()
    val isSuccessVisible: LiveData<Int> get() = _isSuccessVisible

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

    fun isBtnCancelVisible(visible: Int){
        _isCancelVisible.value = visible
    }

    fun isBtnSuccessVisible(visible: Int){
        _isSuccessVisible.value = visible
    }

    fun isBtnBackVisible(visible: Int){
        _isBackVisible.value = visible
    }

    fun isBtnSaveVisible(visible: Int){
        _isSaveVisible.value = visible
    }

    fun init() {
        changeTitle("Review")
        initButtons()
    }

    fun initButtons() {
        isBtnBackVisible(View.GONE)
        isBtnSaveVisible(View.GONE)
        isBtnSuccessVisible(View.GONE)
        isBtnCancelVisible(View.GONE)
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