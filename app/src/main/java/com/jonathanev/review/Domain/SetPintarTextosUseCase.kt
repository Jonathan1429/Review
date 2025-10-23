package com.jonathanev.review.Domain

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.jonathanev.review.Core.Constants.BASERUTA_IMG
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.Data.Model.ColorPregModel
import com.jonathanev.review.Data.Model.EstadoImagen
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.repository.FileHelperImpl
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Data.TypeFile
import javax.inject.Inject

class SetPintarTextosUseCase @Inject constructor(
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val fileHelper: FileHelperImpl
) {
    operator fun invoke(
        isEtPregunta: Boolean,
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        ruta: String
    ): ValidacionesGuiaModel {
        var contColorPreg: Int = 0
        var inicio: Int = 0
        var fin: Int = 0
        var colorPregModel: ColorPregModel? = null
        var texto: String = ""
        val preguntasColor: ArrayList<ColorPregModel> = ArrayList()
        val respuestasColor: ArrayList<ColorPregModel> = ArrayList()
        var builder: SpannableStringBuilder? = null

        texto = if (isEtPregunta) {
            preguntas[contadorPregunta]
            // uri = texto.toUri()
        } else {
            respuestas[contadorPregunta]
            // uri = texto.toUri()
        }

        return if (texto.contains(BASERUTA_IMG_CIFRADO)) {
            val descifrado = setCifrarRutaImagenUseCase(texto, 26 - 3)// cifrar(texto, 26 - 3)
            val cifrado = texto
            texto = descifrado.replace(BASERUTA_IMG.toRegex(), "")
            var soloRuta = ruta.replaceAfterLast("/", "")
            soloRuta = soloRuta.replaceFirst("guias", "imagenes")
            val imagen = texto.replaceBeforeLast("/", "").replace("/", "")
            texto = "$soloRuta$imagen"

            if (!fileHelper.exists("" + texto)) {
                texto = texto.replace("imagenes".toRegex(), "imagenesPivote")
            }

            // Cuando lo que se va a mostrar es una imagen
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    typeFile = TypeFile.IMAGEN,
                    isShowCancelar = true,
                ),
                estadoImagen = EstadoImagen(cifrado, texto),
            )
        } else {
            while (texto.contains("«")) {
                inicio = texto.indexOf("«") + 1
                fin = texto.indexOf("»")
                val color: String = texto.substring(inicio, fin)
                val longColor: Int = color.length
                val colEntero: Int = color.toInt()
                inicio = fin + 1
                fin = texto.indexOf("«", inicio)
                colorPregModel =
                    ColorPregModel((inicio - longColor - 2), (fin - longColor - 2), colEntero)

                if (isEtPregunta) {
                    preguntasColor.add(contColorPreg, colorPregModel)
                } else {
                    respuestasColor.add(contColorPreg, colorPregModel)
                }

                // Eliminar la primera etiqueta y su contenido
                texto = texto.replaceFirst("«.*?»".toRegex(), "")

                // Eliminar la segunda etiqueta y su contenido
                texto = texto.replaceFirst("«.*?»".toRegex(), "")
                contColorPreg++
            }


            builder = SpannableStringBuilder(texto)
            for (coloresPreguntas: ColorPregModel in if (isEtPregunta) preguntasColor else respuestasColor) {
                val colorSpan: ForegroundColorSpan = ForegroundColorSpan(coloresPreguntas.color)
                builder.setSpan(
                    colorSpan,
                    coloresPreguntas.inicioColor,
                    coloresPreguntas.finColor,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            // Cuando lo que se va a pintar es texto
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                ),
                builder = builder,
            )
        }
    }
}