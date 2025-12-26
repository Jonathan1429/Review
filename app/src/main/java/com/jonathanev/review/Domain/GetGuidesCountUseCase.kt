package com.jonathanev.review.Domain

import com.jonathanev.review.Domain.repository.FileNamingRules
import com.jonathanev.review.Domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class GetGuidesCountUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(): Int {
        val currentPath = File(fileRepository.getCurrentPath())
        val extensionXML = FileNamingRules.XML_EXTENSION
        return currentPath.listFiles()?.filter { it.isFile && it.name.endsWith(extensionXML) }?.size ?: 0
    }
}