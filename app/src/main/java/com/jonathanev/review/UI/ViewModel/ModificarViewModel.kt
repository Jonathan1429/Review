package com.jonathanev.review.UI.ViewModel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Domain.getGuiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ModificarViewModel @Inject constructor(
    application: Application,
    val getGuiaUseCase: getGuiaUseCase
) : ViewModel() {
    private val dataStore = DataStoreManager.getInstance(application)
    val contImagenes = dataStore.getCountImage().asLiveData()
    val guiaModel = MutableLiveData<GuiaModel>()
    var colorAnterior = MutableLiveData<Int>()
    var saveClicked = MutableLiveData<Boolean>().apply { value = false }
    var rollClicked = MutableLiveData<Boolean>().apply { value = false }

    fun getGuia(ruta: String) {
        guiaModel.postValue(getGuiaUseCase(ruta))
    }

    fun clickedSave() {
        saveClicked.postValue(!saveClicked.value!!)
    }

    fun clickedRoll() {
        rollClicked.postValue(!rollClicked.value!!)
    }

    fun setColorAnterior(color: Int) {
        colorAnterior.postValue(color)
    }

    // Data Store
    fun getCountImage() {
        dataStore.getCountImage()
    }

    suspend fun setIncrementCounter() {
        dataStore.setIncrementCounter()
    }

    suspend fun resetCounter() {
        dataStore.resetCounter()
    }
}