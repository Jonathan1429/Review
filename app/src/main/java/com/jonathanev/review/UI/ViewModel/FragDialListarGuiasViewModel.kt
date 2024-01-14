package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Domain.getGuiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragDialListarGuiasViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    val getGuiaUseCase: getGuiaUseCase
): ViewModel() {
    var guias = MutableLiveData<List<GuiaModel>>()
    var guia = MutableLiveData<GuiaModel>()

    fun getAllGuias(){
        guias.postValue(guiaRepository.getGuias())
    }

    fun getGuia(position: Int){
        guia.postValue(getGuiaUseCase(position))
    }
}