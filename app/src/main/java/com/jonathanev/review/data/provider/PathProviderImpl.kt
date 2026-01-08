package com.jonathanev.review.data.provider

import com.jonathanev.review.data.storage.StorageFolders.GUIAS
import com.jonathanev.review.data.storage.StorageFolders.IMAGENES
import com.jonathanev.review.domain.model.GuidePathContext
import com.jonathanev.review.domain.model.GuidePathTarget
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathProviderImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
) : PathProvider {
    override val guidesRoot: File
        get() = filePathsProvider.fileGuides

    // Entrar a carpeta en especifico
    override fun resolveFoldersPath(folderId: String): File {
        return filePathsProvider.buildFolder(guidesRoot, folderId)
    }

    /*override fun resolveGuidePath(context: GuidePathContext): File {
        /*return when (context.version) {
            VERSION1 -> resolveV1(this.guidesRoot, context)
            else -> resolveV2(this.guidesRoot, context)
        }*/
    }*/

    private fun resolveV1(base: File, context: GuidePathContext): File {
        return when (context.target) {
            GuidePathTarget.GUIDE_FILE ->
                filePathsProvider.buildFile(base, context.guideName)

            GuidePathTarget.IMAGES_FOLDER ->
                File(
                    filePathsProvider
                        .buildFolder(base, context.guideName)
                        .path
                        .replace(GUIAS, IMAGENES)
                )
        }
    }

    private fun resolveV2(base: File, context: GuidePathContext): File {
        return when (context.target) {
            GuidePathTarget.GUIDE_FILE ->
                filePathsProvider.buildFolderFile(
                    base,
                    context.guideName,
                    context.guideName
                )

            GuidePathTarget.IMAGES_FOLDER ->
                File(
                    filePathsProvider.buildFolderFile(
                        base,
                        context.guideName,
                        context.guideName
                    ).path.replace(GUIAS, IMAGENES)
                )
        }
    }

    /*private val _currentPathFlow = MutableStateFlow(
        filePathsProvider.fileGuides.toString()
    )

    override val currentPathFlow: StateFlow<String> = _currentPathFlow.asStateFlow()

    override fun setCurrentPath(path: String) {
        // 3. Actualizar el valor del MutableStateFlow para notificar a todos los escuchas
        _currentPathFlow.value = path
    }

    // Ya no necesitas override fun getCurrentPath(): String, pero si lo conservas:
    override fun getCurrentPath(): String = currentPathFlow.value

    override fun buildTempPathFile(nameGuide: String): File {
        return filePathsProvider.buildFile(File(getCurrentPath()), nameGuide)
    }

    override fun buildTempPathImages(nameGuide: String): File {
        val pathFile = filePathsProvider.buildFolder(File(getCurrentPath()), nameGuide)
        return File(pathFile.path.replace(GUIAS, IMAGENES))
    }

    override fun resolveGuidePath(nameGuide: GuidePathContext) {
        when (nameGuide.version) {
            VERSION1 -> {
                val pathFile =
                    filePathsProvider.buildFile(File(getCurrentPath()), nameGuide.guideName).path
                setCurrentPath(pathFile)
            }

            // V2 o superior
            else -> {
                val pathFile = filePathsProvider.buildFolderFile(
                    File(getCurrentPath()),
                    nameGuide.guideName,
                    nameGuide.guideName
                ).path
                setCurrentPath(pathFile)
            }
        }
    }

    override fun setBeforePath() {
        val beforePath = filePathsProvider.beforePath(File(getCurrentPath()))
        setCurrentPath(beforePath.path)
    }

    override fun buildTempPathFolder(nameGuide: String): File {
        return File(
            filePathsProvider.buildFolder(
                File(getCurrentPath()),
                nameGuide
            ).path.replace("guias", "imagenes")
        )
    }*/
}