package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Domain.getGuiaPosicionUseCase
import com.jonathanev.review.Domain.getGuiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragDialListarGuiasViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val getGuiaPosicionUseCase: getGuiaPosicionUseCase
): ViewModel() {
    var guias = MutableLiveData<List<GuiaModel>>()
    private var guiaModel = MutableLiveData<GuiaModel>()

    fun getAllGuias(file: File){
        guias.postValue(guiaRepository.getGuias(file))
    }

    fun getGuia(position: Int):GuiaModel = getGuiaPosicionUseCase(position)
}