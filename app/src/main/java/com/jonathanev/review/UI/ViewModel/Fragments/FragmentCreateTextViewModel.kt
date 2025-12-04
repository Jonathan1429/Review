package com.jonathanev.review.UI.ViewModel.Fragments

import android.text.Editable
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Domain.SetPintarLetraUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentCreateTextViewModel @Inject constructor(
    private val setPintarLetraUseCase: SetPintarLetraUseCase
) : ViewModel() {
    fun setPintarLetra(texto: Editable, cursorPosition: Int, colorActual: Int) {
        setPintarLetraUseCase.invoke(texto, cursorPosition, colorActual)
    }
}