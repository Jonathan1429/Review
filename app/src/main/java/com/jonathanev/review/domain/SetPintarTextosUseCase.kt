package com.jonathanev.review.domain

import com.jonathanev.review.core.media.MediaPaths
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.service.ColorRangeParser
import com.jonathanev.review.domain.util.CaesarCipher
import javax.inject.Inject

class SetPintarTextosUseCase @Inject constructor(
    private val caesarCipher: CaesarCipher,
    private val colorRangeParser: ColorRangeParser,
    private val directoryManager: DirectoryManager
) {
    operator fun invoke(
        item: QuestionContentDomain,
        ruta: String,
    ): QuestionContentDomain {
        return when(item){
            is QuestionContentDomain.Image -> {
                var descifrado = caesarCipher.decrypt(item.nameFile, 26 - 3)// cifrar(texto, 26 - 3)
                //val cifrado = item.url
                descifrado = descifrado.replace(MediaPaths.MEDIA_PICKER_BASE_PATH.toRegex(), "")
                var soloRuta = ruta.replaceAfterLast("/", "")
                soloRuta = soloRuta.replaceFirst("guias", "imagenes")
                val imagen = descifrado.replaceBeforeLast("/", "").replace("/", "")
                descifrado = "$soloRuta$imagen"

                if (!directoryManager.existPath(descifrado)) {
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