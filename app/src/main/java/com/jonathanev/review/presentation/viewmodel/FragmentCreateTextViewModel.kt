package com.jonathanev.review.presentation.viewmodel

import android.text.Editable
import androidx.lifecycle.ViewModel
import com.jonathanev.review.domain.SetPintarLetraUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentCreateTextViewModel @Inject constructor(
    private val setPintarLetraUseCase: SetPintarLetraUseCase
) : ViewModel() {
    fun setPintarLetra(texto: Editable, cursorPosition: Int, colorActual: Int) {
        //setPintarLetraUseCase.invoke(texto, cursorPosition, colorActual)
    }
}