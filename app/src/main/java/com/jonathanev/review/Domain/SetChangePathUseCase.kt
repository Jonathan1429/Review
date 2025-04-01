package com.jonathanev.review.Domain

import java.io.File
import javax.inject.Inject

class SetChangePathUseCase @Inject constructor() {
    operator fun invoke(folderName: String): File {
        return File("/data/data/com.jonathanev.review/files/guias/$folderName")
    }
}