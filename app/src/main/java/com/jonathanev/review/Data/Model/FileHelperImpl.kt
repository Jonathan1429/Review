package com.jonathanev.review.Data.Model

import com.jonathanev.review.Data.Interface.FileHelper
import java.io.File
import javax.inject.Inject

class FileHelperImpl @Inject constructor(): FileHelper {
    override fun exists(path: String) = File(path).exists()
}