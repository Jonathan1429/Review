package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.domain.repository.FileNamingRules
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject

class GetGuidesCountUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(): Int {
        return guiaRepository.getNumGuides()
    }
}