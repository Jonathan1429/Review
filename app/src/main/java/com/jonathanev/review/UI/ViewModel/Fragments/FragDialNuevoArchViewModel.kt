package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.FileAction
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.repository.FileHelperImpl
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.SetRenamingUseCase
import com.jonathanev.review.Domain.CreatingFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragDialNuevoArchViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val setRenamingUseCase: SetRenamingUseCase,
    private val creatingFolderUseCase: CreatingFolderUseCase,
    private val fileHelperImpl: FileHelperImpl,
    private val fileRepositoryImpl: FileRepositoryImpl
) : ViewModel() {
    private var _guias = MutableLiveData<List<GuideModel>>()
    val guias: MutableLiveData<List<GuideModel>> get() = _guias

    /*fun getAllUpdatedGuides(file: File) {
        _guias.postValue(guiaRepository.getGuias(file))
    }*/

    fun renamingFile(fileName: String): FileAction {
        return setRenamingUseCase.invoke(fileName)
    }

    fun exist(fileName: String): Boolean {
        val path = fileRepositoryImpl.getCurrentPath().substringBeforeLast("/")
        val newPath = "$path/$fileName.xml"
        return fileHelperImpl.exists(newPath)
    }

    fun creatingFolder(fileName: String): FileAction {
        return creatingFolderUseCase.invoke(fileName)
    }
}