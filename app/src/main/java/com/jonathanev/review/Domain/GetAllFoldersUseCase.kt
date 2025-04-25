package com.jonathanev.review.Domain

import java.io.File
import javax.inject.Inject

class GetAllFoldersUseCase @Inject constructor() {

    operator fun invoke(file: File): List<String> {
        val files = file.listFiles() ?: return emptyList()

        return files
            .sortedWith(compareBy<File>({ !it.isDirectory }, { it.name }))
            .map { it.name }
    }
}