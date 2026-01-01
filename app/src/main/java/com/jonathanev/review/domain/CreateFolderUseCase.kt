package com.jonathanev.review.domain

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.presentation.model.ScreenData
import java.io.File
import javax.inject.Inject

class CreateFolderUseCase @Inject constructor(
    private val jsonManager: JsonManager
) {
    operator fun invoke(data: ScreenData, dir: File) {
        val screenFile = File(dir, "screen.json")

        val initialData = ScreenData(
            name = data.name,
            description = data.description,
            imgFolder = data.imgFolder,
            color = data.color
        )

        jsonManager.write(screenFile, initialData)
    }
}