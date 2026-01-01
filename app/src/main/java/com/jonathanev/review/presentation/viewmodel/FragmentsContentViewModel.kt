package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.domain.GetFoldersWithNumGuidesUseCase
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.folders.model.FolderUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentsContentViewModel @Inject constructor(
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase
): ViewModel() {
    private var _folders = MutableLiveData<List<FolderUiModel>>()
    val folders: MutableLiveData<List<FolderUiModel>> get() = _folders

    fun getAllFolders() {
        val foldersWithNumGuidesDomain = getFoldersWithNumGuidesUseCase.invoke()
        val foldersWithNumGuidesUi = foldersWithNumGuidesDomain.map { it.toUi() }
        _folders.postValue(foldersWithNumGuidesUi)
    }
}