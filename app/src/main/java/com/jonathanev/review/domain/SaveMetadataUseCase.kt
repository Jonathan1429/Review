package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderScreenInfoDomain
import com.jonathanev.review.domain.repository.MetadataRepository
import javax.inject.Inject

class SaveMetadataUseCase @Inject constructor(
    private val metadataRepository: MetadataRepository,
) {
    operator fun invoke(data: FolderScreenInfoDomain) {
        metadataRepository.saveMetadata(data)
    }
}