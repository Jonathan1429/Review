package com.jonathanev.review.data.repository

import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.PathResolve
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import javax.inject.Inject

class PathResolveImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val navigationPathRepository: NavigationPathRepository
) : PathResolve {
    override fun currentPathResolve(guideDomainModel: GuideDomainModel): GuidePath {
        return if (guideDomainModel.version == GuideVersion.V1) {
            GuidePath(
                filePathsProvider.buildGuide(
                    navigationPathRepository.currentPathGuides,
                    guideDomainModel.nameGuide
                )
            )
        } else {
            GuidePath(
                filePathsProvider.buildFolderGuide(
                    navigationPathRepository.currentPathGuides,
                    guideDomainModel.nameGuide,
                    guideDomainModel.nameGuide
                )
            )

        }
    }

    override fun newPathResolve(newFolder: String): GuidePath {
        return GuidePath(
            filePathsProvider.buildFolderGuide(
                navigationPathRepository.currentPathGuides,
                newFolder,
                newFolder
            )
        )
    }
}