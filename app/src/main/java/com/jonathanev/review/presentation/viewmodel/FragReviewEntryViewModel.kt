package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.HasGuidesUseCase
import com.jonathanev.review.domain.ObservePathUseCase
import com.jonathanev.review.presentation.state.EntryGuidesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FragReviewEntryViewModel @Inject constructor(
    private val observePathUseCase: ObservePathUseCase,
    private val hasGuidesUseCase: HasGuidesUseCase
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EntryGuidesUiState> = observePathUseCase.invoke()
        .flatMapLatest { _ ->
            hasGuidesUseCase.invoke()
        }
        .map { response ->
            if (response) EntryGuidesUiState.HasGuides
            else EntryGuidesUiState.Empty
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = EntryGuidesUiState.Loading
        )
}