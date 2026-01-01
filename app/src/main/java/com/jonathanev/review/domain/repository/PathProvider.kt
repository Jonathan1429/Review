package com.jonathanev.review.domain.repository

interface PathProvider {
    fun setCurrentPath(path: String)
    fun getCurrentPath(): String
}