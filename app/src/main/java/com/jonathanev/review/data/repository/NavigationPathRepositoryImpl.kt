package com.jonathanev.review.data.repository

import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationPathRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider
): NavigationPathRepository {
    private var _currentPathGuides: String = filePathsProvider.fileGuides

    override val currentPathGuides: String
        get() = _currentPathGuides

    private var _currentPathImages: String = filePathsProvider.fileImages

    override val currentPathImages: String
        get() = _currentPathImages

    override fun next(fileName: String) {
        _currentPathGuides = filePathsProvider.buildFolder(currentPathGuides, fileName)
        _currentPathImages = filePathsProvider.buildFolder(currentPathImages, fileName)
    }

    override fun back() {
        _currentPathGuides = File(currentPathGuides).parentFile?.path ?: currentPathGuides
        _currentPathImages = File(currentPathImages).parentFile?.path ?: currentPathImages
    }

    override fun reset() {
        _currentPathGuides = filePathsProvider.fileGuides
        _currentPathImages = filePathsProvider.fileImages
    }
}