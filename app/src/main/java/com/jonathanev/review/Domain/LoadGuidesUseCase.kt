package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class LoadGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(): List<GuideModel> {
        val path = File(fileRepository.getCurrentPath())
        return guiaRepository.getGuides(path)
    }
}