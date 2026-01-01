package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.data.media.MediaPaths
import com.jonathanev.review.data.provider.FileHelperImpl
import javax.inject.Inject

class SetPintarTextosUseCase @Inject constructor(
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val getColorRangesUseCase: GetColorRangesUseCase,
    private val fileHelper: FileHelperImpl
) {
    operator fun invoke(
        item: QuestionContentDomain,
        ruta: String,
    ): QuestionContentDomain {
        return when(item){
            is QuestionContentDomain.Image -> {
                var descifrado = setCifrarRutaImagenUseCase(item.nameFile, 26 - 3)// cifrar(texto, 26 - 3)
                //val cifrado = item.url
                descifrado = descifrado.replace(MediaPaths.BASERUTA_IMG.toRegex(), "")
                var soloRuta = ruta.replaceAfterLast("/", "")
                soloRuta = soloRuta.replaceFirst("guias", "imagenes")
                val imagen = descifrado.replaceBeforeLast("/", "").replace("/", "")
                descifrado = "$soloRuta$imagen"

                if (!fileHelper.exists(descifrado)) {
                    descifrado = descifrado.replace("imagenes".toRegex(), "imagenesPivote")
                }

                QuestionContentDomain.Image(descifrado, item.nameFile)
            }
            is QuestionContentDomain.Text -> {
                getColorRangesUseCase.invoke(item.text)
            }

            is QuestionContentDomain.None -> QuestionContentDomain.None
        }
    }
}