package com.jonathanev.review.domain.repository

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface PathProvider {
    val currentPathFlow: StateFlow<String>
    fun setCurrentPath(path: String)
    fun getCurrentPath(): String
    fun buildTempPathFile(nameGuide: String): File
    fun buildTempPathFolder(nameGuide: String): File
    fun setBeforePath()
}