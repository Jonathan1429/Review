package com.jonathanev.review.data.repository

import com.jonathanev.review.data.JsonManager
import com.jonathanev.review.data.mapper.json.toDto
import com.jonathanev.review.data.model.json.ScreenDataDto
import com.jonathanev.review.domain.model.FolderScreenInfoDomain
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.MetadataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class MetadataRepositoryImpl @Inject constructor(
    private val jsonManager: JsonManager,
    private val filePathResolver: FilePathResolver
) : MetadataRepository {
    override suspend fun saveMetadata(
        data: FolderScreenInfoDomain
    ) = withContext(Dispatchers.IO) {
        val guidesPath = File(
            filePathResolver.mapToFolderPath(PathKind.GUIAS).value
        )

        if (!guidesPath.exists()) {
            guidesPath.mkdirs()
        }

        val screenFile = File(guidesPath, SCREEN_DATA_FILE_NAME)

        val screenDataDto = data.toDto()
        jsonManager.write(
            screenFile.path,
            ScreenDataDto.serializer(),
            screenDataDto
        )
    }

    companion object {
        private const val SCREEN_DATA_FILE_NAME = "screen.json"
    }
}