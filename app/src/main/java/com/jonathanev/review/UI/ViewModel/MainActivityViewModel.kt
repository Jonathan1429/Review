package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Domain.getMainPathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val getMainPathUseCase: getMainPathUseCase
) : ViewModel() {

    var guias = MutableLiveData<List<GuiaModel>>()
    var file = MutableLiveData<File>()

    fun getAllGuias(file: File){
        guias.postValue(guiaRepository.getGuias(file))
    }

    fun getMainPath(){
        file.postValue(getMainPathUseCase())
    }
}