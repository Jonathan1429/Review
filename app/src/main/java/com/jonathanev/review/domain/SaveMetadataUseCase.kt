package com.jonathanev.review.domain

import com.jonathanev.review.data.mapper.json.toDto
import com.jonathanev.review.domain.model.ScreenDataDomain
import com.jonathanev.review.domain.repository.MetadataRepository
import com.jonathanev.review.presentation.model.ScreenDataUi
import javax.inject.Inject

class SaveMetadataUseCase @Inject constructor(
    private val metadataRepository: MetadataRepository,
) {
    operator fun invoke(data: ScreenDataDomain) {
        val screenDataDto = data.toDto()
        metadataRepository.saveMetadata(screenDataDto)
    }
}