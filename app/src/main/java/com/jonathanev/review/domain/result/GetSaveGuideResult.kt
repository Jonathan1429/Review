package com.jonathanev.review.domain.result

sealed class GetSaveGuideResult{
    data object SaveGuide: GetSaveGuideResult()
    data class Failure(val error: SaveGuideError): GetSaveGuideResult()
}
