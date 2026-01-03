package com.jonathanev.review.data.provider

import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.domain.DirectoryManager
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.repository.FileNamingRules
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

class DirectoryManagerImpl @Inject constructor(
    private val pathProvider: PathProvider
) : DirectoryManager {
    override fun prepareCleanDirectory(path: String, isNewFile: Boolean) {
        when {
            isNewFile -> {
                val directory = File(path)
                if (directory.exists()) {
                    directory.deleteRecursively()
                }
                directory.mkdir()
            }

            else -> {
                val basePath =
                    pathProvider.getCurrentPath().replace(FileNamingRules.XML_EXTENSION, "")
                val imagesFolder =
                    File(basePath.replace(StorageFolders.GUIAS, StorageFolders.IMAGENES))

                if (!imagesFolder.exists()) {
                    imagesFolder.mkdir()
                }
            }
        }
    }

    override fun existPath(path: String): Boolean {
        return File(path).exists()
    }

    override fun moveImagesV1(
        listImages: List<QuestionContentDomain.Image>,
        nameGuide: String
    ) {
        val imagesFolder = pathProvider.buildTempPathFile(nameGuide)

        val currentDeviceNames =
            imagesFolder.parentFile?.listFiles()?.map { it.name }?.toSet() ?: emptySet()

        listImages.filter { it.nameFile in currentDeviceNames }.forEach { image ->
            val destination = File(imagesFolder, image.nameFile)

            Files.move(
                Paths.get(image.uri),
                Paths.get(destination.path),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }

    override fun deleteLeftoverImagesInDevice(
        nameGuide: String,
        listImages: List<QuestionContentDomain.Image>
    ) {
        val imagesFolder = pathProvider.buildTempPathFile(nameGuide)

        // Borrar imagenes que ya no estén en el XML pero si en el dispositivo
        val currentDeviceNames = imagesFolder.listFiles()?.map { it.name }?.toSet() ?: emptySet()
        val listDelete = currentDeviceNames - listImages.map { it.nameFile }.toSet()

        listDelete.forEach { image ->
            val destination = File(imagesFolder, image)
            if (destination.exists() && destination.isFile) {
                destination.delete()
            }
        }
    }
}