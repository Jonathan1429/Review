package com.jonathanev.review.domain.repository

import java.io.File

interface NavigationPathRepository {
    val currentPath: File

    fun next(fileName: String)
    fun back()
    fun reset()
}