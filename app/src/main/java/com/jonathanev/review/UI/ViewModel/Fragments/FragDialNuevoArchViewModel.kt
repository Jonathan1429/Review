package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.FileAction
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.repository.FileHelperImpl
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.SetRenamingUseCase
import com.jonathanev.review.Domain.repository.CreatingFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragDialNuevoArchViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val setRenamingUseCase: SetRenamingUseCase,
    private val creatingFolderUseCase: CreatingFolderUseCase,
    private val fileHelperImpl: FileHelperImpl,
    private val fileRepositoryImpl: FileRepositoryImpl
) : ViewModel() {
    private var _guias = MutableLiveData<List<GuiaModel>>()
    val guias: MutableLiveData<List<GuiaModel>> get() = _guias

    fun getAllUpdatedGuides(file: File) {
        _guias.postValue(guiaRepository.getGuias(file))
    }

    fun renamingFile(fileName: String): FileAction {
        return setRenamingUseCase.invoke(fileName)
    }

    fun exist(): Boolean {
        return fileHelperImpl.exists(fileRepositoryImpl.getCurrentPath())
    }

    fun creatingFolder(fileName: String) {
        creatingFolderUseCase.invoke(fileName)
    }
}