package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentWithoutFilesViewModel @Inject constructor(
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val filePathsProvider: FilePathsProvider,
    private val guiaRepository: GuiaRepository
): ViewModel(){
    fun setMainPath(){
        val initialPath = filePathsProvider.fileGuides
        fileRepositoryImpl.setCurrentPath(initialPath.path)

        setGuidesMainPath()
    }

    private fun setGuidesMainPath() {
        guiaRepository.getFolders()
    }
}