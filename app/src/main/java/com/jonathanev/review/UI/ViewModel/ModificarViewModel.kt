package com.jonathanev.review.UI.ViewModel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Domain.getGuiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    // Data Store
    fun getCountImage() {
        viewModelScope.launch {
            dataStore.getCountImage()
        }
    }

    fun llamaCorruIncremento() {
        viewModelScope.launch {
            setIncrementCounter()
        }
    }

    suspend fun setIncrementCounter() {
        dataStore.setIncrementCounter()
    }

    suspend fun resetCounter() {
        dataStore.resetCounter()
    }

    fun getGuia(ruta: String) {
        guiaModel.postValue(getGuiaUseCase(ruta))
    }

    fun clickedSave() {
        saveClicked.postValue(!saveClicked.value!!)
    }

    fun clickedRoll() {
        rollClicked.postValue(!rollClicked.value!!)
    }
}