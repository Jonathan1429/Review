package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.HasGuidesUseCase
import com.jonathanev.review.presentation.state.EntryGuidesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FragReviewEntryViewModel @Inject constructor(
    private val hasGuidesUseCase: HasGuidesUseCase
) : ViewModel() {
    val uiState: StateFlow<EntryGuidesUiState> = hasGuidesUseCase.invoke()
        .map { response ->
            if (response) EntryGuidesUiState.HasGuides
            else EntryGuidesUiState.Empty
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EntryGuidesUiState.Loading
        )
}