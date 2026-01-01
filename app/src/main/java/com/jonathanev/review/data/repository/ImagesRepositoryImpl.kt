package com.jonathanev.review.data.repository

import android.content.Context
import android.net.Uri
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.model.QuestionContentDomain
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImagesRepository {
    override suspend fun saveImage(image: QuestionContentDomain.Image, imagesPath: File) {
        val uri = Uri.parse(image.uri)
        val fileName = image.nameFile

        val outputFile = File(imagesPath, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("No se pudo abrir imagen")
    }
}