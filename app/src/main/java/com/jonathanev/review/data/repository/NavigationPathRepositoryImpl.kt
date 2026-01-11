package com.jonathanev.review.data.repository

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.NavigationPathRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationPathRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider
): NavigationPathRepository {
    private var _currentPathGuides: File = filePathsProvider.fileGuides

    override val currentPathGuides: File
        get() = _currentPathGuides

    private var _currentPathImages: File = filePathsProvider.fileImages

    override val currentPathImages: File
        get() = _currentPathImages

    override fun next(fileName: String) {
        _currentPathGuides = filePathsProvider.buildFolder(currentPathGuides, fileName)
        _currentPathImages = filePathsProvider.buildFolder(currentPathImages, fileName)
    }

    override fun back() {
        _currentPathGuides = currentPathGuides.parentFile ?: currentPathGuides
        _currentPathImages = currentPathImages.parentFile ?: currentPathImages
    }

    override fun reset() {
        _currentPathGuides = filePathsProvider.fileGuides
        _currentPathImages = filePathsProvider.fileImages
    }
}