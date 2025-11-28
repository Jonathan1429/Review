package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.Model.GuideResult
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.GetGuidePosicionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragmentListGuidesViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val getGuidePosicionUseCase: GetGuidePosicionUseCase,
    private val filePathsProvider: FilePathsProvider,
    private val fileRepositoryImpl: FileRepositoryImpl
): ViewModel() {
    private var cachedGuides: List<GuideModel> = emptyList()
    private var _guides = MutableLiveData<List<GuideModel>>()
    val guides get() = _guides

    fun getAllGuides() {
        cachedGuides = guiaRepository.getGuides()
        _guides.postValue(cachedGuides)
    }

    fun getGuideSelected(position: Int): GuideResult {
        return getGuidePosicionUseCase.invoke(position, cachedGuides)
    }

    fun setMainPath(){
        val initialPath = filePathsProvider.fileGuides
        fileRepositoryImpl.setCurrentPath(initialPath.path)

        getGuidesMainPath()
    }

    private fun getGuidesMainPath() {
        guiaRepository.getFolders()
    }

    fun changeFilePath(nameGuide: String) {
        val newPath = filePathsProvider.buildFile(File(fileRepositoryImpl.getCurrentPath()), nameGuide).toString()
        fileRepositoryImpl.setCurrentPath(newPath)
    }
}