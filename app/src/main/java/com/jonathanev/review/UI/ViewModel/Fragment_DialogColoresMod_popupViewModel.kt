package com.jonathanev.review.UI.ViewModel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class Fragment_DialogColoresMod_popupViewModel @Inject constructor() : ViewModel() {

    // LiveData que almacena el color seleccionado
    private val _colorSeleccionado = MutableLiveData<Int>()
    val colorSeleccionado: LiveData<Int> get() = _colorSeleccionado

    // Método para actualizar el color
    fun setColor(color: Int) {
        _colorSeleccionado.value = color
    }

    // Método para resetear al color por defecto
    fun resetColor() {
        _colorSeleccionado.value = Color.BLACK
    }
}