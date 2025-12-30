package com.jonathanev.review.Domain

import com.jonathanev.review.presentation.model.QuestionContent
import com.jonathanev.review.data.media.MediaPaths
import com.jonathanev.review.data.repository.FileHelperImpl
import javax.inject.Inject

class SetPintarTextosUseCase @Inject constructor(
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val getColorRanges: GetColorRanges,
    private val fileHelper: FileHelperImpl
) {
    operator fun invoke(
        item: QuestionContent,
        ruta: String,
    ): QuestionContent {
        return when(item){
            is QuestionContent.Image -> {
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

                QuestionContent.Image(descifrado, item.nameFile)
            }
            is QuestionContent.Text -> {
                getColorRanges.invoke(item.text)
            }

            is QuestionContent.None -> QuestionContent.None
        }
    }
}