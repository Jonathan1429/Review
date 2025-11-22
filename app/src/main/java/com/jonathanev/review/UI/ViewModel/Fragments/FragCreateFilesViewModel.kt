package com.jonathanev.review.UI.ViewModel.Fragments

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.Model.prueba.PreviewState
import com.jonathanev.review.Domain.CreatingFolderUseCase
import com.jonathanev.review.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragCreateFilesViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableLiveData(PreviewState())
    val uiState: LiveData<PreviewState> get() = _uiState

    fun loadIconsFor(action: FolderAction) {
        val icons = when(action) {
            FolderAction.CREATING_FILE -> listOf(R.drawable.ic_archivo)
            FolderAction.CREATING_FOLDER -> listOf(
                R.drawable.ic_anchor_solid_full,
                R.drawable.ic_angellist_brands_solid_full,
                R.drawable.ic_bacteria_solid_full
            )

            FolderAction.RENAMING_FILE -> emptyList()
            FolderAction.RENAMING_FOLDER -> emptyList()
            FolderAction.NONE -> emptyList()
        }

        _uiState.value = _uiState.value!!.copy(
            icons = icons,
            selectedIndex = 0,
            icon = icons.first(),
            //icon = icons.firstOrNull(),
            //name = "Nuevo archivo",
            color = Color.GRAY
        )
    }

    fun onIconSelected(position: Int) {
        val current = _uiState.value!!
        _uiState.value = current.copy(
            selectedIndex = position,
            icon = current.icons[position]
        )
    }

    fun setColor(color: Int) {
        _uiState.value = _uiState.value!!.copy(color = color)
    }
}