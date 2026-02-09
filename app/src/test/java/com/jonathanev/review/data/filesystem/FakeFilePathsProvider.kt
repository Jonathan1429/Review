package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.provider.FilePathsProvider

class FakeFilePathsProvider(
    override val fileGuides: String
) : FilePathsProvider {
    override val fileImages: String
        get() = error("fileImages no es usado en este test")

    override fun buildGuide(base: String, file: String): String {
        error("buildGuide no es usado en este test")
    }

    override fun buildImage(base: String, image: String): String {
        error("buildImage no es usado en este test")
    }

    override fun buildFolderGuide(
        base: String,
        folder: String,
        file: String
    ): String {
        return "$base/$folder/$file"
    }

    override fun buildFolder(base: String, folder: String): String {
        error("buildFolder no es usado en este test")
    }
}
