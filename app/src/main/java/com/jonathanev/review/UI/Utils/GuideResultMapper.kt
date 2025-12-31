package com.jonathanev.review.UI.Utils

import com.jonathanev.review.Domain.model.GuideDomainModel
import com.jonathanev.review.Domain.model.GuideResultDomain
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.GuideUiModel

fun GuideResultDomain.toUi(): GuideResultUi {
    return when (this) {
        is GuideResultDomain.Error -> GuideResultUi.Error(this.message)
        is GuideResultDomain.Success -> GuideResultUi.Success(this.guideDomainModel.toUi())
    }
}

fun GuideDomainModel.toUi(): GuideUiModel = GuideUiModel(this.nameGuide, this.description)