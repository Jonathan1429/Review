package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.domain.model.GuidePathContext
import java.io.File
import javax.inject.Inject

class ResolveFolderPathUseCase @Inject constructor(
    private val pathProvider: PathProvider
) {
    operator fun invoke(context: GuidePathContext) {
        //return pathProvider.resolveGuidePath(context)
    }
}