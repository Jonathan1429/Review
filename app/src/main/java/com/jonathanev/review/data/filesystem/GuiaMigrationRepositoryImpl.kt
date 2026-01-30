package com.jonathanev.review.data.filesystem

import android.content.Context
import android.util.Log
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.repository.GuiaMigrationRepository
import com.jonathanev.review.domain.result.MigrationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class GuiaMigrationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GuiaMigrationRepository {
    override fun moveGuides(): MigrationResult {
        val moved = mutableListOf<String>()
        val failed = mutableListOf<String>()

        val rootPath = File(context.filesDir, StorageFolders.GUIAS)
        if (!rootPath.exists()) return MigrationResult(emptyList(), emptyList())

        val guidesInDevice = rootPath.listFiles()
            ?.filter { it.isFile && it.extension == Extensions.XML_EXTENSION } ?: emptyList()
        if (guidesInDevice.isEmpty()) return MigrationResult(emptyList(), emptyList())

        val otrosDir = File(rootPath, StorageFolders.OTROS)
        val isFolderReady = otrosDir.exists() || otrosDir.mkdirs()

        if (!isFolderReady) {
            Log.e("MIGRATION", "No se pudo preparar la carpeta de destino.")
            return MigrationResult(emptyList(), guidesInDevice.map { it.name })
        }

        guidesInDevice.forEach { file ->
            try {
                val newPath = File(otrosDir, file.name)
                if (newPath.exists()){
                    Log.i("Migration: ", "Archivo existente: ${file.name}")
                } else {
                    val success = file.renameTo(newPath)
                    if (success) {
                        moved.add(file.name)
                    } else {
                        failed.add(file.name)
                    }
                }
            } catch (e: Exception) {
                failed.add(file.name)
            }

        }

        return MigrationResult(moved, failed)
    }
}