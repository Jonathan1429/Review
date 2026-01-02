package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.MetadataRepository
import com.jonathanev.review.presentation.model.ScreenData
import javax.inject.Inject

class SaveMetadataUseCase @Inject constructor(
    private val metadataRepository: MetadataRepository,
) {
    operator fun invoke(data: ScreenData) {
        metadataRepository.saveMetadata(data)
    }
}