package com.jonathanev.review.presentation.navigation

interface NavigationPathRepository {
    val currentPathGuides: String
    val currentPathImages: String

    fun next(fileName: String)
    fun back()
    fun reset()
}