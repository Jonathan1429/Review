package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class MoveNonFolderFilesToOtrosUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke() {
        return guiaRepository.moveGuides()
    }
}