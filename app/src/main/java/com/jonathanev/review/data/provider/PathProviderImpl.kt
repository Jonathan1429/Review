package com.jonathanev.review.data.provider

import com.jonathanev.review.domain.repository.PathProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathProviderImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
) : PathProvider {
    private val _currentPathFlow = MutableStateFlow(
        filePathsProvider.fileGuides.toString()
    )

    val currentPathFlow: StateFlow<String> = _currentPathFlow.asStateFlow()

    override fun setCurrentPath(path: String) {
        // 3. Actualizar el valor del MutableStateFlow para notificar a todos los escuchas
        _currentPathFlow.value = path
    }

    // Ya no necesitas override fun getCurrentPath(): String, pero si lo conservas:
    override fun getCurrentPath(): String = currentPathFlow.value
}