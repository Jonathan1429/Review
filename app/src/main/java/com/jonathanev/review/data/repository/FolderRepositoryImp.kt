package com.jonathanev.review.data.repository

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.FolderFileModel
import java.io.File
import javax.inject.Inject

class FolderRepositoryImp @Inject constructor(
    private val jsonManager: JsonManager,
    private val pathProvider: PathProvider
) : FolderRepository {
    override fun getAttributesFolder(folderPath: File): FolderDomainModel {
        val nameFolder = folderPath.toString().substringAfterLast("/")

        val file = File(folderPath, "screen.json")
        if (!file.exists()) return FolderDomainModel(
            name = nameFolder
        )

        val fileModel = jsonManager.read<FolderFileModel>(file)
        return FolderDomainModel(name = fileModel.name)
    }

    override fun getFolders(): List<FolderDomainModel> {
        val file = File(pathProvider.getCurrentPath())

        return file.listFiles()
            ?.filter { it.isDirectory }
            ?.map { item ->
                FolderDomainModel(
                    name = item.name
                )
            }
            ?: emptyList()
    }
}