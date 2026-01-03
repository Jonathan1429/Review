package com.jonathanev.review.domain

import android.os.Build.VERSION
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.PathProvider
import javax.inject.Inject

class UpdateImagesUseCase @Inject constructor(
    private val pathProvider: PathProvider,
    private val directoryManager: DirectoryManager,
    private val saveGuideImagesUseCase: SaveGuideImagesUseCase,
    private val getVersionUseCase: GetVersionUseCase
) {
    suspend operator fun invoke(
        nameGuide: String = "",
        preguntasProcesadas: List<QuestionItemDomain>,
        respuestasProcesadas: List<QuestionItemDomain>,
        isNewFile: Boolean
    ) {
        val imagesFolder = pathProvider.buildTempPathFile(nameGuide) // V2
        directoryManager.prepareCleanDirectory(imagesFolder.path, isNewFile)

        val version = getVersionUseCase.invoke()
        val listImages = (preguntasProcesadas + respuestasProcesadas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()

        if (isNewFile) {
            if (listImages.isNotEmpty()) {
                saveGuideImagesUseCase.saveImagesInDevice(listImages, imagesFolder)
            }
        } else {
            // Moving images V1
            if (version == Versions.VERSION1){
                directoryManager.moveImagesV1(
                    listImages = listImages,
                    nameGuide = nameGuide
                )
            }
        }

        // Delete images V2
        directoryManager.deleteLeftoverImagesInDevice(nameGuide, listImages)
    }
}