package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.mapper.json.toDomain
import com.jonathanev.review.data.mapper.json.toDto
import com.jonathanev.review.data.model.AttributesFolderDto
import com.jonathanev.review.data.model.json.ScreenDataDto
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
import kotlinx.coroutines.withContext
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

    override suspend fun deleteFolder(): Boolean = withContext(Dispatchers.IO) {
        val pathGuides = File(filePathResolver.mapToFolderPath(PathKind.GUIAS).value)
        val pathImages = File(filePathResolver.mapToFolderPath(PathKind.IMAGENES).value)

        val guidesDeleted = deleteSafely(pathGuides)
        val imagesDeleted = deleteSafely(pathImages)

        val isSuccess = guidesDeleted && imagesDeleted

        if (isSuccess) {
            refreshFolders.value = System.currentTimeMillis()
        }

        isSuccess
    }

    private fun deleteSafely(folder: File): Boolean {
        if (!folder.exists()) return true
        return folder.deleteRecursively()
    }

    override suspend fun createFolder(
        data: FolderScreenInfoDomain
    ): Boolean = withContext(Dispatchers.IO) {
        val guidesPath = File(filePathResolver.mapToFolderPath(PathKind.GUIAS).value)
        val imagesPath = File(filePathResolver.mapToFolderPath(PathKind.IMAGENES).value)

        val guidesCreated = guidesPath.ensureDirectory()
        val imagesCreated = imagesPath.ensureDirectory()

        if (guidesCreated && imagesCreated) {
            refreshFolders.value = System.currentTimeMillis()
            true
        } else {
            false
        }
    }

    override suspend fun renameFolder(
        oldName: String,
        newName: String,
        data: FolderScreenInfoDomain
    ): Boolean = withContext(Dispatchers.IO) {
        val rootPath = navigationPathRepository.getRootGuides().value
        val oldFolder = File(rootPath, oldName)
        val newFolder = File(rootPath, newName)

        val isRenamed = if (oldName != newName) {
            oldFolder.renameTo(newFolder)
        } else {
            true
        }

        if (isRenamed) {
            val screenFile = File(newFolder, "screen.json")
            val screenDataDto = data.toDto()
            jsonManager.write(
                screenFile.path,
                ScreenDataDto.serializer(),
                screenDataDto
            )
            refreshFolders.value = System.currentTimeMillis()
            true
        } else {
            false
        }
    }

    private fun File.ensureDirectory(): Boolean = exists() || mkdirs()

    override fun getFolders(): Flow<List<FolderDomainModel>> {
        return refreshFolders.map {
            val rootPath = navigationPathRepository.getRootGuides().value
            val rootDir = File(rootPath)

            if (!rootDir.exists() || !rootDir.isDirectory) {
                return@map emptyList()
            }

            rootDir.listFiles()
                ?.asSequence()
                ?.filter { it.isDirectory }
                ?.sortedBy { it.name }
                ?.map { folder ->
                    val attributes = loadFolderAttributes(folder.name)

                    FolderDomainModel(
                        folder = FolderAttributesDomain(
                            name = attributes.name,
                            imgFolder = attributes.imgFolder,
                            color = attributes.color
                        ),
                        numGuides = countGuidesInFolder(folder)
                    )
                }
                ?.toList()
                ?: emptyList()
        }.flowOn(Dispatchers.IO)
    }

    private fun countGuidesInFolder(folder: File): Int {
        val children = folder.listFiles().orEmpty()

        val guidesV1 = children.count { it.isXmlFile() }

        val guidesV2 = children.asSequence()
            .filter { it.isDirectory }
            .sumOf { subFolder ->
                subFolder.listFiles().orEmpty().count { it.isXmlFile() }
            }

        return guidesV1 + guidesV2
    }

    private fun File.isXmlFile(): Boolean =
        isFile && extension.equals(Extensions.XML_EXTENSION, ignoreCase = true)
}