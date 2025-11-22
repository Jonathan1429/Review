package com.jonathanev.review.Domain

import com.jonathanev.review.Data.repository.FileRepositoryImpl
import java.io.File
import javax.inject.Inject

class GetNumGuidesUseCase @Inject constructor(private val fileRepositoryImpl: FileRepositoryImpl) {
    operator fun invoke(): List<Int> {
        val currentPath = File(fileRepositoryImpl.getCurrentPath())

        return currentPath
            .listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name }
            ?.map { dir -> dir.listFiles()?.size ?: 0 }
            ?: emptyList()
    }
}