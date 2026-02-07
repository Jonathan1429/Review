package com.jonathanev.review.data.infraestructure

import com.jonathanev.review.domain.repository.FileOutputStreamFactory
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

class RealFileOutputStreamFactory @Inject constructor(): FileOutputStreamFactory {
    override fun create(path: String): OutputStream = FileOutputStream(path)
}