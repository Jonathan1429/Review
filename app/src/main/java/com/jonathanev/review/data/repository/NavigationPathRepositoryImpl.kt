package com.jonathanev.review.data.repository

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationPathRepositoryImpl @Inject constructor(
    private val pathProvider: PathProvider,
    private val filePathsProvider: FilePathsProvider
): NavigationPathRepository {
    private var _currentPath: File = pathProvider.guidesRoot

    override val currentPath: File
        get() = _currentPath

    override fun next(fileName: String) {
        _currentPath = filePathsProvider.buildFolder(currentPath, fileName)
    }

    override fun back() {
        _currentPath = _currentPath.parentFile ?: _currentPath
    }

    override fun reset() {
        _currentPath = pathProvider.guidesRoot
    }
}