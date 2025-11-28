package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.prueba.FolderModel
import com.jonathanev.review.Data.provider.GuiaProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentsContentViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository
): ViewModel() {
    private var _folders = MutableLiveData<List<FolderModel>>()
    val folders: MutableLiveData<List<FolderModel>> get() = _folders

    fun getAllFolders() {
        _folders.postValue(guiaRepository.getFolders())
    }
}