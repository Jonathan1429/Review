package com.jonathanev.review.Domain

import java.io.File
import javax.inject.Inject

class GetAllFoldersUseCase @Inject constructor() {

    operator fun invoke(file: File): List<String> =
        file.listFiles()?.filter { it.isDirectory }?.map { it.name }?.sorted() ?: emptyList()
}