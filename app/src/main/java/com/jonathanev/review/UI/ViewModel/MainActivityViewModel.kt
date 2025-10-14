package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Domain.CreateFoldersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val createFoldersUseCase: CreateFoldersUseCase
) : ViewModel() {

    //var guias = MutableLiveData<List<GuiaModel>>()
    private val _guias = MutableLiveData<List<GuiaModel>>(emptyList())
    val guias: LiveData<List<GuiaModel>> get() = _guias
    private val _createdFolders = MutableLiveData(false)

    fun getAllGuias(file: File) = _guias.postValue(guiaRepository.getGuias(file))

    fun createFolders() = createFoldersUseCase.invoke()

    fun foldersCreated(value: Boolean){
        _createdFolders.postValue(value)
    }

    fun getFoldersCreated(): Boolean{
        return _createdFolders.value == true
    }
}