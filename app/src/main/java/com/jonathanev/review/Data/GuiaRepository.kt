package com.jonathanev.review.Data

import android.content.Intent
import android.util.Log
import android.util.Xml
import androidx.core.content.ContextCompat.startActivity
import com.jonathanev.review.Core.Constants.ANSWER
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.Core.Constants.CUESTIONARIO
import com.jonathanev.review.Core.Constants.DECODED
import com.jonathanev.review.Core.Constants.ENCODED
import com.jonathanev.review.Core.Constants.GUIAESTUDIO
import com.jonathanev.review.Core.Constants.IMAGEN
import com.jonathanev.review.Core.Constants.INTERROGANTE
import com.jonathanev.review.Core.Constants.NOMBREGUIA
import com.jonathanev.review.Core.Constants.NOQUESTION
import com.jonathanev.review.Core.Constants.POSQUESTION
import com.jonathanev.review.Core.Constants.PREGUNTA
import com.jonathanev.review.Core.Constants.QUESTION
import com.jonathanev.review.Core.Constants.RESPUESTA
import com.jonathanev.review.Core.Constants.TEXTO
import com.jonathanev.review.Core.Constants.VERSION
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.ResponseGuia
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Data.Model.prueba.QAItem
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Domain.GetAllGuiasUseCase
import com.jonathanev.review.Domain.GetColorRanges
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.UI.View.ActivityRepasarGuia
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
    private val getColorRanges: GetColorRanges,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
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

    fun saveFileV2(
        currentPath: String,
        preguntas: MutableList<QuestionItem>,
        respuestas: MutableList<QuestionItem>,
    ): Boolean {
        val nombreArchivo = currentPath.substringAfterLast("/")
        val file = File(currentPath)

        // Eliminamos el archivo anteriormente creado
        if (file.exists()) {
            file.delete()
            Log.d("ArchivoEliminado", "Archivo eliminado")
        }

        //Vamos a crear el archivo que acabamos de eliminar pero con el nuevo cuestionario
        try {
            val serializer = xmlSerializerFactory.create()
            val fos = fileOutputStreamFactory.create(currentPath)
            // fos = openFileOutput(nombreArchivo, MODE_PRIVATE) // Guarda el archivo en la raiz
            serializer.setOutput(fos, "UTF-8")
            serializer.startDocument(null, java.lang.Boolean.valueOf(true))
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.startTag("", GUIAESTUDIO)
            serializer.attribute("", VERSION, "2.0")
            serializer.startTag("", CUESTIONARIO)
            serializer.attribute("", NOMBREGUIA, nombreArchivo)

            writeQuestionsAnswers(serializer, preguntas, QUESTION)
            writeQuestionsAnswers(serializer, respuestas, ANSWER)

            // Si los campos estan vacios simplemente cierro las etiquetas y directamente
            // guardo el documento en el teléfono.
            serializer.endTag("", CUESTIONARIO)
            serializer.endTag("", GUIAESTUDIO)
            serializer.endDocument()
            serializer.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.exists()
    }

    private fun writeQuestionsAnswers(
        serializer: XmlSerializer,
        items: MutableList<QuestionItem>,
        type: String // Question or Answer
    ) {
        // Creo la etiqueta interrogante con su respectiva pregunta
        for ((index, item) in items.withIndex()) {
            serializer.startTag("", type)
            serializer.attribute("", "pos${type}", index.toString())

            for (content in item.content){
                when(content){
                    is QuestionContent.Image -> {
                        serializer.startTag("", IMAGEN)
                        serializer.attribute("", DECODED, content.decodedPath)
                        serializer.attribute("", ENCODED, content.encodedPath)
                        serializer.endTag("", IMAGEN)
                    }

                    QuestionContent.None -> Unit

                    is QuestionContent.Text -> {
                        serializer.startTag("", TEXTO)
                        serializer.attribute("", TEXTO, content.text)
                        serializer.endTag("", TEXTO)
                    }
                }
            }

            serializer.endTag("", type)
        }
    }

    fun saveFileV1(
        currentPath: String,
        preguntas: MutableList<QuestionItem>,
        respuestas: MutableList<QuestionItem>,
        /*didTheGuideAlreadyExist: Boolean,
        ruta: String*/
    ): ValidacionesGuiaModel {
        val nombreArchivo = currentPath.substringAfterLast("/")
        val file = File(currentPath)

        // Eliminamos el archivo anteriormente creado
        if (file.exists()) {
            file.delete()
            Log.d("ArchivoEliminado", "Archivo eliminado")
        }

        //Vamos a crear el archivo que acabamos de eliminar pero con el nuevo cuestionario
        return try {
            val serializer = xmlSerializerFactory.create()
            val fos = fileOutputStreamFactory.create(currentPath)
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

    fun obtenerDatosXMLV1(ruta: String): QAItem {
        //val qaItem = mutableListOf<QAItem>()
        val questions = mutableListOf<QuestionContent>()
        val answers = mutableListOf<QuestionContent>()
        val questionItem = mutableListOf<QuestionItem>()
        val answerItem = mutableListOf<QuestionItem>()

        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder
        try {
            db = dbf.newDocumentBuilder()
            /*val filePath: File = if (ruta == "null") {
                File(filePathsProvider.fileGuides.toString())
            } else {
                File(ruta)
            }*/
            val filePath: File = File(ruta)
            val doc = db.parse(filePath)

            // Buscamos los Nodos Interrogante y accedemos a lo que se encuentre dentro.
            val cuestionario: NodeList = doc.getElementsByTagName(INTERROGANTE)
            for (noItem in 0 until cuestionario.length) {
                // Accedes a los elmentos de dicho nodo
                val e: Element = cuestionario.item(noItem) as Element

                // Guardo cada uno de los valores en su respectivo arreglo.
                val question = e.getAttribute(PREGUNTA)
                val answer = e.getAttribute(RESPUESTA)

                var textDecoded = ""
                if (question.contains(BASERUTA_IMG_CIFRADO)){
                    textDecoded = setCifrarRutaImagenUseCase.invoke(question, 26 - 3)
                    questions.add(QuestionContent.Image(textDecoded,question))
                } else {
                    questions.add(getColorRanges.invoke(question))
                }
                questionItem.add(QuestionItem(questions))

                if (answer.contains(BASERUTA_IMG_CIFRADO)){
                    textDecoded = setCifrarRutaImagenUseCase.invoke(answer, 26 - 3)
                    answers.add(QuestionContent.Image(textDecoded,answer))
                } else {
                    answers.add(getColorRanges.invoke(answer))
                }
                answerItem.add(QuestionItem(answers))
            }
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return QAItem(questionItem, answerItem)
    }

    /*private fun typeItem(question: String): TypeFile{
        return if (question.contains(BASERUTA_IMG_CIFRADO))
            TypeFile.IMAGEN else TypeFile.TEXTO
    }*/
}