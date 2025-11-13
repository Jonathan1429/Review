package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.BASERUTA_IMG
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.repository.FileHelperImpl
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
                var descifrado = setCifrarRutaImagenUseCase(item.encodedPath, 26 - 3)// cifrar(texto, 26 - 3)
                //val cifrado = item.url
                descifrado = descifrado.replace(BASERUTA_IMG.toRegex(), "")
                var soloRuta = ruta.replaceAfterLast("/", "")
                soloRuta = soloRuta.replaceFirst("guias", "imagenes")
                val imagen = descifrado.replaceBeforeLast("/", "").replace("/", "")
                descifrado = "$soloRuta$imagen"

                if (!fileHelper.exists(descifrado)) {
                    descifrado = descifrado.replace("imagenes".toRegex(), "imagenesPivote")
                }

                QuestionContent.Image(descifrado, item.encodedPath)
            }
            is QuestionContent.Text -> {
                getColorRanges.invoke(item.text)
            }

            is QuestionContent.None -> QuestionContent.None
        }
    }
}