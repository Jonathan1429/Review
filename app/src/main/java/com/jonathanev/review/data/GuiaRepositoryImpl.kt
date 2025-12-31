package com.jonathanev.review.data

import android.graphics.Color
import android.util.Xml
import com.jonathanev.review.Domain.GetColorRanges
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetSubstringPathUseCase
import com.jonathanev.review.Domain.model.ContentType
import com.jonathanev.review.Domain.model.QAType
import com.jonathanev.review.R
import com.jonathanev.review.data.model.GuideXmlModel
import com.jonathanev.review.presentation.state.ResponseDomain
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.state.QAItemDomain
import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.presentation.model.QuestionItemDomain
import com.jonathanev.review.data.media.MediaPaths
import com.jonathanev.review.data.xml.Attributes
import com.jonathanev.review.data.xml.Structure
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.data.xml.XmlMapper
import com.jonathanev.review.data.xml.XmlTagsV1
import com.jonathanev.review.data.xml.XmlTagsV2
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
    private val fileOutputStreamFactory: FileOutputStreamFactory
) : GuiaRepository {
    override fun getFolders(file: File): List<FolderUiModel> {
        return file.listFiles()
            ?.filter { it.isDirectory }
            ?.map { item ->
                FolderUiModel(
                    name = item.name,
                    description = "",
                    imgFolder = R.drawable.ic_anchor_solid_full,
                    color = Color.BLACK
                )
            }
            ?: emptyList()
    }

    override fun getGuides(file: File): List<GuideXmlModel> {
        return file.listFiles()
            ?.filter { !it.isDirectory }
            ?.mapNotNull { item ->
                runCatching {
                    getAttributesGuide(item)
                }.getOrNull()
            }
            ?: emptyList()
    }

    override fun saveFileV2(
        nameGuide: String,
        description: String,
        currentPath: File,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
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
            } catch (_: Exception) {
            }
            serializer.startDocument(null, true)
            serializer.startTag("", Structure.GUIAESTUDIO)
            serializer.attribute("", Attributes.VERSION, "2.0")

            serializer.startTag("", Structure.CUESTIONARIO)
            serializer.attribute("", Attributes.NOMBREGUIA, nameGuide)
            serializer.attribute("", Attributes.DESCRIPCION, description)

            writeQuestionsAnswers(serializer, preguntas, XmlMapper.toXmlQA(QAType.QUESTION))
            writeQuestionsAnswers(serializer, respuestas, XmlMapper.toXmlQA(QAType.ANSWER))

            serializer.endTag("", Structure.CUESTIONARIO)
            serializer.endTag("", Structure.GUIAESTUDIO)
            serializer.endDocument()

            fos.close()

            if (!tempFile.renameTo(newFile)) return false

            if (newFile != currentPath) {
                currentPath.delete()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    private fun writeQuestionsAnswers(
        serializer: XmlSerializer,
        items: List<QuestionItemDomain>,
        type: String // Question or Answer
    ) {
        // Creo la etiqueta interrogante con su respectiva pregunta
        for ((index, item) in items.withIndex()) {
            serializer.startTag("", type)
            serializer.attribute("", "pos${type}", index.toString())

            for (content in item.content) {
                when (content) {
                    is QuestionContentDomain.Image -> {
                        serializer.startTag("", XmlMapper.toXmlContent(ContentType.IMAGE))
                        serializer.attribute("", Attributes.URI, "")
                        serializer.attribute("", Attributes.NAMEFILE, content.nameFile)
                        serializer.endTag("", XmlTagsV2.TEXTO)
                    }

                    QuestionContentDomain.None -> Unit

                    is QuestionContentDomain.Text -> {
                        serializer.startTag("", XmlMapper.toXmlContent(ContentType.IMAGE))
                        serializer.attribute("", XmlTagsV2.TEXTO, content.text)
                        serializer.endTag("", XmlMapper.toXmlContent(ContentType.IMAGE))
                    }
                }
            }

            serializer.endTag("", type)
        }
    }

    private fun obtenerDatosXMLV2(ruta: String, version: String): List<QAItemDomain> {
        val qaItemDomain = mutableListOf<QAItemDomain>()

        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val file = File(ruta)
        val doc = db.parse(file)

        //listaQA.addAll(QAItem(getQAXML(doc, QUESTION),getQAXML(doc, ANSWER)))
        //questionItems.addAll()
        getQAXML(qaItemDomain, doc, XmlMapper.toXmlQA(QAType.QUESTION), ruta, version)
        getQAXML(qaItemDomain, doc, XmlMapper.toXmlQA(QAType.ANSWER), ruta, version)

        return qaItemDomain
    }

    private fun getQAXML(
        qaItemDomain: MutableList<QAItemDomain>,
        doc: Document,
        typeContent: String,
        ruta: String,
        version: String
    ) {
        // Leer Questions
        val questionsNode = doc.getElementsByTagName(typeContent) //Question/Answer
        for (i in 0 until questionsNode.length) {
            val element = questionsNode.item(i) as Element
            val contentList = mutableListOf<QuestionContentDomain>()

            val texts = element.getElementsByTagName(XmlMapper.toXmlContent(ContentType.TEXT))
            for (j in 0 until texts.length) {
                val t = texts.item(j) as Element
                val textValue = t.getAttribute(XmlMapper.toXmlContent(ContentType.TEXT))
                val qcText = getColorRanges.invoke(textValue)

                contentList.add(
                    QuestionContentDomain.Text(
                        text = qcText.text,
                        colorRangeDomains = qcText.colorRangeDomains // tu lógica aquí
                    )
                )
            }

            val images = element.getElementsByTagName(XmlMapper.toXmlContent(ContentType.IMAGE))
            for (j in 0 until images.length) {
                val img = images.item(j) as Element
                //val uri = img.getAttribute(URI)
                val nameFile = img.getAttribute(Attributes.NAMEFILE)
                val uri = setSubstringPathUseCase.invoke(
                    path = ruta,
                    version = version,
                    nameFile = nameFile
                )
                contentList.add(
                    QuestionContentDomain.Image(uri, nameFile)
                )
            }

            val item = QuestionItemDomain(contentList)
            if (typeContent == XmlMapper.toXmlQA(QAType.QUESTION)) {
                qaItemDomain.add(QAItemDomain(question = ResponseDomain.Filled(item)))
            } else {
                val current = qaItemDomain[i]
                qaItemDomain[i] = current.copy(answer = ResponseDomain.Filled(item))
            }
        }
    }

    private fun obtenerDatosXMLV1(ruta: String, version: String): List<QAItemDomain> {
        val listaQA = mutableListOf<QAItemDomain>()

        val dbf = DocumentBuilderFactory.newInstance()

        try {
            val db = dbf.newDocumentBuilder()
            val filePath = File(ruta)
            val doc = db.parse(filePath)

            val cuestionario: NodeList = doc.getElementsByTagName(XmlTagsV2.INTERROGANTE)

            for (i in 0 until cuestionario.length) {

                val e = cuestionario.item(i) as Element

                val ques = e.getAttribute(XmlTagsV1.PREGUNTA)
                val ans = e.getAttribute(XmlTagsV1.RESPUESTA)

                // ---- PREGUNTA ----
                val preguntaContent = mutableListOf<QuestionContentDomain>()

                val preguntaProcesada = if (ques.contains(MediaPaths.BASERUTA_IMG_CIFRADO)) {
                    var decoded = setCifrarRutaImagenUseCase.invoke(ques, 26 - 3)
                    decoded = setSubstringPathUseCase.invoke(ruta, decoded, version)
                    val nameFile = decoded.substringAfterLast("/")
                    QuestionContentDomain.Image(uri = decoded, nameFile = nameFile)
                } else {
                    getColorRanges.invoke(ques)
                }

                preguntaContent.add(preguntaProcesada)
                val preguntaItem = QuestionItemDomain(preguntaContent.toList())

                // ---- RESPUESTA ----
                val respuestaContent = mutableListOf<QuestionContentDomain>()

                val respuestaProcesada = if (ans.contains(MediaPaths.BASERUTA_IMG_CIFRADO)) {
                    var decoded = setCifrarRutaImagenUseCase.invoke(ans, 26 - 3)
                    decoded = setSubstringPathUseCase.invoke(ruta, decoded, version)
                    val nameFile = decoded.substringAfterLast("/")
                    QuestionContentDomain.Image(uri = decoded, nameFile = nameFile)
                } else {
                    getColorRanges.invoke(ans)
                }

                respuestaContent.add(respuestaProcesada)
                val respuestaItem = QuestionItemDomain(respuestaContent.toList())


                listaQA.add(
                    QAItemDomain(
                        question = ResponseDomain.Filled(preguntaItem),
                        answer = ResponseDomain.Filled(respuestaItem)
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listaQA
    }

    override fun getXMLVersion(ruta: String): List<QAItemDomain> {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val file = File(ruta)
        val doc = db.parse(file)

        val versionNode = doc.getElementsByTagName(Structure.GUIAESTUDIO)
        val element = versionNode.item(0) as Element
        val version = element.getAttribute(Attributes.VERSION)

        //return obtenerDatosXMLV1(ruta)
        return if (version == Versions.VERSION1)
            obtenerDatosXMLV1(ruta, version)
        else
            obtenerDatosXMLV2(ruta, version)
    }

    override fun getAttributesGuide(file: File): GuideXmlModel {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(file)

        val versionNode = doc.getElementsByTagName(Structure.GUIAESTUDIO)
        val elementGuide = versionNode.item(0) as Element
        val version = elementGuide.getAttribute(Attributes.VERSION)

        val cuestionarioNode = doc.getElementsByTagName(Structure.CUESTIONARIO)
        val element = cuestionarioNode.item(0) as Element

        var name = ""
        val description = element.getAttribute(Attributes.DESCRIPCION)
        //var name = element.getAttribute("nombreGuia")
        if (version == "1.0") {
            val fileName = file.path.substringAfterLast("/")
            name = fileName
            name = name.replace(".xml", "")
        } else {
            name = element.getAttribute("nombreGuia")
        }

        return GuideXmlModel(
            nameGuide = name,
            description = description
        )
    }

    override fun setAttributesGuide(
        file: File,
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
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

        val versionNode = doc.getElementsByTagName(Structure.GUIAESTUDIO)
        val elementGuide = versionNode.item(0) as Element
        return elementGuide.getAttribute(Attributes.VERSION)
    }
}