package com.jonathanev.review.Data.repository

import com.jonathanev.review.Domain.FileHelper
import java.io.File
import javax.inject.Inject

class FileHelperImpl @Inject constructor(): FileHelper {
    override fun exists(path: String) = File(path).exists()
}