package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jonathanev.review.domain.BackNavigationUseCase
import com.jonathanev.review.domain.NextNavigationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val nextNavigationUseCase: NextNavigationUseCase,
    private val backNavigationUseCase: BackNavigationUseCase
) : ViewModel() {
    companion object {
        private const val KEY_GUIDES_PATH = "guides_path"
    }

    private val _guidesPath =
        MutableStateFlow(
            savedStateHandle[KEY_GUIDES_PATH]
                ?: ""
        )
    val guidesPath: StateFlow<String> = _guidesPath

    fun setMainPath() {
        _guidesPath.value = ""
        savedStateHandle[KEY_GUIDES_PATH] = ""
    }

    fun next(folder: String) {
        val nextGuides =
            nextNavigationUseCase.invoke(guidesPath.value, folder)

        _guidesPath.value = nextGuides.value

        savedStateHandle[KEY_GUIDES_PATH] = nextGuides.value
    }

    fun back() {
        val backGuides =
            backNavigationUseCase.invoke(guidesPath.value)
        _guidesPath.value = backGuides.value

        savedStateHandle[KEY_GUIDES_PATH] = backGuides.value
    }
}