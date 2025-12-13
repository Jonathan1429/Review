package com.jonathanev.review.Data.repository

import android.content.Context
import android.net.Uri
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.repository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    @ApplicationContext private val context: Context,
    private val dataStore: DataStoreManager
    //private val getAllGuiasUseCase: GetAllGuiasUseCase
) : FileRepository {
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

    override suspend fun saveImage(image: QuestionContent.Image, imagesPath: File) {
        val uri = Uri.parse(image.decodedPath)

        val count = dataStore.getCountImage().first()
        val fileName = "$count.png"

        if (!imagesPath.exists()) {
            imagesPath.mkdirs()
        }

        val outputFile = File(imagesPath, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo abrir imagen")

        dataStore.setIncrementCounter()
    }
}