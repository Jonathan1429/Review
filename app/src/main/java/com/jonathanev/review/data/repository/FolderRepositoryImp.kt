package com.jonathanev.review.data.repository

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.FolderFileModel
import com.jonathanev.review.domain.model.FolderWithNumGuidesDomainModel
import com.jonathanev.review.domain.repository.FileExplorerRepository
import java.io.File
import javax.inject.Inject

class FolderRepositoryImp @Inject constructor(
    private val jsonManager: JsonManager,
    private val fileExplorerRepository: FileExplorerRepository
) : FolderRepository {
    /*override fun getAttributesFolder(folderPath: File): FolderDomainModel {
        val nameFolder = folderPath.toString().substringAfterLast("/")

        val file = File(folderPath, "screen.json")
        if (!file.exists()) return FolderDomainModel(
            name = nameFolder
        )

        val fileModel = jsonManager.read<FolderFileModel>(file)
        return FolderDomainModel(name = fileModel.name)
    }*/

    override fun getFolders(): List<FolderWithNumGuidesDomainModel> {
        return fileExplorerRepository.listCurrent().filter { it.isDirectory }.map { item ->
            val numGuidesCurrentFolder = item.listFiles()?.filter { it.name != "screen.json" }?.size ?: 0
            FolderWithNumGuidesDomainModel(
                FolderDomainModel(item.name),
                numGuidesCurrentFolder
            )
        }
    }
}