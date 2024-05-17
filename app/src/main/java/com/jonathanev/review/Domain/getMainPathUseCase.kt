package com.jonathanev.review.Domain

import java.io.File
import javax.inject.Inject

class getMainPathUseCase @Inject constructor() {
    operator fun invoke(): File = File("/data/data/com.jonathanev.review/files/")
}