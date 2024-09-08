package com.jonathanev.review.Data

import android.content.Context
import android.util.Xml
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Core.Constants.rutaPrin
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiaProvider
import com.jonathanev.review.Data.Model.ResponseGuia
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Domain.getAllGuiasUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import kotlin.io.path.exists

class GuiaRepository @Inject constructor(
    val getAllGuiasUseCase: getAllGuiasUseCase,
    val guiaProvider: GuiaProvider,
    @ApplicationContext private val context: Context
) {
    fun getGuias(file: File): List<GuiaModel> {
        guiaProvider.guias = getAllGuiasUseCase(file)!!
        return guiaProvider.guias
    }

    fun saveFile(
        nombreArchivo: String,
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>
    ): ValidacionesGuiaModel {
        val fos: FileOutputStream
        val serializer: XmlSerializer = Xml.newSerializer()

        return try {
            fos = context.openFileOutput("$nombreArchivo.xml", MODE_PRIVATE)
            serializer.setOutput(fos, "UTF-8")
            serializer.startDocument(null, java.lang.Boolean.valueOf(true))
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.startTag("", "GuiaEstudio")
            serializer.attribute("", "version", "1.0")
            serializer.startTag("", "Cuestionario")
            serializer.attribute("", "nombreGuia", nombreArchivo)
            // Creo la etiqueta interrogante con su respectiva pregunta
            for (i in preguntas.indices) {
                serializer.startTag("", "Interrogante")
                serializer.attribute("", "pregunta", preguntas[i])
                serializer.attribute("", "respuesta", respuestas[i])
                serializer.endTag("", "Interrogante")
            }
            // Si los campos estan vacios simplemente cierro las etiquetas y directamente
            // guardo el documento en el teléfono.
            serializer.endTag("", "Cuestionario")
            serializer.endTag("", "GuiaEstudio")
            serializer.endDocument()
            serializer.flush()
            fos?.close()

            val rutaGuiaCreada = File("$rutaPrin/$nombreArchivo.xml")
            if (rutaGuiaCreada.exists()) {
                Files.copy(
                    Paths.get("$rutaPrin/$nombreArchivo.xml"),
                    Paths.get("$file/$nombreArchivo.xml"),
                    StandardCopyOption.REPLACE_EXISTING
                )

                if (Paths.get("$file/$nombreArchivo.xml").exists()) {
                    // Borrar archivo de files y agregarlo en una carpeta de guias
                    File(rutaPrin, "$nombreArchivo.xml").delete()
                    val ruta = "$file/$nombreArchivo.xml"

                    ValidacionesGuiaModel(
                        message = "Guia de estudio creada exitosamente",
                        responseGuia = ResponseGuia(ruta),
                        estadoUI = EstadoUI(
                            isCreatedGuia = true,
                        )
                    )
                } else {
                    ValidacionesGuiaModel(
                        message = "Guia de estudio no se creó correctamente"
                    )
                }
            } else {
                ValidacionesGuiaModel(
                    message = "Guia de estudio no se creó correctamente"
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()

            ValidacionesGuiaModel(
                message = "Guia de estudio no se creó correctamente"
            )
        }
    }
}