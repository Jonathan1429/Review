package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragDialNuevoArchViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository
) : ViewModel() {
    var guias = MutableLiveData<List<GuiaModel>>()

    fun getAllUpdatedGuides(file: File) {
        guias.postValue(guiaRepository.getGuias(file))
    }
}