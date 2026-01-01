package com.jonathanev.review.domain

import java.io.File
import javax.inject.Inject

class GetImagenesEnDispositivoUseCase @Inject constructor() {
    operator fun invoke(file: File): Set<String> {
        val currentDeviceNames =
            file.listFiles()?.map { it.name }?.toSet() ?: emptySet()
        return currentDeviceNames
    }
}