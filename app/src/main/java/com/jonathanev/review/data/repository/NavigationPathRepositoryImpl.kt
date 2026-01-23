package com.jonathanev.review.data.repository

import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.repository.NavigationPathRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationPathRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider
): NavigationPathRepository {
    private var _currentPathGuides: GuidePath = GuidePath(filePathsProvider.fileGuides)

    override val currentPathGuides: GuidePath
        get() = _currentPathGuides

    private var _currentPathImages: GuidePath = GuidePath(filePathsProvider.fileImages)

    override val currentPathImages: GuidePath
        get() = _currentPathImages

    override fun next(fileName: String) {
        _currentPathGuides = GuidePath(filePathsProvider.buildFolder(currentPathGuides.value, fileName))
        _currentPathImages = GuidePath(filePathsProvider.buildFolder(currentPathImages.value, fileName))
    }

    override fun back() {
        _currentPathGuides = GuidePath(File(currentPathGuides.value).parentFile?.path ?: currentPathGuides.value)
        _currentPathImages = GuidePath(File(currentPathImages.value).parentFile?.path ?: currentPathImages.value)
    }

    override fun reset() {
        _currentPathGuides = GuidePath(filePathsProvider.fileGuides)
        _currentPathImages = GuidePath(filePathsProvider.fileImages)
    }
}