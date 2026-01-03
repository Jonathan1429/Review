package com.jonathanev.review.data.provider

import com.jonathanev.review.domain.repository.PathProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathProviderImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
) : PathProvider {
    private val _currentPathFlow = MutableStateFlow(
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
    }
}