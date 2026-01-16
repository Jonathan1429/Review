package com.jonathanev.review.domain

import com.jonathanev.review.data.repository.GuiaMigrationRepository
import javax.inject.Inject

class MoveNonFolderFilesToOtrosUseCase @Inject constructor(
    private val migrationRepository: GuiaMigrationRepository
) {
    operator fun invoke() {
        return migrationRepository.moveGuides()
    }
}