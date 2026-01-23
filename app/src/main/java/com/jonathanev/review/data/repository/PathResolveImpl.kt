package com.jonathanev.review.data.repository

import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.PathResolve
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class PathResolveImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val navigationPathRepository: NavigationPathRepository
) : PathResolve {
    override fun currentPathResolve(guideDomainModel: GuideDomainModel, guideFileName: String): GuidePath {
        return if (guideDomainModel.version == GuideVersion.V1) {
            GuidePath(
                filePathsProvider.buildGuide(
                    navigationPathRepository.currentPathGuides.value,
                    guideFileName
                )
            )
        } else {
            GuidePath(
                filePathsProvider.buildFolderGuide(
                    navigationPathRepository.currentPathGuides.value,
                    guideDomainModel.nameGuide,
                    guideFileName
                )
            )

        }
    }

    override fun newPathResolve(newFolder: String, newFile: String): GuidePath {
        return GuidePath(
            filePathsProvider.buildFolderGuide(
                navigationPathRepository.currentPathGuides.value,
                newFolder,
                newFile
            )
        )
    }
}