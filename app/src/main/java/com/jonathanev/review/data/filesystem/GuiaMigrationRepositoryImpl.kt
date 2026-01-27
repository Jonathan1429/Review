package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.repository.GuiaMigrationRepository
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import java.io.File
import javax.inject.Inject

class GuiaMigrationRepositoryImpl @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val imagesRepository: ImagesRepository
): GuiaMigrationRepository {
    override fun moveGuides() {
        val currentPath = File(navigationPathRepository.getPathGuides().value)

        val guidesInDivice = currentPath.listFiles()
            ?.filter { it.isFile && it.extension == Extensions.XML_EXTENSION } ?: emptyList()

        if (guidesInDivice.isNotEmpty()) {
            val otrosDir = File(currentPath, StorageFolders.OTROS)
            if (!otrosDir.exists()) {
                otrosDir.mkdir()
            }

            guidesInDivice.forEach { file ->
                val newPath = File(otrosDir, file.name)
                file.renameTo(newPath)
            }

            imagesRepository.moveUnassignedImages()
        }
    }
}