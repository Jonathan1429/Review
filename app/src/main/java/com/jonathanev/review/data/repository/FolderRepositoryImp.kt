package com.jonathanev.review.data.repository

import android.graphics.Color
import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.Model.prueba.FolderModel
import com.jonathanev.review.R
import java.io.File
import javax.inject.Inject

class FolderRepositoryImp @Inject constructor(
    private val jsonManager: JsonManager
) : FolderRepository {
    override fun getAttributesFolder(folderPath: File): FolderModel {
        val nameFolder = folderPath.toString().substringAfterLast("/")

        val file = File(folderPath, "screen.json")
        if (!file.exists()) return FolderModel(
            name = nameFolder,
            description = "",
            imgFolder = R.drawable.ic_anchor_solid_full,
            color = Color.BLACK
        )

        return jsonManager.read<FolderModel>(file)
    }
}