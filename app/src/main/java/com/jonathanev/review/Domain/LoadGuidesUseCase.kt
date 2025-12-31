package com.jonathanev.review.Domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.model.GuideXmlModel
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class LoadGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(): List<GuideXmlModel> {
        val path = File(fileRepository.getCurrentPath())
        return guiaRepository.getGuides(path)
    }
}