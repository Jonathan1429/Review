package com.jonathanev.review.presentation.mapper

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.result.GuideResultDomain
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.domain.model.GuideVersion as GuideVersionDomain
import com.jonathanev.review.presentation.model.GuideVersion as GuideVersionUI

fun GuideResultDomain.toUi(): GuideResultUi {
    return when (this) {
        is GuideResultDomain.Error -> GuideResultUi.Error
        is GuideResultDomain.Success -> GuideResultUi.Success(this.guideDomainModel.toUi())
    }
}

fun GuideUiModel.toDomain(): GuideDomainModel {
    return GuideDomainModel(
        version = this.version.toDomain(),
        nameGuide = this.nameGuide,
        description = this.description
    )
}

fun GuideDomainModel.toUi(): GuideUiModel {
    return GuideUiModel(
        version = this.version.toUI(),
        nameGuide = this.nameGuide,
        description = this.description
    )
}

fun GuideVersionDomain.toUI(): GuideVersionUI {
    return when (this) {
        GuideVersionDomain.V1 -> GuideVersionUI.V1
        GuideVersionDomain.V2 -> GuideVersionUI.V2
    }
}

fun GuideVersionUI.toDomain(): GuideVersionDomain {
    return when (this) {
        GuideVersionUI.V1 -> GuideVersionDomain.V1
        GuideVersionUI.V2 -> GuideVersionDomain.V2
    }
}