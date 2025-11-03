package com.jonathanev.review.UI.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.CreateFoldersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val createFoldersUseCase: CreateFoldersUseCase,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val filePathsProvider: FilePathsProvider
) : ViewModel() {

    //var guias = MutableLiveData<List<GuiaModel>>()
    private val _guias = MutableLiveData<List<GuiaModel>>()
    val guias: LiveData<List<GuiaModel>> get() = _guias
    private val _createdFolders = MutableLiveData(false)
    private val _shouldRequestPermission = MutableLiveData<Boolean>()
    val shouldRequestPermission: LiveData<Boolean> get() = _shouldRequestPermission

    fun getAllGuias(file: File) {
        //return fileRepositoryImpl.getFilesInCurrentPath()
        /*val a = fileRepositoryImpl.getFilesInCurrentPath()
        Log.i("a", a.toString())
        _guias.postValue(a)*/
        _guias.postValue(guiaRepository.getGuias(file))
    }

    /*fun setGuiasInProvider(){
        fileRepositoryImpl.setFilesInCurrentPath()
        val a = fileRepositoryImpl.getFilesInCurrentPath()
        Log.i("a", a.toString())
        _guias.postValue(a)
    }*/

    fun createFolders() = createFoldersUseCase.invoke()

    fun foldersCreated(value: Boolean){
        _createdFolders.postValue(value)
    }

    fun getFoldersCreated(): Boolean{
        return _createdFolders.value == true
    }

    fun checkIfNeedsPermission(hasPermission: Boolean){
        if (!hasPermission){
            _shouldRequestPermission.value = true
        }
    }

    fun setCurrentPath(){
        fileRepositoryImpl.setCurrentPath(filePathsProvider.fileGuides.toString())
    }

    fun getCurrentPath(): String {
        return fileRepositoryImpl.getCurrentPath()
    }
}