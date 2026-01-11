package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.mapper.toColorType
import com.jonathanev.review.data.mapper.toIconType
import com.jonathanev.review.data.model.AttributesFolderJson
import com.jonathanev.review.data.provider.DefaultFolderAttributesProvider
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.presentation.event.UIStopEvent
import java.io.File
import javax.inject.Inject

class FolderRepositoryImp @Inject constructor(
    private val jsonManager: JsonManager,
    private val navigationPathRepository: NavigationPathRepository,
    private val defaultFolderAttributesProvider: DefaultFolderAttributesProvider,
    private val filePathsProvider: FilePathsProvider
) : FolderRepository {
    private fun loadFolderAttributes(nameFolder: String): AttributesFolderJson {
        val currentPath =
            filePathsProvider.buildFolder(navigationPathRepository.currentPathGuides, nameFolder)

        val file = File(currentPath, "screen.json")
        if (!file.exists()) return defaultFolderAttributesProvider.default(nameFolder)

        val fileModel = jsonManager.read<AttributesFolderJson>(file)
        return AttributesFolderJson(
            name = fileModel.name,
            imgFolder = fileModel.imgFolder,
            color = fileModel.color,
        )
    }

    override fun deleteFolder(nameFolder: String): UIStopEvent {
        val pathGuides =
            filePathsProvider.buildFolder(navigationPathRepository.currentPathGuides, nameFolder)
        val pathImages =
            filePathsProvider.buildImage(navigationPathRepository.currentPathImages, nameFolder)

        return if (pathGuides.deleteRecursively()) {
            pathImages.deleteRecursively()
            UIStopEvent.DeleteFolderSuccess("Se ha borrado la carpeta correctamente")
        } else {
            UIStopEvent.ShowMessage("No se pudo borrar la carpeta correctamente")
        }
    }

    override fun getFolders(): List<FolderDomainModel> {
        return navigationPathRepository.currentPathGuides.listFiles()?.filter { it.isDirectory }
            ?.map { item ->
                val numGuidesCurrentFolder =
                    item.listFiles()?.filter { it.name != "screen.json" }?.size ?: 0
                val attributes = loadFolderAttributes(item.name)
                FolderDomainModel(
                    folder = FolderAttributesDomain(
                        name = attributes.name,
                        imgFolder = attributes.imgFolder.toIconType(),
                        color = attributes.color.toColorType()
                    ),
                    numGuides = numGuidesCurrentFolder
                )
            } ?: emptyList()
    }
}