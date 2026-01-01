package com.jonathanev.review.domain

interface FileHelper {
    fun exists(path: String): Boolean
}