package com.jonathanev.review.Domain

import java.io.File
import javax.inject.Inject

class setChangePathUseCase @Inject constructor() {
    operator fun invoke(folderName: String): File {
        return File("/data/data/com.jonathanev.review/files/$folderName")
    }
}