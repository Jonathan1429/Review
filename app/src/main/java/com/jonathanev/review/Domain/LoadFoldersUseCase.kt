package com.jonathanev.review.Domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class LoadFoldersUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val fileRepository: FileRepository
) {
    operator fun invoke(): List<FolderUiModel> {
        val path = File(fileRepository.getCurrentPath())
        return guiaRepository.getFolders(path)
    }
}