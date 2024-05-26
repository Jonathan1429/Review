package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Domain.getGuiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ModificarViewModel @Inject constructor(
    val getGuiaUseCase: getGuiaUseCase
) : ViewModel(){
    val guiaModel = MutableLiveData<GuiaModel>()
    var colorAnterior = MutableLiveData<Int>()
    var saveClicked = MutableLiveData<Boolean>().apply { value = false }
    var rollClicked = MutableLiveData<Boolean>().apply { value = false }

    fun getGuia(ruta: String){
        guiaModel.postValue(getGuiaUseCase(ruta))
    }

    fun clickedSave(){
        saveClicked.postValue(!saveClicked.value!!)
    }

    fun clickedRoll(){
        rollClicked.postValue(!rollClicked.value!!)
    }

    fun setColorAnterior(color: Int){
        colorAnterior.postValue(color)
    }
}