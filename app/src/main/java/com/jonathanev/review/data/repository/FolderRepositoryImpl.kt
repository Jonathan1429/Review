package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.mapper.json.toDomain
import com.jonathanev.review.data.model.AttributesFolderDto
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.factory.DefaultFolderAttributesProvider
import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.model.FolderScreenInfoDomain
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val jsonManager: JsonManager,
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider,
    private val filePathResolver: FilePathResolver
) : FolderRepository {
    private val refreshFolders = MutableStateFlow(System.currentTimeMillis())

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
            refreshFolders.value = System.currentTimeMillis()
            true
        } else {
            false
        }
    }

    override suspend fun createFolder(data: FolderScreenInfoDomain): Boolean {
        val guidesPath =
            File(filePathResolver.mapToFolderPath(PathKind.GUIAS).value)
        val imagesPath =
            File(filePathResolver.mapToFolderPath(PathKind.IMAGENES).value)

        if (!guidesPath.exists()) {
            val pathGuides = guidesPath.mkdir()
            if (!pathGuides) return false
        }

        if (!imagesPath.exists()) {
            val pathImages = imagesPath.mkdir()
            if (!pathImages) return false
        }

        refreshFolders.value = System.currentTimeMillis()
        return true
    }

    override fun getFolders(): Flow<List<FolderDomainModel>> {
        return refreshFolders.map {
            val rootPath = navigationPathRepository.getRootGuides().value
            val rootDir = File(rootPath)

            if (!rootDir.exists() || !rootDir.isDirectory) {
                return@map emptyList()
            }

            rootDir.listFiles()
                ?.filter { it.isDirectory }
                ?.sortedBy { it.name }
                ?.map { item ->
                    val itemChildren = item.listFiles() ?: emptyArray()

                    val guidesV1 = itemChildren.count { file ->
                        file.isFile && file.extension == Extensions.XML_EXTENSION
                    }

                    val guidesV2 = itemChildren
                        .filter { it.isDirectory }
                        .sumOf { subFolder ->
                            subFolder.listFiles()?.count { file ->
                                file.isFile && file.extension == Extensions.XML_EXTENSION
                            } ?: 0
                        }

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
        }.flowOn(Dispatchers.IO)
    }
}