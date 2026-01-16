package com.jonathanev.review.data.repository

interface NavigationPathRepository {
    val currentPathGuides: String
    val currentPathImages: String

    fun next(fileName: String)
    fun back()
    fun reset()
}