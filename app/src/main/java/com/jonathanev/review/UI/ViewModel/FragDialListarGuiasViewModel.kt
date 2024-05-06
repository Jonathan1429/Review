package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import com.jonathanev.review.Domain.getGuiaPosicionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragDialListarGuiasViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val guiaProvider: GuiaProvider,
    private val getGuiaPosicionUseCase: getGuiaPosicionUseCase
): ViewModel() {
    var guias = MutableLiveData<List<GuiaModel>>()

    fun getAllGuias(){
        guias.postValue(guiaProvider.guias)
    }

    fun getAllUpdatedGuides(file: File){
        guias.postValue(guiaRepository.getGuias(file))
    }

    fun getGuia(position: Int):GuiaModel = getGuiaPosicionUseCase(position)
}