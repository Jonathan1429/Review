package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.NavigationPathRepository
import java.io.File
import javax.inject.Inject

class GetCurrentPathFilesUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(): Pair<File, File> {
        return Pair(navigationPathRepository.currentPathGuides, navigationPathRepository.currentPathImages)
    }
}