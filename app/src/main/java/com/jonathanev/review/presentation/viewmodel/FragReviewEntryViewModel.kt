package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.GuiaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FragReviewEntryViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository
) : ViewModel() {
    fun getGuides(relativeGuidePath: RelativeGuidePath): Int {
        return guiaRepository.getNumGuides(relativeGuidePath)
    }
}