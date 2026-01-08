package com.jonathanev.review.data.repository

import com.jonathanev.review.domain.repository.FileExplorerRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import java.io.File
import javax.inject.Inject

class FileExplorerRepositoryImpl @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) : FileExplorerRepository {
    override fun listCurrent(): List<File> {
        return navigationPathRepository.currentPath.listFiles()?.toList() ?: emptyList()
    }
}