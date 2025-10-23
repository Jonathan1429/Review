package com.jonathanev.review.Data.repository

import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider
): FileRepository {
    // 1. Crear el StateFlow privado y Mutable
    private val _currentPathFlow = MutableStateFlow(
        filePathsProvider.fileGuides.toString()
    )

    // 2. Exponer un StateFlow público e inmutable para la lectura
    val currentPathFlow: StateFlow<String> = _currentPathFlow.asStateFlow()

    // La variable simple 'currentPath' ya no es necesaria.

    override fun getFilesInCurrentPath(): List<File> {
        // Usar el valor actual del Flow para obtener los archivos
        val directory = File(currentPathFlow.value)
        return directory.listFiles()?.toList() ?: emptyList()
    }

    override fun setCurrentPath(path: String) {
        // 3. Actualizar el valor del MutableStateFlow para notificar a todos los escuchas
        _currentPathFlow.value = path
    }

    // Ya no necesitas override fun getCurrentPath(): String, pero si lo conservas:
    override fun getCurrentPath(): String = currentPathFlow.value
}