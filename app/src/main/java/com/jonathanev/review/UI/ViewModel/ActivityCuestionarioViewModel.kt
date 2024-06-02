package com.jonathanev.review.UI.ViewModel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ActivityCuestionarioViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository
) : ViewModel(){
    var guias = MutableLiveData<List<GuiaModel>>()
    var imagenes = MutableLiveData<List<Uri>>()
    var colorAnterior = MutableLiveData<Int>()
    var saveClicked = MutableLiveData<Boolean>().apply { value = false }
    var rollClicked = MutableLiveData<Boolean>().apply { value = false }

    fun getAllUpdatedGuides(file: File){
        guias.postValue(guiaRepository.getGuias(file))
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