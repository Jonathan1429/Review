package com.jonathanev.review.Domain

interface FileHelper {
    fun exists(path: String): Boolean
}