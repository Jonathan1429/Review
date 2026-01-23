package com.jonathanev.review.domain.result

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QAItemDomain

sealed class GetGuideResult {
    data class Success(val guideDomainModel: GuideDomainModel, val list: List<QAItemDomain>): GetGuideResult()
    data object NotFound : GetGuideResult()
    data object InvalidFormat : GetGuideResult()
    data object UnknownError : GetGuideResult()
    data object Error : GetGuideResult()
}