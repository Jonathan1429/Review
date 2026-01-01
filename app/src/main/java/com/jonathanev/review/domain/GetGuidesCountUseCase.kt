package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.FileNamingRules
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject

class GetGuidesCountUseCase @Inject constructor(
    private val pathProvider: PathProvider
) {
    operator fun invoke(): Int {
        val currentPath = File(pathProvider.getCurrentPath())
        val extensionXML = FileNamingRules.XML_EXTENSION
        return currentPath.listFiles()?.filter { it.isFile && it.name.endsWith(extensionXML) }?.size ?: 0
    }
}