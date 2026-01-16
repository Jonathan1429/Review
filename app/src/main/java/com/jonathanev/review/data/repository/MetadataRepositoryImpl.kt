package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.data.model.json.ScreenDataDto
import com.jonathanev.review.domain.repository.MetadataRepository
import java.io.File
import javax.inject.Inject

class MetadataRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val jsonManager: JsonManager,
    private val navigationPathRepository: NavigationPathRepository
) : MetadataRepository {
    override fun saveMetadata(data: ScreenDataDto) {
        val currentPath =
            File(filePathsProvider.buildFolder(navigationPathRepository.currentPathGuides, data.name))

        if (!currentPath.exists()) {
            currentPath.mkdir()
        }

        val screenFile = File(currentPath, "screen.json")

        jsonManager.write(screenFile, ScreenDataDto.serializer(), data)
    }
}