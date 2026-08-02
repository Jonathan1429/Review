package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.mapper.json.toDomain
import com.jonathanev.review.data.model.AttributesFolderDto
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.factory.DefaultFolderAttributesProvider
import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val jsonManager: JsonManager,
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider,
    private val filePathResolver: FilePathResolver
) : FolderRepository {
    private fun loadFolderAttributes(nameFolder: String): FolderAttributesDomain {
        val currentPath =
            filePathsProvider.buildFolder(
                navigationPathRepository.getRootGuides().value,
                nameFolder
            )

        val file = File(currentPath, "screen.json")
        if (!file.exists()) return DefaultFolderAttributesProvider.default(nameFolder)

        val attributesFolderDto = jsonManager.read(file.path, AttributesFolderDto.serializer())
        val attributesFolderDomain = attributesFolderDto.toDomain()

        return attributesFolderDomain
    }

    override suspend fun deleteFolder(): Boolean {
        val pathGuides =
            File(filePathResolver.mapToFolderPath(PathKind.GUIAS).value)
        val pathImages =
            File(filePathResolver.mapToFolderPath(PathKind.IMAGENES).value)

        return if (pathGuides.deleteRecursively()) {
            pathImages.deleteRecursively()
            true
        } else {
            false
        }
    }

    override fun getFolders(): Flow<List<FolderDomainModel>> = flow {
        val rootPath = navigationPathRepository.getRootGuides().value
        val folderList = File(rootPath).listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name }
            ?.map { item ->
                val guidesV1 =
                    item.listFiles()?.filter { file ->
                        file.isFile && file.extension == Extensions.XML_EXTENSION
                    }?.size ?: 0

                val guidesV2 = item.listFiles()?.filter { it.isDirectory }?.sumOf { folder ->
                    folder.listFiles()
                        ?.filter { it.isFile && it.extension == Extensions.XML_EXTENSION }?.size
                        ?: 0
                } ?: 0

                val attributes = loadFolderAttributes(item.name)
                FolderDomainModel(
                    folder = FolderAttributesDomain(
                        name = attributes.name,
                        imgFolder = attributes.imgFolder,
                        color = attributes.color
                    ),
                    numGuides = guidesV1 + guidesV2
                )
            } ?: emptyList()

        emit(folderList)
    }
}