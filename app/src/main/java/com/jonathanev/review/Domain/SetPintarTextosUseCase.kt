package com.jonathanev.review.Domain

import com.jonathanev.review.Core.Constants.BASERUTA_IMG
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.repository.FileHelperImpl
import javax.inject.Inject

class SetPintarTextosUseCase @Inject constructor(
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val fileHelper: FileHelperImpl
) {
    operator fun invoke(
        item: QuestionContent,
        ruta: String,
    ): QuestionContent {
        val colorRange: List<ColorRange> = mutableListOf()
        var contColorPreg: Int = 0
        var text: String = ""

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
                val originalText = item.text
                text = originalText

                while (text.contains("«")) {
                    var startTag = text.indexOf("«")
                    val endTag = text.indexOf("»")
                    val color: Int = text.substring(startTag + 1, endTag).toInt()
                    //val longColor: Int = color.length
                    //val colEntero: Int = color.toInt()
                    startTag = endTag//fin + 1
                    val startText = endTag + 1
                    val endText = text.indexOf("«", startText)

                    colorRange.plus(ColorRange(startText, endText, color))

                    // Eliminar la primera etiqueta y su contenido
                    text = text.replaceFirst("«.*?»".toRegex(), "")

                    // Eliminar la segunda etiqueta y su contenido
                    text = text.replaceFirst("«.*?»".toRegex(), "")
                    contColorPreg++
                }

                QuestionContent.Text(text, colorRange)
            }

            QuestionContent.None -> QuestionContent.None
        }
    }
}