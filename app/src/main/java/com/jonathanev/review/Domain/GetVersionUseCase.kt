package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import java.io.File
import javax.inject.Inject

class GetVersionUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(file: File): String {
        return guiaRepository.getVersion(file)
    }
}