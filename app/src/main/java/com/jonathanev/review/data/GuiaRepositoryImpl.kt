package com.jonathanev.review.data

import android.util.Xml
import com.jonathanev.review.data.mapper.toXmlContent
import com.jonathanev.review.data.mapper.toXmlQA
import com.jonathanev.review.data.media.MediaPaths
import com.jonathanev.review.data.model.GuideXmlModel
import com.jonathanev.review.data.model.QAItemXml
import com.jonathanev.review.data.model.QuestionContentXml
import com.jonathanev.review.data.model.QuestionItemXml
import com.jonathanev.review.data.model.ResponseXml
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.xml.Attributes
import com.jonathanev.review.data.xml.Structure
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.data.xml.XmlTagsV1
import com.jonathanev.review.data.xml.XmlTagsV2
import com.jonathanev.review.domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.domain.SetSubstringPathUseCase
import com.jonathanev.review.domain.model.ContentType
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QAType
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.FileNamingRules.XML_EXTENSION
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.repository.PathProvider
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
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setSubstringPathUseCase: SetSubstringPathUseCase,
    private val xmlSerializerFactory: XmlSerializerFactory,
    private val fileOutputStreamFactory: FileOutputStreamFactory,
    private val pathProvider: PathProvider,
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider
) : GuiaRepository {
    private var _guidesRecovery = emptyList<GuideXmlModel>()
    override val guidesRecovery: List<GuideXmlModel>
        get() = _guidesRecovery

    override fun getNumGuides(): Int {
        return navigationPathRepository.currentPath.listFiles()?.size ?: 0
    }

    override fun getGuides(): List<GuideXmlModel> {
        val listFiles = navigationPathRepository.currentPath.listFiles()
            ?.filter { file ->
                file.isFile &&
                        file.extension == XML_EXTENSION
            } ?: emptyList()

        val listFromFolders = navigationPathRepository.currentPath
            .listFiles()
            ?.filter { it.isDirectory }?.map { folder ->
                folder.listFiles()?.find { file ->
                    file.isFile &&
                            file.extension == XML_EXTENSION &&
                            file.nameWithoutExtension == (file.parentFile?.name ?: "")
                }
            } ?: emptyList()

        val result = (listFiles + listFromFolders)
        val resultGuides = result.map { file -> getAttributesGuide(file!!) }
        _guidesRecovery = resultGuides
        return resultGuides
    }

    /*private fun getGuidesFromFolder(folderId: String): List<GuideXmlModel> {
        val context = GuidePathContext(
            version = VERSION1,
            guideName = folderId,
            target = GuidePathTarget.GUIDES,
        )

        val pathFiles = pathProvider.resolveGuidePath(context)

        return pathFiles.listFiles()
            ?.filter { !it.name.endsWith(".json") }
            ?.mapNotNull { item ->
                runCatching {
                    getAttributesGuide(item)
                }.getOrNull()
            }
            ?: emptyList()
    }*/

    override fun renameGuide(
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel
    ): Boolean {
        val currentPath = filePathsProvider.buildFolderFile(
            navigationPathRepository.currentPath,
            attributesGuide.nameGuide,
            attributesGuide.nameGuide
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
            serializer.attribute("", Attributes.NOMBREGUIA, fileName)
            serializer.attribute("", Attributes.DESCRIPCION, description)

            writeQuestionsAnswers(serializer, preguntas, toXmlQA(QAType.QUESTION))
            writeQuestionsAnswers(serializer, respuestas, toXmlQA(QAType.ANSWER))

            serializer.endTag("", Structure.CUESTIONARIO)
            serializer.endTag("", Structure.GUIAESTUDIO)
            serializer.endDocument()

            fos.close()

            val isRenamed = !tempFile.renameTo(currentPath)
            if (!isRenamed) {
                tempFile.delete()
                return false
            }

            if (attributesGuide.version == Versions.VERSION1) {
                val pathV1 = filePathsProvider.buildFile(
                    navigationPathRepository.currentPath,
                    fileName
                )

                pathV1.delete()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    override fun saveFileV2(
        nameGuide: String,
        description: String,
        version: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean {
        val currentPath = filePathsProvider.buildFolderFile(
            navigationPathRepository.currentPath,
            nameGuide,
            nameGuide
        )
        /*val parentDir = currentPath.parent

        val newFile = File(
            /* parent = */ parentDir,
            /* child = */ "${nameGuide}.xml"
        )*/

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

            writeQuestionsAnswers(serializer, preguntas, toXmlQA(QAType.QUESTION))
            writeQuestionsAnswers(serializer, respuestas, toXmlQA(QAType.ANSWER))

            serializer.endTag("", Structure.CUESTIONARIO)
            serializer.endTag("", Structure.GUIAESTUDIO)
            serializer.endDocument()

            fos.close()

            val isRenamed = !tempFile.renameTo(currentPath)
            if (!isRenamed) {
                tempFile.delete()
                return false
            }

            if (version == Versions.VERSION1) {
                val pathV1 = filePathsProvider.buildFile(
                    navigationPathRepository.currentPath,
                    nameGuide
                )

                pathV1.delete()
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
                        serializer.startTag("", toXmlContent(ContentType.IMAGE))
                        serializer.attribute("", Attributes.URI, "")
                        serializer.attribute("", Attributes.NAMEFILE, content.nameFile)
                        serializer.endTag("", XmlTagsV2.IMAGEN)
                    }

                    QuestionContentDomain.None -> Unit

                    is QuestionContentDomain.Text -> {
                        serializer.startTag("", toXmlContent(ContentType.TEXT))
                        serializer.attribute("", XmlTagsV2.TEXTO, content.text)
                        serializer.endTag("", toXmlContent(ContentType.TEXT))
                    }
                }
            }

            serializer.endTag("", type)
        }
    }

    private fun obtenerDatosXMLV2(guideDomainModel: GuideDomainModel?): List<QAItemXml> {
        val currentPath = filePathsProvider.buildFolderFile(
            navigationPathRepository.currentPath,
            guideDomainModel!!.nameGuide,
            guideDomainModel.nameGuide
        )

        val qaItemXml = mutableListOf<QAItemXml>()

        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(currentPath)

        //listaQA.addAll(QAItem(getQAXML(doc, QUESTION),getQAXML(doc, ANSWER)))
        //questionItems.addAll()
        getQAXML(
            qaItemXml,
            doc,
            toXmlQA(QAType.QUESTION),
            (currentPath.parent ?: ""),
            guideDomainModel.version
        )
        getQAXML(
            qaItemXml,
            doc,
            toXmlQA(QAType.ANSWER),
            (currentPath.parent ?: ""),
            guideDomainModel.version
        )

        return qaItemXml
    }

    private fun getQAXML(
        qaItemDomain: MutableList<QAItemXml>,
        doc: Document,
        typeContent: String,
        path: String,
        version: String
    ) {
        // Leer Questions
        val questionsNode = doc.getElementsByTagName(typeContent) //Question/Answer
        for (i in 0 until questionsNode.length) {
            val element = questionsNode.item(i) as Element
            val contentList = mutableListOf<QuestionContentXml>()

            val texts = element.getElementsByTagName(toXmlContent(ContentType.TEXT))
            for (j in 0 until texts.length) {
                val t = texts.item(j) as Element
                val textValue = t.getAttribute(toXmlContent(ContentType.TEXT))
                //val qcText = getColorRanges.invoke(textValue)

                contentList.add(
                    QuestionContentXml.Text(
                        text = textValue,
                        colorRangeXml = emptyList()
                    )
                )
            }

            val images = element.getElementsByTagName(toXmlContent(ContentType.IMAGE))
            for (j in 0 until images.length) {
                val img = images.item(j) as Element
                //val uri = img.getAttribute(URI)
                val nameFile = img.getAttribute(Attributes.NAMEFILE)
                val uri = setSubstringPathUseCase.invoke(
                    path = path,
                    version = version,
                    nameFile = nameFile
                )
                contentList.add(
                    QuestionContentXml.Image(uri, nameFile)
                )
            }

            val item = QuestionItemXml(contentList)
            if (typeContent == toXmlQA(QAType.QUESTION)) {
                qaItemDomain.add(QAItemXml(question = ResponseXml.Filled(item)))
            } else {
                val current = qaItemDomain[i]
                qaItemDomain[i] = current.copy(answer = ResponseXml.Filled(item))
            }
        }
    }

    private fun obtenerDatosXMLV1(guideDomainModel: GuideDomainModel?): List<QAItemXml> {
        val listaQA = mutableListOf<QAItemXml>()

        val dbf = DocumentBuilderFactory.newInstance()

        try {
            val db = dbf.newDocumentBuilder()
            val currentPath = filePathsProvider.buildFile(
                navigationPathRepository.currentPath,
                guideDomainModel!!.nameGuide
            )
            val doc = db.parse(currentPath)

            val cuestionario: NodeList = doc.getElementsByTagName(XmlTagsV2.INTERROGANTE)

            for (i in 0 until cuestionario.length) {

                val e = cuestionario.item(i) as Element

                val ques = e.getAttribute(XmlTagsV1.PREGUNTA)
                val ans = e.getAttribute(XmlTagsV1.RESPUESTA)

                // ---- PREGUNTA ----
                val preguntaContent = mutableListOf<QuestionContentXml>()

                val preguntaProcesada = if (ques.contains(MediaPaths.BASERUTA_IMG_CIFRADO)) {
                    var decoded = setCifrarRutaImagenUseCase.invoke(ques, 26 - 3)
                    decoded = setSubstringPathUseCase.invoke(
                        currentPath.path,
                        decoded,
                        guideDomainModel.version
                    )
                    val nameFile = decoded.substringAfterLast("/")
                    QuestionContentXml.Image(uri = decoded, nameFile = nameFile)
                } else {
                    QuestionContentXml.Text(
                        text = ans,
                        colorRangeXml = emptyList()
                    )
                    //getColorRanges.invoke(ques)
                }

                preguntaContent.add(preguntaProcesada)
                val preguntaItem = QuestionItemXml(preguntaContent.toList())

                // ---- RESPUESTA ----
                val respuestaContent = mutableListOf<QuestionContentXml>()

                val respuestaProcesada = if (ans.contains(MediaPaths.BASERUTA_IMG_CIFRADO)) {
                    var decoded = setCifrarRutaImagenUseCase.invoke(ans, 26 - 3)
                    decoded = setSubstringPathUseCase.invoke(
                        currentPath.path,
                        decoded,
                        guideDomainModel.version
                    )
                    val nameFile = decoded.substringAfterLast("/")
                    QuestionContentXml.Image(uri = decoded, nameFile = nameFile)
                } else {
                    QuestionContentXml.Text(
                        text = ans,
                        colorRangeXml = emptyList()
                    )
                    //getColorRanges.invoke(ans)
                }

                respuestaContent.add(respuestaProcesada)
                val respuestaItem = QuestionItemXml(respuestaContent.toList())


                listaQA.add(
                    QAItemXml(
                        question = ResponseXml.Filled(preguntaItem),
                        answer = ResponseXml.Filled(respuestaItem)
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listaQA
    }

    override fun getXML(guideDomainModel: GuideDomainModel?): List<QAItemXml> {
        val version = guideDomainModel?.version!!

        //return obtenerDatosXMLV1(ruta)
        return if (version == Versions.VERSION1)
            obtenerDatosXMLV1(guideDomainModel)
        else
            obtenerDatosXMLV2(guideDomainModel)
    }

    /*override fun getAttributesGuide(): GuideXmlModel {
        val currentPath = File(pathProvider.getCurrentPath())
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(currentPath)
        return contentAttributesGuide(doc)
    }*/

    private fun getAttributesGuide(file: File): GuideXmlModel {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(file)
        return contentAttributesGuide(doc)
    }

    private fun contentAttributesGuide(doc: Document): GuideXmlModel {
        val cuestionarioNode = doc
            .getElementsByTagName(Structure.CUESTIONARIO)
            .item(0) as Element

        val guiaEstudioNode = doc
            .getElementsByTagName(Structure.GUIAESTUDIO)
            .item(0) as Element

        var name = cuestionarioNode.getAttribute(Attributes.NOMBREGUIA).replace(".$XML_EXTENSION", "")
        val description = cuestionarioNode.getAttribute(Attributes.DESCRIPCION)
        val version = guiaEstudioNode.getAttribute(Attributes.VERSION)

        return GuideXmlModel(
            version = version,
            nameGuide = name,
            description = description
        )
    }

    /*private fun contentAttributesGuide(doc: Document, currentPath: File): GuideXmlModel {
        val versionNode = doc.getElementsByTagName(Structure.GUIAESTUDIO)
        val elementGuide = versionNode.item(0) as Element
        val version = elementGuide.getAttribute(Attributes.VERSION)

        val cuestionarioNode = doc.getElementsByTagName(Structure.CUESTIONARIO)
        val element = cuestionarioNode.item(0) as Element

        var name = ""
        val description = element.getAttribute(Attributes.DESCRIPCION)
        //var name = element.getAttribute("nombreGuia")
        if (version == "1.0") {
            val fileName = currentPath.path.substringAfterLast("/")
            name = fileName
            name = name.replace(".xml", "")
        } else {
            name = element.getAttribute("nombreGuia")
        }

        return GuideXmlModel(
            nameGuide = name,
            description = description
        )
    }*/

    /*override fun setAttributesGuide(
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ): Boolean {
        return saveFileV2(
            fileName,
            description,
            version,
            preguntas,
            respuestas
        )
    }*/

    /*override fun getVersion(nameGuide: String): String {
        //val currentPath = pathProvider.getCurrentPath()
        val context = GuidePathContext()
        pathProvider.resolveGuidePath()
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(currentPath)

        val versionNode = doc.getElementsByTagName(Structure.GUIAESTUDIO)
        val elementGuide = versionNode.item(0) as Element
        return elementGuide.getAttribute(Attributes.VERSION)
    }*/
}