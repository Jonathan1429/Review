package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragmentWithoutFilesViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val guiaRepository: GuiaRepository,
    private val filePathsProvider: FilePathsProvider,
): ViewModel(){
    fun setMainPath(){
        val initialPath = filePathsProvider.fileGuides
        fileRepository.setCurrentPath(initialPath.path)

        setGuidesMainPath()
    }

    private fun setGuidesMainPath() {
        val initialPath = filePathsProvider.fileGuides
        guiaRepository.getFolders(initialPath)
    }
}