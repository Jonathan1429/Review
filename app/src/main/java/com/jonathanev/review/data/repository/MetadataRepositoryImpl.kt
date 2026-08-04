package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.mapper.json.toDto
import com.jonathanev.review.data.model.json.ScreenDataDto
import com.jonathanev.review.domain.model.FolderScreenInfoDomain
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.MetadataRepository
import java.io.File
import javax.inject.Inject

class MetadataRepositoryImpl @Inject constructor(
    private val jsonManager: JsonManager,
    private val filePathResolver: FilePathResolver
) : MetadataRepository {
    override suspend fun saveMetadata(data: FolderScreenInfoDomain) {
        val guidesPath =
            File(filePathResolver.mapToFolderPath(PathKind.GUIAS).value)
        val screenFile = File(guidesPath, "screen.json").path

        val screenDataDto = data.toDto()
        jsonManager.write(screenFile, ScreenDataDto.serializer(), screenDataDto)
    }
}