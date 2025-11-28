package com.jonathanev.review.Data.provider

import android.graphics.Color
import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.Model.prueba.FolderModel
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.R
import java.io.File
import javax.inject.Inject

class GuiaProvider @Inject constructor(
    private val fileRepositoryImpl: FileRepositoryImpl
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

    /*var folders: List<FolderModel> = emptyList()
    var guias: List<GuideModel> = emptyList()*/
}