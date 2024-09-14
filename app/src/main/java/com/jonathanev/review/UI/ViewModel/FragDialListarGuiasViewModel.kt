package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import com.jonathanev.review.Domain.getGuiaPosicionUseCase
import com.jonathanev.review.Domain.getMainPathUseCase
import com.jonathanev.review.Domain.setChangePathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragDialListarGuiasViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val guiaProvider: GuiaProvider,
    private val getGuiaPosicionUseCase: getGuiaPosicionUseCase,
    private val setChangePathUseCase: setChangePathUseCase,
    private val getMainPathUseCase: getMainPathUseCase
) : ViewModel() {
    var guias = MutableLiveData<List<GuiaModel>>()
    var file = MutableLiveData<File>()

    fun getAllGuias() {
        guias.postValue(guiaProvider.guias)
    }

    fun getAllUpdatedGuides(file: File) {
        guias.postValue(guiaRepository.getGuias(file))
    }

    fun changeFilePath(folderName: String) {
        // return setChangePathUseCase(folderName)
        file.postValue(setChangePathUseCase(folderName))
    }

    fun getMainPath() {
        file.postValue(getMainPathUseCase())
    }

    fun getGuia(position: Int): GuiaModel = getGuiaPosicionUseCase(position)
}