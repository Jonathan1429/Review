package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.domain.repository.MetadataRepository
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.presentation.model.ScreenData
import java.io.File
import javax.inject.Inject

class MetadataRepositoryImpl @Inject constructor(
    private val pathProvider: PathProvider,
    private val jsonManager: JsonManager
) : MetadataRepository {
    override fun saveMetadata(data: ScreenData) {
        /*val currentPath = File(pathProvider.getCurrentPath())

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

        jsonManager.write(screenFile, initialData)*/
    }
}