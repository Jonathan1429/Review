package com.jonathanev.review.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ColorViewModel : ViewModel() {
    private val _color = MutableLiveData<Int>()
    val color: LiveData<Int>
        get() = _color

    fun setColor(nuevoColor: Int) {
        _color.value = nuevoColor
    }
}