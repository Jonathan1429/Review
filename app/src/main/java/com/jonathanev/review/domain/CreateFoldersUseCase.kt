package com.jonathanev.review.domain

import javax.inject.Inject

class CreateFoldersUseCase @Inject constructor(
    private val directoryManager: DirectoryManager
) {
    operator fun invoke(): Boolean {
        return directoryManager.createFoldersMain()
    }
}