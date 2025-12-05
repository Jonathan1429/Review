package com.jonathanev.review.UI.ViewModel.Fragments

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainToolbarViewModel @Inject constructor(): ViewModel() {
    private var _title = MutableLiveData<String>()
    val title: LiveData<String> get() = _title

    private val _isBackVisible = MutableLiveData<Int>()
    val isBackVisible: LiveData<Int> get() = _isBackVisible

    private val _isSaveVisible = MutableLiveData<Int>()
    val isSaveVisible: LiveData<Int> get() = _isSaveVisible

    fun changeTitle(title: String){
        _title.value = title
    }

    fun isBtnBackVisible(visible: Int){
        _isBackVisible.value = visible
    }

    fun isSaveVisible(visible: Int){
        _isSaveVisible.value = visible
    }

    fun init() {
        changeTitle("Review")
        isBtnBackVisible(View.GONE)
        isSaveVisible(View.GONE)
    }
}