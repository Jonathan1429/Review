package com.jonathanev.review.presentation.mapper

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.result.GuideResultDomain
import com.jonathanev.review.presentation.files.model.GuideResultUi
import com.jonathanev.review.presentation.files.model.GuideUiModel

fun GuideResultDomain.toUi(): GuideResultUi {
    return when (this) {
        is GuideResultDomain.Error -> GuideResultUi.Error
        is GuideResultDomain.Success -> GuideResultUi.Success(this.guideDomainModel.toUi())
    }
}

fun GuideDomainModel.toUi(): GuideUiModel = GuideUiModel(this.nameGuide, this.description)