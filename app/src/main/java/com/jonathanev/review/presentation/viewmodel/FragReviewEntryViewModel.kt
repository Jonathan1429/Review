package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.HasGuidesUseCase
import com.jonathanev.review.presentation.state.EntryGuidesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragReviewEntryViewModel @Inject constructor(
    private val hasGuidesUseCase: HasGuidesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<EntryGuidesUiState>(EntryGuidesUiState.Loading)
    val uiState: StateFlow<EntryGuidesUiState> = _uiState.asStateFlow()

    init {
        checkHasGuides()
    }

    fun checkHasGuides() {
        viewModelScope.launch {
            _uiState.value = EntryGuidesUiState.Loading

            val hasGuides = hasGuidesUseCase()
            _uiState.value = if (hasGuides) {
                EntryGuidesUiState.HasGuides
            } else {
                EntryGuidesUiState.Empty
            }
        }
    }
}