package com.jonathanev.review.Data.provider

import android.graphics.Color
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.DI.IoDispatcher
import com.jonathanev.review.DI.MainDispatcher
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.Model.prueba.FolderModel
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class GuiaProvider @Inject constructor(
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val fileRepository: FileRepository,
) {
    fun loadFoldersFromDevice(): List<FolderModel> {
        val currentPath = File(fileRepositoryImpl.getCurrentPath())
        val files = currentPath.listFiles() ?: emptyArray()

        return files
            .filter { it.isDirectory }
            .map { file ->
                FolderModel(
                    nameFolder = file.name,
                    description = "",
                    imgFolder = R.drawable.ic_anchor_solid_full,
                    color = Color.BLACK
                )
            }
    }

    fun loadGuidesFromDevice(): List<GuideModel>{
        val currentPath = File(fileRepositoryImpl.getCurrentPath())
        val files = currentPath.listFiles() ?: emptyArray()

        return files
            .filter { !it.isDirectory }
            .map { file ->
                GuideModel(
                    nameGuide = file.name,
                    description = ""
                )
            }
    }

    suspend fun saveImagesInDevice(images: List<QuestionContent.Image>, imagesPath: File) {
        images.map { image ->
            fileRepository.saveImage(image, imagesPath)
        }
    }
    /*var folders: List<FolderModel> = emptyList()
    var guias: List<GuideModel> = emptyList()*/
}