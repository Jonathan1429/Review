package com.jonathanev.review.Data

import android.graphics.Color
import android.util.Log
import android.util.Xml
import com.jonathanev.review.Core.Constants.ANSWER
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.Core.Constants.CUESTIONARIO
import com.jonathanev.review.Core.Constants.DECODED
import com.jonathanev.review.Core.Constants.DESCRIPCION
import com.jonathanev.review.Core.Constants.ENCODED
import com.jonathanev.review.Core.Constants.GUIAESTUDIO
import com.jonathanev.review.Core.Constants.IMAGEN
import com.jonathanev.review.Core.Constants.INTERROGANTE
import com.jonathanev.review.Core.Constants.NAMEFILE
import com.jonathanev.review.Core.Constants.NOMBREGUIA
import com.jonathanev.review.Core.Constants.PREGUNTA
import com.jonathanev.review.Core.Constants.QUESTION
import com.jonathanev.review.Core.Constants.RESPUESTA
import com.jonathanev.review.Core.Constants.TEXTO
import com.jonathanev.review.Core.Constants.URI
import com.jonathanev.review.Core.Constants.VERSION
import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.Model.prueba.AnswerState
import com.jonathanev.review.Data.Model.prueba.FolderModel
import com.jonathanev.review.Data.Model.prueba.QAItem
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.GetColorRanges
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetSubstringPathUseCase
import com.jonathanev.review.R
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

open class XmlSerializerFactory @Inject constructor() {
    fun create(): XmlSerializer = Xml.newSerializer()
}

class FileOutputStreamFactory @Inject constructor() {
    fun create(path: String): FileOutputStream = FileOutputStream(path)
}

@Singleton
class GuiaRepositoryImpl @Inject constructor(
    private val getColorRanges: GetColorRanges,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setSubstringPathUseCase: SetSubstringPathUseCase,
    private val xmlSerializerFactory: XmlSerializerFactory,
    private val fileOutputStreamFactory: FileOutputStreamFactory,
    private val filePathsProvider: FilePathsProvider
): GuiaRepository {
    /*fun getGuias(file: File): List<com.jonathanev.review.Data.Model.GuideModel> {
        guiaProvider.guias = getAllGuiasUseCase.invoke(file)
        return guiaProvider.guias
    }*/

    override fun getFolders(file: File): List<FolderModel> {
        return file.listFiles()
            ?.filter { it.isDirectory }
            ?.map { item ->
                FolderModel(
                    nameFolder = item.name,
                    description = "",
                    imgFolder = R.drawable.ic_anchor_solid_full,
                    color = Color.BLACK
                )
            }
            ?: emptyList()
    }

    override fun getGuides(file: File): List<GuideModel> {
        return file.listFiles()
            ?.filter { !it.isDirectory }
            ?.mapNotNull { item ->
                runCatching {
                    getAttributesGuide(item)
                }.getOrNull()
            }
            ?: emptyList()
    }

    /*fun updateNumGuidesInFolders(listFolders: List<FolderModel>){
        guiaProvider.folders = listFolders
    }*/

    fun saveFileV2(
        nameGuide: String,
        description: String,
        currentPath: String,
        imagesPath: File,
        preguntas: MutableList<QuestionItem>,
        respuestas: MutableList<QuestionItem>,
    ): Boolean {
        val file = File(currentPath)

        // Eliminamos el archivo anteriormente creado
        if (file.exists()) {
            file.delete()

            // Delete images from guide
            //imagesPath.delete()
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
            serializer.attribute("", NOMBREGUIA, nameGuide)
            serializer.attribute("", DESCRIPCION, description)

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
                        serializer.attribute("", URI, content.uri)
                        serializer.attribute("", NAMEFILE, content.nameFile)
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

    fun obtenerDatosXMLV2(ruta: String): List<QAItem> {
        val qaItem = mutableListOf<QAItem>()

        /*val listaQA = mutableListOf<QAItem>()
        val questionItems = mutableListOf<QuestionItem>()
        val answerItems = mutableListOf<QuestionItem>()*/

        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val file = File(ruta)
        val doc = db.parse(file)

        //listaQA.addAll(QAItem(getQAXML(doc, QUESTION),getQAXML(doc, ANSWER)))
        //questionItems.addAll()
        getQAXML(qaItem, doc, QUESTION)
        getQAXML(qaItem, doc, ANSWER)

        return qaItem
    }

    private fun getQAXML(qaItem: MutableList<QAItem>, doc: Document, typeContent: String) {

        //val items = mutableListOf<QuestionItem>()

        // Leer Questions
        val questionsNode = doc.getElementsByTagName(typeContent) //Question/Answer
        for (i in 0 until questionsNode.length) {
            val element = questionsNode.item(i) as Element
            val contentList = mutableListOf<QuestionContent>()

            val texts = element.getElementsByTagName(TEXTO)
            for (j in 0 until texts.length) {
                val t = texts.item(j) as Element
                val textValue = t.getAttribute(TEXTO)
                val qcText = getColorRanges.invoke(textValue)

                contentList.add(
                    QuestionContent.Text(
                        text = qcText.text,
                        colorRanges = qcText.colorRanges // tu lógica aquí
                    )
                )
            }

            val images = element.getElementsByTagName(IMAGEN)
            for (j in 0 until images.length) {
                val img = images.item(j) as Element
                val uri = img.getAttribute(URI)
                val nameFile = img.getAttribute(NAMEFILE)
                contentList.add(
                    QuestionContent.Image(uri, nameFile)
                )
            }

            val item = QuestionItem(contentList)
            if (typeContent == QUESTION){
                qaItem.add(QAItem(question = item))
            } else {
                val current = qaItem[i]
                qaItem[i] = current.copy(answer = AnswerState.Filled(item))
            }
        }
    }

    fun obtenerDatosXMLV1(ruta: String): List<QAItem> {
        val listaQA = mutableListOf<QAItem>()

        val dbf = DocumentBuilderFactory.newInstance()

        try {
            val db = dbf.newDocumentBuilder()
            val filePath = File(ruta)
            val doc = db.parse(filePath)

            val cuestionario: NodeList = doc.getElementsByTagName(INTERROGANTE)

            for (i in 0 until cuestionario.length) {

                val e = cuestionario.item(i) as Element

                val ques = e.getAttribute(PREGUNTA)
                val ans  = e.getAttribute(RESPUESTA)

                // ---- PREGUNTA ----
                val preguntaContent = mutableListOf<QuestionContent>()

                val preguntaProcesada = if (ques.contains(BASERUTA_IMG_CIFRADO)) {
                    var decoded = setCifrarRutaImagenUseCase.invoke(ques, 26 - 3)
                    decoded = setSubstringPathUseCase.invoke(ruta, decoded)
                    QuestionContent.Image(decoded, ques)
                } else {
                    getColorRanges.invoke(ques)
                }

                preguntaContent.add(preguntaProcesada)
                val preguntaItem = QuestionItem(preguntaContent.toList())

                // ---- RESPUESTA ----
                val respuestaContent = mutableListOf<QuestionContent>()

                val respuestaProcesada = if (ans.contains(BASERUTA_IMG_CIFRADO)) {
                    val decoded = setCifrarRutaImagenUseCase.invoke(ans, 26 - 3)
                    QuestionContent.Image(decoded, ans)
                } else {
                    getColorRanges.invoke(ans)
                }

                respuestaContent.add(respuestaProcesada)
                val respuestaItem = QuestionItem(respuestaContent.toList())


                listaQA.add(
                    QAItem(
                        question = preguntaItem,
                        answer = AnswerState.Filled(respuestaItem)
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listaQA
    }

    override fun getXMLVersion(ruta: String): List<QAItem> {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val file = File(ruta)
        val doc = db.parse(file)

        val versionNode = doc.getElementsByTagName(GUIAESTUDIO)
        val element = versionNode.item(0) as Element
        val textValue = element.getAttribute(VERSION)

        //return obtenerDatosXMLV1(ruta)
        return if(textValue == "1.0") obtenerDatosXMLV1(ruta) else obtenerDatosXMLV2(ruta)
    }

    override fun getAttributesGuide(file: File): GuideModel {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(file)

        val cuestionarioNode = doc.getElementsByTagName("Cuestionario")
        val element = cuestionarioNode.item(0) as Element

        val name = element.getAttribute("nombreGuia")
        val description = element.getAttribute("Descripcion")

        return GuideModel(
            nameGuide = name,
            description = description
        )
    }
}