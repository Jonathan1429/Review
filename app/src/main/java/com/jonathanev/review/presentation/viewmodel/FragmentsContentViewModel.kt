package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.mapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentsContentViewModel @Inject constructor(
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase
): ViewModel() {
    private var _folders = MutableLiveData<List<FolderUiModel>>()
    val folders: LiveData<List<FolderUiModel>> get() = _folders

    fun getAllFolders() {
        val foldersDomainModel = getFoldersWithNumGuidesUseCase.invoke()
        val foldersUiModel = foldersDomainModel.map { it.toUi() }
        _folders.postValue(foldersUiModel)
    }
}