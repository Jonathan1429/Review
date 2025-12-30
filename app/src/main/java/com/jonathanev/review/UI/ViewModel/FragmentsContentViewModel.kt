package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.Domain.LoadFoldersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentsContentViewModel @Inject constructor(
    private val loadFoldersUseCase: LoadFoldersUseCase,
): ViewModel() {
    private var _folders = MutableLiveData<List<FolderUiModel>>()
    val folders: MutableLiveData<List<FolderUiModel>> get() = _folders

    fun getAllFolders() {
        _folders.postValue(loadFoldersUseCase.invoke())
    }
}