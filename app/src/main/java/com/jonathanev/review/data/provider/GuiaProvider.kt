package com.jonathanev.review.data.provider

import com.jonathanev.review.data.Model.prueba.QuestionContent
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class GuiaProvider @Inject constructor(
    private val fileRepository: FileRepository,
) {
    fun loadGuideFiles(): List<File> {
        val currentPath = File(fileRepository.getCurrentPath())
        return currentPath.listFiles()
            ?.filter { !it.isDirectory }
            ?: emptyList()
    }

    fun loadFolders(): List<File>{
        val currentPath = File(fileRepository.getCurrentPath())
        return currentPath.listFiles()
            ?.filter { it.isDirectory }
            ?: emptyList()
    }

    suspend fun saveImagesInDevice(images: List<QuestionContent.Image>, imagesPath: File) {
        images.map { image ->
            fileRepository.saveImage(image, imagesPath)
        }
    }
    /*var folders: List<FolderModel> = emptyList()
    var guias: List<GuideModel> = emptyList()*/
}