package com.jonathanev.review.data.provider

import com.jonathanev.review.domain.FileHelper
import java.io.File
import javax.inject.Inject

class FileHelperImpl @Inject constructor(): FileHelper {
    override fun exists(path: String) = File(path).exists()
}