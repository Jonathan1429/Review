package com.jonathanev.review.Domain

import android.graphics.Color
import com.jonathanev.review.data.Model.prueba.FolderModel
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.R
import javax.inject.Inject

class GetAllFoldersUseCase @Inject constructor(
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(): List<FolderModel> {
        val file = filePathsProvider.fileGuides
        val files = file.listFiles() ?: return emptyList()

        return files.map { folder ->
            FolderModel(
                name = folder.name,
                description = "",
                imgFolder = R.drawable.ic_anchor_solid_full,
                color = Color.BLACK
            )
        }
        /*.sortedWith(compareBy<File>({ !it.isDirectory }, { it.name }))
        .map { it.name }*/
    }
}