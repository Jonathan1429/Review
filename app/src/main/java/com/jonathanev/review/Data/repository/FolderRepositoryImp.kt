package com.jonathanev.review.Data.repository

import android.graphics.Color
import com.jonathanev.review.Data.JsonManager
import com.jonathanev.review.Data.Model.prueba.FolderModel
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