package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Core.Constants
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import com.jonathanev.review.Domain.GetGuiaPosicionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragDialListarGuiasViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val guiaProvider: GuiaProvider,
    private val getGuiaPosicionUseCase: GetGuiaPosicionUseCase,
) : ViewModel() {
    private var _guias = MutableLiveData<List<GuiaModel>>()
    val guias: MutableLiveData<List<GuiaModel>> get() = _guias

    private var _file = MutableLiveData<File>()
    val file: MutableLiveData<File> get() = _file

    fun getAllGuias() {
        _guias.postValue(guiaProvider.guias)
    }

    fun getAllUpdatedGuides(file: File) {
        _guias.postValue(guiaRepository.getGuias(file))
    }

    fun changeFilePath(folderName: String) {
        _file.postValue(File("${Constants.file}/$folderName"))
    }

    fun getMainPath() {
        _file.postValue(Constants.file)
    }

    fun getGuia(position: Int): GuiaModel = getGuiaPosicionUseCase(position)
}