package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.filesystem.FilePathsProviderImpl
import com.jonathanev.review.data.mapper.json.toDomain
import com.jonathanev.review.data.model.AttributesFolderDto
import com.jonathanev.review.domain.factory.DefaultFolderAttributesProvider
import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import java.io.File
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val jsonManager: JsonManager,
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProviderImpl: FilePathsProviderImpl
) : FolderRepository {
    private fun loadFolderAttributes(nameFolder: String): FolderAttributesDomain {
        val currentPath =
            filePathsProviderImpl.buildFolder(navigationPathRepository.getPathGuides().value, nameFolder)

        val file = File(currentPath, "screen.json")
        if (!file.exists()) return DefaultFolderAttributesProvider.default(nameFolder)

        val attributesFolderDto = jsonManager.read(file.path, AttributesFolderDto.serializer())
        val attributesFolderDomain = attributesFolderDto.toDomain()

        return attributesFolderDomain
    }

    override fun deleteFolder(nameFolder: String): Boolean {
        val pathGuides =
            filePathsProviderImpl.buildFolder(navigationPathRepository.getPathGuides().value, nameFolder)
        val pathImages =
            filePathsProviderImpl.buildFolder(navigationPathRepository.getPathImages().value, nameFolder)

        return if (File(pathGuides).deleteRecursively()) {
            File(pathImages).deleteRecursively()
            true
        } else {
            false
        }
    }

    override fun getFolders(): List<FolderDomainModel> {
        return File(navigationPathRepository.getPathGuides().value).listFiles()?.filter { it.isDirectory }
            ?.map { item ->
                val numGuidesCurrentFolder =
                    item.listFiles()?.filter { it.name != "screen.json" }?.size ?: 0
                val attributes = loadFolderAttributes(item.name)
                FolderDomainModel(
                    folder = FolderAttributesDomain(
                        name = attributes.name,
                        imgFolder = attributes.imgFolder,
                        color = attributes.color
                    ),
                    numGuides = numGuidesCurrentFolder
                )
            } ?: emptyList()
    }
}