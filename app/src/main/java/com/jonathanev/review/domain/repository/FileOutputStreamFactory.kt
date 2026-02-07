package com.jonathanev.review.domain.repository

import java.io.OutputStream

interface FileOutputStreamFactory {
    fun create(path: String): OutputStream
}