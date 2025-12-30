package com.jonathanev.review.Domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.Model.GuideModel
import java.io.File
import javax.inject.Inject

class GetAttributesGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(file: File): GuideModel {
        return guiaRepository.getAttributesGuide(file)
    }
}