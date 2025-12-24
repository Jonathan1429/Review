package com.jonathanev.review.Domain

import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class GetGuidesCountUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(): Int {
        val currentPath = File(fileRepository.getCurrentPath())
        //cambiar ruta
        return currentPath.listFiles()?.filter { it.name != "screen.json" }?.size ?: 0
    }
}