package com.jonathanev.review.domain.provider

interface FilePathsProvider {
    val fileGuides: String
    val fileImages: String
    fun buildGuide(base: String, file: String): String
    fun buildImage(base: String, image: String): String
    fun buildFolderGuide(base: String, folder: String, file: String): String
    fun buildFolder(base: String, folder: String): String
}