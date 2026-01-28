package com.jonathanev.review.data.filesystem

import android.util.Xml
import com.jonathanev.review.core.media.MediaPaths
import com.jonathanev.review.data.mapper.xml.toDomain
import com.jonathanev.review.data.mapper.xml.toTagXml
import com.jonathanev.review.data.model.xml.GuideXmlDto
import com.jonathanev.review.data.model.xml.QAItemXmlDto
import com.jonathanev.review.data.model.xml.QuestionContentXmlDto
import com.jonathanev.review.data.model.xml.QuestionItemXmlDto
import com.jonathanev.review.data.model.xml.ResponseXmlDto
import com.jonathanev.review.data.util.PathHandler
import com.jonathanev.review.data.xml.Attributes
import com.jonathanev.review.data.xml.Structure
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.data.xml.XmlTagsV1
import com.jonathanev.review.data.xml.XmlTagsV2
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.model.ContentType
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideSource
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QAType
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.service.FilePathResolverService
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileNotFoundException
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
    private val pathHandler: PathHandler,
    private val xmlSerializerFactory: XmlSerializerFactory,
    private val fileOutputStreamFactory: FileOutputStreamFactory,
    private val filePathResolverService: FilePathResolverService
) : GuiaRepository {
    private var _guidesRecovery = emptyList<GuideDomainModel>()
    override val guidesRecovery: List<GuideDomainModel>
        get() = _guidesRecovery

    private fun listGuides(currentPathGuides: String): List<File?> {
        val listFiles = File(currentPathGuides).listFiles()
            ?.filter { file ->
                file.isFile &&
                        file.extension == Extensions.XML_EXTENSION
            } ?: emptyList()

        val listFromFolders = File(currentPathGuides)
            .listFiles()
            ?.filter { it.isDirectory }
            ?.flatMap { folder ->
                folder.listFiles()
                    ?.filter { it.isFile && it.extension == Extensions.XML_EXTENSION }
                    ?: emptyList()
            } ?: emptyList()

        return (listFiles + listFromFolders)
    }

    override fun getNumGuides(currentPathGuides: String): Int {
        val result = listGuides(currentPathGuides).size
        return result
    }

    override fun getGuides(currentPathGuides: String): List<GuideDomainModel> {
        val result = listGuides(currentPathGuides)
        val resultGuides = result.map { file -> getAttributesGuide(file!!) }
        _guidesRecovery = resultGuides
        return resultGuides
    }

    override fun renameGuide(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        guideContext: GuideContext.Rename
    ): Boolean {
        val path = filePathResolverService.mapToFilePathRename(guideContext)
        val tempFile = File("$path.tmp")

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
            serializer.attribute("", Attributes.NOMBREGUIA, guideContext.name.value)
            serializer.attribute("", Attributes.DESCRIPCION, guideContext.description.value)

            writeQuestionsAnswers(serializer, preguntas, QAType.QUESTION.toTagXml())
            writeQuestionsAnswers(serializer, respuestas, QAType.ANSWER.toTagXml())

            serializer.endTag("", Structure.CUESTIONARIO)
            serializer.endTag("", Structure.GUIAESTUDIO)
            serializer.endDocument()

            fos.close()

            val newPath = filePathResolverService.renamePathGuidesV2(guideContext)
            val isRenamed = tempFile.renameTo(File(newPath))

            if (!isRenamed){
                tempFile.delete()
                return false
            }

            File(path).delete()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun saveGuide(
        guideDomainModel: GuideDomainModel,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean {
        val currentPath = filePathResolverService.mapToFilePathActual(GuideContext.Actual(guideDomainModel))
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
            serializer.attribute("", Attributes.NOMBREGUIA, guideDomainModel.nameGuide)
            serializer.attribute("", Attributes.DESCRIPCION, guideDomainModel.description)

            writeQuestionsAnswers(serializer, preguntas, QAType.QUESTION.toTagXml())
            writeQuestionsAnswers(serializer, respuestas, QAType.ANSWER.toTagXml())

            serializer.endTag("", Structure.CUESTIONARIO)
            serializer.endTag("", Structure.GUIAESTUDIO)
            serializer.endDocument()

            fos.close()

            val newPath = filePathResolverService.getPathGuidesV2(guideDomainModel)
            val isRenamed = tempFile.renameTo(File(newPath))

            if (!isRenamed){
                tempFile.delete()
                return false
            }

            if (guideDomainModel.version == GuideVersion.V1){
                File(currentPath).delete()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun deleteGuide(
        deleteGuide: GuideContext.DeleteGuide
    ): Boolean {
        val pathGuide = filePathResolverService.mapToFilePathDelete(deleteGuide)
        return File(pathGuide).deleteRecursively()
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
                        serializer.startTag("", ContentType.IMAGE.toTagXml())
                        serializer.attribute("", Attributes.URI, "")
                        serializer.attribute("", Attributes.NAMEFILE, content.nameFile)
                        serializer.endTag("", XmlTagsV2.IMAGEN)
                    }

                    QuestionContentDomain.None -> Unit

                    is QuestionContentDomain.Text -> {
                        serializer.startTag("", ContentType.TEXT.toTagXml())
                        serializer.attribute("", XmlTagsV2.TEXTO, content.text)
                        serializer.endTag("", ContentType.TEXT.toTagXml())
                    }
                }
            }

            serializer.endTag("", type)
        }
    }

    private fun obtenerDatosXMLV2(
        guideDomainModel: GuideDomainModel,
        guideSource: GuideSource
    ): GetGuideResult {
        val currentPath = when (guideSource) {
            is GuideSource.CurrentPath -> {
                guideSource.path.value
            }

            is GuideSource.CustomPath -> {
                guideSource.path.value
                /*filePathsProvider.buildFolderGuide(
                    guideSource.path.value,
                    guideDomainModel.nameGuide,
                    guideFileName
                )*/
            }
        }

        val qaItemXmlDto = mutableListOf<QAItemXmlDto>()
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()

        try {
            val doc = db.parse(File(currentPath))

            getQAXML(
                qaItemXmlDto,
                doc,
                QAType.QUESTION.toTagXml(),
                (File(currentPath).parent ?: ""),
                guideDomainModel.version
            )
            getQAXML(
                qaItemXmlDto,
                doc,
                QAType.ANSWER.toTagXml(),
                (File(currentPath).parent ?: ""),
                guideDomainModel.version
            )

            return GetGuideResult.Success(guideDomainModel, qaItemXmlDto.map { it.toDomain() })
        } catch (e: FileNotFoundException) {
            return GetGuideResult.NotFound
        } catch (e: SAXException) {
            return GetGuideResult.InvalidFormat
        } catch (e: Exception) {
            return GetGuideResult.UnknownError
        }
    }

    private fun getQAXML(
        qaItemDomain: MutableList<QAItemXmlDto>,
        doc: Document,
        typeContent: String,
        path: String,
        version: GuideVersion
    ) {
        // Leer Questions
        val questionsNode = doc.getElementsByTagName(typeContent) //Question/Answer
        for (i in 0 until questionsNode.length) {
            val element = questionsNode.item(i) as Element
            val contentList = mutableListOf<QuestionContentXmlDto>()

            val texts = element.getElementsByTagName(ContentType.TEXT.toTagXml())
            for (j in 0 until texts.length) {
                val t = texts.item(j) as Element
                val textValue = t.getAttribute(ContentType.TEXT.toTagXml())

                contentList.add(
                    QuestionContentXmlDto.Text(
                        text = textValue,
                        colorRangeXmlDto = emptyList()
                    )
                )
            }

            val images = element.getElementsByTagName(ContentType.IMAGE.toTagXml())
            for (j in 0 until images.length) {
                val img = images.item(j) as Element
                //val uri = img.getAttribute(URI)
                val nameFile = img.getAttribute(Attributes.NAMEFILE)
                val uri = pathHandler.getSubstringPath(
                    path = path,
                    version = version,
                    nameFile = nameFile
                )
                contentList.add(
                    QuestionContentXmlDto.Image(uri, nameFile)
                )
            }

            val item = QuestionItemXmlDto(contentList)
            if (typeContent == QAType.QUESTION.toTagXml()) {
                qaItemDomain.add(QAItemXmlDto(question = ResponseXmlDto.Filled(item)))
            } else {
                val current = qaItemDomain[i]
                qaItemDomain[i] = current.copy(answer = ResponseXmlDto.Filled(item))
            }
        }
    }

    private fun obtenerDatosXMLV1(
        guideDomainModel: GuideDomainModel,
        guideSource: GuideSource
    ): GetGuideResult {
        val listaQA = mutableListOf<QAItemXmlDto>()
        val dbf = DocumentBuilderFactory.newInstance()

        try {
            val db = dbf.newDocumentBuilder()

            val currentPath = when (guideSource) {
                is GuideSource.CurrentPath -> {
                    guideSource.path.value
                }

                is GuideSource.CustomPath -> {
                    guideSource.path.value
                    /*filePathsProvider.buildGuide(
                        guideSource.path.value,
                        guideFileName
                    )*/
                }
            }

            val doc = db.parse(File(currentPath))

            val cuestionario: NodeList = doc.getElementsByTagName(XmlTagsV2.INTERROGANTE)

            for (i in 0 until cuestionario.length) {

                val e = cuestionario.item(i) as Element

                val ques = e.getAttribute(XmlTagsV1.PREGUNTA)
                val ans = e.getAttribute(XmlTagsV1.RESPUESTA)

                // ---- PREGUNTA ----
                val preguntaContent = mutableListOf<QuestionContentXmlDto>()

                val preguntaProcesada = if (ques.contains(MediaPaths.ENCRYPTED_IMAGE_BASE_PATH)) {
                    var decoded = pathHandler.encrypt(ques)
                    decoded = pathHandler.getSubstringPath(
                        currentPath,
                        decoded,
                        guideDomainModel.version
                    )
                    val nameFile = decoded.substringAfterLast("/")
                    QuestionContentXmlDto.Image(uri = decoded, nameFile = nameFile)
                } else {
                    QuestionContentXmlDto.Text(
                        text = ans,
                        colorRangeXmlDto = emptyList()
                    )
                }

                preguntaContent.add(preguntaProcesada)
                val preguntaItem = QuestionItemXmlDto(preguntaContent.toList())

                // ---- RESPUESTA ----
                val respuestaContent = mutableListOf<QuestionContentXmlDto>()

                val respuestaProcesada = if (ans.contains(MediaPaths.ENCRYPTED_IMAGE_BASE_PATH)) {
                    var decoded = pathHandler.encrypt(ans)
                    decoded = pathHandler.getSubstringPath(
                        currentPath,
                        decoded,
                        guideDomainModel.version
                    )
                    val nameFile = decoded.substringAfterLast("/")
                    QuestionContentXmlDto.Image(uri = decoded, nameFile = nameFile)
                } else {
                    QuestionContentXmlDto.Text(
                        text = ans,
                        colorRangeXmlDto = emptyList()
                    )
                }

                respuestaContent.add(respuestaProcesada)
                val respuestaItem = QuestionItemXmlDto(respuestaContent.toList())


                listaQA.add(
                    QAItemXmlDto(
                        question = ResponseXmlDto.Filled(preguntaItem),
                        answer = ResponseXmlDto.Filled(respuestaItem)
                    )
                )
            }

        } catch (e: FileNotFoundException) {
            return GetGuideResult.NotFound
        } catch (e: SAXException) {
            return GetGuideResult.InvalidFormat
        } catch (e: Exception) {
            return GetGuideResult.UnknownError
        }

        return GetGuideResult.Success(guideDomainModel, listaQA.map { it.toDomain() })
    }

    override fun getXMLGuide(guideContext: GuideContext.Actual): GetGuideResult {
        val version = guideContext.guide.version

        val path = filePathResolverService.mapToFilePathActual(guideContext)
        return if (version == GuideVersion.V1)
            obtenerDatosXMLV1(guideContext.guide, GuideSource.CurrentPath(GuidePath(path)))
        else
            obtenerDatosXMLV2(guideContext.guide, GuideSource.CurrentPath(GuidePath(path)))
    }

    override fun moveGuide(guideContext: GuideContext.Moving): Boolean {
        val guidePath = filePathResolverService.mapToTargetGuidePath(
            GuideContext.Moving(
                guideContext.guide,
                guideContext.oldGuidePath,
                guideContext.oldImagePath
            )
        )

        val sourceGuidePath = filePathResolverService.mapToSourceGuidePath(
            GuideContext.Moving(
                guideContext.guide,
                guideContext.oldGuidePath,
                guideContext.oldImagePath
            )
        )

        //val guidePath = guideContext.currentGuidePath
        //val sourceGuidePath = guideContext.oldGuidePath
        //val currentGuidePath = getGuidePath(guidePath.value, guideContext.guide, guideFileName)
        //val oldGuidePath = getGuidePath(sourceGuidePath.value, guideContext.guide, guideFileName)
        return File(sourceGuidePath).renameTo(File(guidePath))
    }

    /*private fun getGuidePath(
        sourceGuidePath: String,
        guideDomain: GuideDomainModel,
        guideFileName: String
    ): String {
        return if (guideDomain.version == GuideVersion.V1) {
            filePathsProvider.buildGuide(
                sourceGuidePath,
                guideFileName
            )
        } else {
            filePathsProvider.buildFolderGuide(
                sourceGuidePath,
                guideDomain.nameGuide,
                guideFileName
            )
        }
    }*/

    private fun getAttributesGuide(file: File): GuideDomainModel {
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(file)
        val cuestionarioNode = doc
            .getElementsByTagName(Structure.CUESTIONARIO)
            .item(0) as Element

        val guiaEstudioNode = doc
            .getElementsByTagName(Structure.GUIAESTUDIO)
            .item(0) as Element

        val description = cuestionarioNode.getAttribute(Attributes.DESCRIPCION)
        val version = guiaEstudioNode.getAttribute(Attributes.VERSION)
        val name = if (version == Versions.VERSION1) {
            file.name.replace(Extensions.POINT_XML_EXTENSION, "")
        } else {
            cuestionarioNode.getAttribute(Attributes.NOMBREGUIA)
        }

        return GuideXmlDto(
            version = version,
            nameGuide = name,
            description = description
        ).toDomain()
    }

    /*private fun getCurrentPath(guideDomainModel: GuideDomainModel, basePath: String = navigationPathRepository.currentPathGuides.value) = if (guideDomainModel.version == GuideVersion.V1) {
        val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
        filePathsProvider.buildGuide(
            basePath,
            file
        )
    } else {
        val file = FileNamingRules.buildXmlFileName(guideDomainModel.nameGuide)
        filePathsProvider.buildFolderGuide(
            basePath,
            guideDomainModel.nameGuide,
            file
        )
    }*/
}