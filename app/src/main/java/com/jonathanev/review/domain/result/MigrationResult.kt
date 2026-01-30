package com.jonathanev.review.domain.result

data class MigrationResult(
    val movedFiles: List<String>,
    val failedFiles: List<String>
) {
    val hasSuccess: Boolean get() = movedFiles.isNotEmpty()
}