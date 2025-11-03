package com.jonathanev.review.Data.repository

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.GetAllGuiasUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    //private val getAllGuiasUseCase: GetAllGuiasUseCase
): FileRepository {
    // 1. Crear el StateFlow privado y Mutable
    private val _currentPathFlow = MutableStateFlow(
        filePathsProvider.fileGuides.toString()
    )

    // 2. Exponer un StateFlow público e inmutable para la lectura
    val currentPathFlow: StateFlow<String> = _currentPathFlow.asStateFlow()

    /*// 1. Crear el StateFlow privado y Mutable
    private val _currentFilesFlow = MutableStateFlow(
        emptyList<GuiaModel>()
    )
    // 2. Exponer un StateFlow público e inmutable para la lectura
    private val currentFilesFlow: StateFlow<List<GuiaModel>> = _currentFilesFlow.asStateFlow()
    override fun setFilesInCurrentPath() {
        val guides = getAllGuiasUseCase.invoke(File(currentPathFlow.value))
        _currentFilesFlow.value = guides
    }

    override fun getFilesInCurrentPath(): List<GuiaModel> = currentFilesFlow.value*/

    override fun setCurrentPath(path: String) {
        // 3. Actualizar el valor del MutableStateFlow para notificar a todos los escuchas
        _currentPathFlow.value = path
    }

    // Ya no necesitas override fun getCurrentPath(): String, pero si lo conservas:
    override fun getCurrentPath(): String = currentPathFlow.value
}