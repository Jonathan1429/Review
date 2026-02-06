package com.jonathanev.review.data.repository

import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationPathRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider
) : NavigationPathRepository {
    override fun getRootGuides() = GuidePath(filePathsProvider.fileGuides)
    override fun getRootImages() = GuidePath(filePathsProvider.fileImages)

    override fun next(current: RelativeGuidePath, fileName: String) = RelativeGuidePath(
        value = if (current.value.isBlank()) fileName else "${current.value}/$fileName"
    )

    override fun back(current: RelativeGuidePath): RelativeGuidePath =
        RelativeGuidePath(
            current.value.trimEnd('/')
                .substringBeforeLast("/", "")
        )
}