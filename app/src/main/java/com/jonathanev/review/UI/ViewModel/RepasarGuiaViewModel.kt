package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Domain.getGuiaPosicionUseCase
import com.jonathanev.review.Domain.getGuiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RepasarGuiaViewModel @Inject constructor(
    val getGuiaUseCase: getGuiaUseCase
): ViewModel(){
    val guiaModel = MutableLiveData<GuiaModel>()

    fun getGuia(ruta: String){
        guiaModel.postValue(getGuiaUseCase(ruta))
    }
}