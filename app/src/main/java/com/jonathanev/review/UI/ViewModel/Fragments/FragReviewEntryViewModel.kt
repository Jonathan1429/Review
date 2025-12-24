package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.ViewModel
import com.jonathanev.review.Domain.GetGuidesCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragReviewEntryViewModel @Inject constructor(
    private val getGuidesCountUseCase: GetGuidesCountUseCase
): ViewModel() {
    fun getGuides() {
        getGuidesCountUseCase.invoke()
    }
}