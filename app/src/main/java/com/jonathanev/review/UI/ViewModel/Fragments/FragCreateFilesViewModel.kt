package com.jonathanev.review.UI.ViewModel.Fragments

import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.Model.ScreenData
import com.jonathanev.review.Data.Model.prueba.PreviewState
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.CreateFolderUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragCreateFilesViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider,
    private val createFolderUseCase: CreateFolderUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PreviewState())
    val uiState = _uiState.asStateFlow()

    fun loadIconsFor(action: FolderAction) {
        val icons = when (action) {
            FolderAction.CREATING_FILE -> listOf(R.drawable.ic_lightbulb_solid_full)
            FolderAction.CREATING_FOLDER -> listOf(
                R.drawable.ic_anchor_solid_full,
                R.drawable.ic_angellist_brands_solid_full,
                R.drawable.ic_bacteria_solid_full
            )

            FolderAction.RENAMING_FILE -> emptyList()
            FolderAction.RENAMING_FOLDER -> emptyList()
            FolderAction.NONE -> emptyList()
        }

        _uiState.value = _uiState.value.copy(
            icons = icons,
            selectedIndex = 0,
            icon = icons.first(),
            //icon = icons.firstOrNull(),
            //name = "Nuevo archivo",
            color = Color.GRAY
        )
    }

    fun onIconSelected(position: Int) {
        val current = _uiState.value
        _uiState.value = current.copy(
            selectedIndex = position,
            icon = current.icons[position]
        )
    }

    fun setColor(color: Int) {
        _uiState.value = _uiState.value.copy(color = color)
    }

    fun validations(text: String): Boolean {
        return text.isEmpty()
    }

    fun saveMetadata(data: ScreenData) {
        val currentPath = File(fileRepository.getCurrentPath())
        val folderPath = File(currentPath, data.name)

        if (!folderPath.exists()){
            folderPath.mkdir()
            createScreenMetadata(data, folderPath)
        }
    }

    private fun createScreenMetadata(data: ScreenData, dir: File) {
        createFolderUseCase.invoke(data, dir)
    }
}