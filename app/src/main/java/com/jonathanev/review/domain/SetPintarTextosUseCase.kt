package com.jonathanev.review.domain

import com.jonathanev.review.core.media.MediaPaths
import com.jonathanev.review.data.filesystem.DirectoryManagerImpl
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.service.ColorRangeParser
import javax.inject.Inject

class SetPintarTextosUseCase @Inject constructor(
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val colorRangeParser: ColorRangeParser,
    private val fileHelper: DirectoryManagerImpl
) {
    operator fun invoke(
        item: QuestionContentDomain,
        ruta: String,
    ): QuestionContentDomain {
        return when(item){
            is QuestionContentDomain.Image -> {
                var descifrado = setCifrarRutaImagenUseCase(item.nameFile, 26 - 3)// cifrar(texto, 26 - 3)
                //val cifrado = item.url
                descifrado = descifrado.replace(MediaPaths.MEDIA_PICKER_BASE_PATH.toRegex(), "")
                var soloRuta = ruta.replaceAfterLast("/", "")
                soloRuta = soloRuta.replaceFirst("guias", "imagenes")
                val imagen = descifrado.replaceBeforeLast("/", "").replace("/", "")
                descifrado = "$soloRuta$imagen"

                if (!fileHelper.existPath(descifrado)) {
                    descifrado = descifrado.replace("imagenes".toRegex(), "imagenesPivote")
                }

                QuestionContentDomain.Image(descifrado, item.nameFile)
            }
            is QuestionContentDomain.Text -> {
                colorRangeParser.invoke(item.text)
            }

            is QuestionContentDomain.None -> QuestionContentDomain.None
        }
    }
}