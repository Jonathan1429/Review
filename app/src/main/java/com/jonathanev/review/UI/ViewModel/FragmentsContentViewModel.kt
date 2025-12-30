package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.data.Model.prueba.FolderModel
import com.jonathanev.review.Domain.LoadFoldersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentsContentViewModel @Inject constructor(
    private val loadFoldersUseCase: LoadFoldersUseCase,
): ViewModel() {
    private var _folders = MutableLiveData<List<FolderModel>>()
    val folders: MutableLiveData<List<FolderModel>> get() = _folders

    fun getAllFolders() {
        _folders.postValue(loadFoldersUseCase.invoke())
    }
}