package com.jonathanev.review.Domain

import com.jonathanev.review.Data.provider.FilePathsProvider
import javax.inject.Inject

class DeleteContentInPiv @Inject constructor(
    private val filePathsProvider: FilePathsProvider
) {
    operator fun invoke(){
        val files = filePathsProvider.fileImagesPiv.listFiles()
        if (files != null) {
            for (subFile in files) {
                subFile.delete()
            }
        }
    }
}