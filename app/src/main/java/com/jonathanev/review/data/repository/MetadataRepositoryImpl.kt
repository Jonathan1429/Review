package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.MetadataRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.presentation.model.ScreenData
import java.io.File
import javax.inject.Inject

class MetadataRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val jsonManager: JsonManager,
    private val navigationPathRepository: NavigationPathRepository
) : MetadataRepository {
    override fun saveMetadata(data: ScreenData) {
        val currentPath = filePathsProvider.buildFolder(navigationPathRepository.currentPathGuides, data.name)

        if (!currentPath.exists()) {
            currentPath.mkdir()
        }

        val screenFile = File(currentPath, "screen.json")

        val initialData = ScreenData(
            name = data.name,
            description = data.description,
            imgFolder = data.imgFolder,
            color = data.color
        )

        jsonManager.write(screenFile, initialData)
    }
}