package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.presentation.folders.model.FolderAction
import javax.inject.Inject

class MoveGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(mode: FolderAction.MovingFile): Boolean {
        return guiaRepository.moveGuide(mode)
    }
}