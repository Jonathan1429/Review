package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.data.mapper.GuideXmlMapper
import java.io.File
import javax.inject.Inject

class LoadGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val pathProvider: PathProvider,
) {
    operator fun invoke(): List<GuideDomainModel> {
        val path = File(pathProvider.getCurrentPath())
        val guidesXML = guiaRepository.getGuides(path)
        return guidesXML.map { GuideXmlMapper.toDomain(it) }
    }
}