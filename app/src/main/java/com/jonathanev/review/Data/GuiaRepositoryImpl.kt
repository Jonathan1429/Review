package com.jonathanev.review.Data

import android.graphics.Color
import android.util.Xml
import com.jonathanev.review.Core.Constants.ANSWER
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.Core.Constants.CUESTIONARIO
import com.jonathanev.review.Core.Constants.DESCRIPCION
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
import com.jonathanev.review.Core.Constants.VERSION1
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
) : GuiaRepository {
    override fun getFolders(file: File): List<FolderModel> {
        return file.listFiles()
            ?.filter { it.isDirectory }
            ?.map { item ->
                FolderModel(
                    name = item.name,
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
        currentPath: File,
        preguntas: List<QuestionItem>,
        respuestas: List<QuestionItem>,
    ): Boolean {
        val parentDir = currentPath.parent ?: return false
        val newFile = File(
            /* parent = */ parentDir,
            /* child = */ "$nameGuide.xml"
        )
        val tempFile = File("$currentPath.tmp")

        return try {
            val serializer = xmlSerializerFactory.create()
            val fos = fileOutputStreamFactory.create(tempFile.path)

            serializer.setOutput(fos, "UTF-8")
            try {
                serializer.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output",
                    true
                )
            } catch (_: Exception) {}
            serializer.startDocument(null, true)
            serializer.startTag("", GUIAESTUDIO)
            serializer.attribute("", VERSION, "2.0")

            serializer.startTag("", CUESTIONARIO)
            serializer.attribute("", NOMBREGUIA, nameGuide)
            serializer.attribute("", DESCRIPCION, description)

            writeQuestionsAnswers(serializer, preguntas, QUESTION)
            writeQuestionsAnswers(serializer, respuestas, ANSWER)

            serializer.endTag("", CUESTIONARIO)
            serializer.endTag("", GUIAESTUDIO)
            serializer.endDocument()

            fos.close()



            if(!tempFile.renameTo(newFile)) return false

            if (newFile != currentPath){
                currentPath.delete()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    private fun writeQuestionsAnswers(
        serializer: XmlSerializer,
        items: List<QuestionItem>,
        type: String // Question or Answer
    ) {
        // Creo la etiqueta interrogante con su respectiva pregunta
        for ((index, item) in items.withIndex()) {
            serializer.startTag("", type)
            serializer.attribute("", "pos${type}", index.toString())

            for (content in item.content) {
                when (content) {
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

    private fun obtenerDatosXMLV2(ruta: String, version: String): List<QAItem> {
        val qaItem = mutableListOf<QAItem>()

        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val file = File(ruta)
        val doc = db.parse(file)

        //listaQA.addAll(QAItem(getQAXML(doc, QUESTION),getQAXML(doc, ANSWER)))
        //questionItems.addAll()
        getQAXML(qaItem, doc, QUESTION, ruta, version)
        getQAXML(qaItem, doc, ANSWER, ruta, version)

        return qaItem
    }

    private fun getQAXML(
        qaItem: MutableList<QAItem>,
        doc: Document,
        typeContent: String,
        ruta: String,
        version: String
    ) {
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
                //val uri = img.getAttribute(URI)
                val nameFile = img.getAttribute(NAMEFILE)
                val uri = setSubstringPathUseCase.invoke(
                    path = ruta,
                    version = version,
                    nameFile = nameFile
                )
                contentList.add(
                    QuestionContent.Image(uri, nameFile)
                )
            }

            val item = QuestionItem(contentList)
            if (typeContent == QUESTION) {
                qaItem.add(QAItem(question = item))
            } else {
                val current = qaItem[i]
                qaItem[i] = current.copy(answer = AnswerState.Filled(item))
            }
        }
    }

    private fun obtenerDatosXMLV1(ruta: String, version: String): List<QAItem> {
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
                val ans = e.getAttribute(RESPUESTA)

                // ---- PREGUNTA ----
                val preguntaContent = mutableListOf<QuestionContent>()

                val preguntaProcesada = if (ques.contains(BASERUTA_IMG_CIFRADO)) {
                    var decoded = setCifrarRutaImagenUseCase.invoke(ques, 26 - 3)
                    decoded = setSubstringPathUseCase.invoke(ruta, decoded, version)
                    val nameFile = decoded.substringAfterLast("/")
                    QuestionContent.Image(uri = decoded, nameFile = nameFile)
                } else {
                    getColorRanges.invoke(ques)
                }

                preguntaContent.add(preguntaProcesada)
                val preguntaItem = QuestionItem(preguntaContent.toList())

                // ---- RESPUESTA ----
                val respuestaContent = mutableListOf<QuestionContent>()

                val respuestaProcesada = if (ans.contains(BASERUTA_IMG_CIFRADO)) {
                    var decoded = setCifrarRutaImagenUseCase.invoke(ans, 26 - 3)
                    decoded = setSubstringPathUseCase.invoke(ruta, decoded, version)
                    val nameFile = decoded.substringAfterLast("/")
                    QuestionContent.Image(uri = decoded, nameFile = nameFile)
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
        val version = element.getAttribute(VERSION)

        //return obtenerDatosXMLV1(ruta)
        return if (version == VERSION1) obtenerDatosXMLV1(
            ruta,
            version
        ) else obtenerDatosXMLV2(ruta, version)
    }

    override fun getAttributesGuide(file: File): GuideModel {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(file)

        val versionNode = doc.getElementsByTagName(GUIAESTUDIO)
        val elementGuide = versionNode.item(0) as Element
        val version = elementGuide.getAttribute(VERSION)

        val cuestionarioNode = doc.getElementsByTagName(CUESTIONARIO)
        val element = cuestionarioNode.item(0) as Element

        var name = ""
        val description = element.getAttribute(DESCRIPCION)
        //var name = element.getAttribute("nombreGuia")
        if (version == "1.0") {
            val fileName = file.path.substringAfterLast("/")
            name = fileName
            name = name.replace(".xml", "")
        } else {
            name = element.getAttribute("nombreGuia")
        }

        return GuideModel(
            nameGuide = name,
            description = description
        )
    }

    override fun setAttributesGuide(
        file: File,
        fileName: String,
        description: String,
        preguntas: List<QuestionItem>,
        respuestas: List<QuestionItem>
    ): Boolean {
        return saveFileV2(
            fileName,
            description,
            file,
            preguntas,
            respuestas
        )
    }

    override fun getVersion(file: File): String {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(file)

        val versionNode = doc.getElementsByTagName(GUIAESTUDIO)
        val elementGuide = versionNode.item(0) as Element
        return elementGuide.getAttribute(VERSION)
    }
}