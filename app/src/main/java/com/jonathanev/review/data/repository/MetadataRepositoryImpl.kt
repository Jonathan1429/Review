package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.data.model.json.ScreenDataDto
import com.jonathanev.review.domain.repository.MetadataRepository
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import java.io.File
import javax.inject.Inject

class MetadataRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val jsonManager: JsonManager,
    private val navigationPathRepository: NavigationPathRepository
) : MetadataRepository {
    override fun saveMetadata(data: ScreenDataDto) {
        val guidesPath =
            File(filePathsProvider.buildFolder(navigationPathRepository.currentPathGuides, data.name))
        val imagesPath =
            File(filePathsProvider.buildFolder(navigationPathRepository.currentPathImages, data.name))
        if (!guidesPath.exists()) {
            guidesPath.mkdir()
        }

        if (!imagesPath.exists()){
            imagesPath.mkdir()
        }

        val screenFile = File(guidesPath, "screen.json")

        jsonManager.write(screenFile, ScreenDataDto.serializer(), data)
    }
}