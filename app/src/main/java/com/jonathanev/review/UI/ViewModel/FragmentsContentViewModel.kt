package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.provider.GuiaProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentsContentViewModel @Inject constructor(
    private val guiaProvider: GuiaProvider
): ViewModel() {
    private var _guias = MutableLiveData<List<GuiaModel>>()
    val guias: MutableLiveData<List<GuiaModel>> get() = _guias

    fun getAllGuias() {
        _guias.postValue(guiaProvider.guias)
    }
}