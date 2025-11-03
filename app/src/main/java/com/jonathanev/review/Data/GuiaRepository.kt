package com.jonathanev.review.Data

import android.util.Xml
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Data.Model.PreguntaRespuestaModel
import com.jonathanev.review.Data.Model.ResponseGuia
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Domain.GetAllGuiasUseCase
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

open class XmlSerializerFactory @Inject constructor() {
    fun create(): XmlSerializer = Xml.newSerializer()
}

class FileOutputStreamFactory @Inject constructor() {
    fun create(path: String): FileOutputStream = FileOutputStream(path)
}

class GuiaRepository @Inject constructor(
    private val getAllGuiasUseCase: GetAllGuiasUseCase,
    private val guiaProvider: GuiaProvider,
    private val xmlSerializerFactory: XmlSerializerFactory,
    private val fileOutputStreamFactory: FileOutputStreamFactory,
    private val filePathsProvider: FilePathsProvider
    //@ApplicationContext private val context: Context
) {
    fun getGuias(file: File): List<GuiaModel> {
        guiaProvider.guias = getAllGuiasUseCase(file)
        return guiaProvider.guias
    }

    fun saveFile(
        nombreArchivo: String,
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        didTheGuideAlreadyExist: Boolean,
        ruta: String
    ): ValidacionesGuiaModel {
        // Eliminamos el archivo anteriormente creado
        if (didTheGuideAlreadyExist) {
            File(ruta).delete()
            //Log.d("ArchivoEliminado", "Archivo eliminado")
        }

        //Vamos a crear el archivo que acabamos de eliminar pero con el nuevo cuestionario
        return try {
            val serializer = xmlSerializerFactory.create()
            val fos = fileOutputStreamFactory.create(ruta)
            // fos = openFileOutput(nombreArchivo, MODE_PRIVATE) // Guarda el archivo en la raiz
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
            fos.close()

            // val rutaActualizada = "$file/$nombreArchivo"
            ValidacionesGuiaModel(
                message = "Guia de estudio creada exitosamente",
                responseGuia = ResponseGuia(ruta),
                estadoUI = EstadoUI(
                    isCreatedGuia = true,
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()

            ValidacionesGuiaModel(
                message = "Guia de estudio no se creó correctamente",
            )
        }
    }

    fun obtenerDatosXML(ruta: String): List<PreguntaRespuestaModel> {
        val preguntasRespuestas = mutableListOf<PreguntaRespuestaModel>()
        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder
        try {
            db = dbf.newDocumentBuilder()
            val filePath: File = if (ruta == "null") {
                File(filePathsProvider.fileGuides.toString())
            } else {
                File(ruta)
            }
            val doc = db.parse(filePath)

            // Buscamos los Nodos Interrogante y accedemos a lo que se encuentre dentro.
            val cuestionario: NodeList = doc.getElementsByTagName("Interrogante")
            for (i in 0 until cuestionario.length) {
                // Accedes a los elmentos de dicho nodo
                val e: Element = cuestionario.item(i) as Element

                // Guardo cada uno de los valores en su respectivo arreglo.
                val pregunta = e.getAttribute("pregunta")
                val respuesta = e.getAttribute("respuesta")
                preguntasRespuestas.add(PreguntaRespuestaModel(pregunta, respuesta))
            }
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return preguntasRespuestas
    }
}