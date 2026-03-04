package com.jonathanev.review.data.filesystem

import android.util.Log
import com.jonathanev.review.core.media.MediaPaths
import com.jonathanev.review.data.mapper.xml.toDomain
import com.jonathanev.review.data.mapper.xml.toTagXml
import com.jonathanev.review.data.model.xml.GuideXmlDto
import com.jonathanev.review.data.model.xml.QAItemXmlDto
import com.jonathanev.review.data.model.xml.QuestionContentXmlDto
import com.jonathanev.review.data.model.xml.QuestionItemXmlDto
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
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.QAType
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.FileOutputStreamFactory
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.XmlSerializerFactory
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.GetSaveGuideResult
import com.jonathanev.review.domain.result.SaveGuideError
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

@Singleton
class GuiaRepositoryImpl @Inject constructor(
    private val pathHandler: PathHandler,
    private val xmlSerializerFactory: XmlSerializerFactory,
    private val fileOutputStreamFactory: FileOutputStreamFactory,
    private val filePathResolver: FilePathResolver
) : GuiaRepository {
    private var _guidesRecovery = emptyList<GuideDomainModel>()
    override val guidesRecovery: List<GuideDomainModel>
        get() = _guidesRecovery

    private fun listGuides(relativeGuidePath: RelativeGuidePath): List<File> {
        val path = filePathResolver.mapToFolderPath(relativeGuidePath, PathKind.GUIAS)
        val listFiles = File(path.value).listFiles()
            ?.filter { file ->
                file.isFile &&
                        file.extension == Extensions.XML_EXTENSION
            } ?: emptyList()

        val listFromFolders = File(path.value)
            .listFiles()
            ?.filter { it.isDirectory }
            ?.flatMap { folder ->
                folder.listFiles()
                    ?.filter { it.isFile && it.extension == Extensions.XML_EXTENSION }
                    ?: emptyList()
            } ?: emptyList()

        return (listFiles + listFromFolders)
    }

    override fun getNumGuides(relativeGuidePath: RelativeGuidePath): Int {
        val result = listGuides(relativeGuidePath).size
        return result
    }

    override fun getGuides(relativeGuidePath: RelativeGuidePath): List<GuideDomainModel> {
        val result = listGuides(relativeGuidePath)
        val resultGuides = result.sortedBy { it.name }.map { file -> getAttributesGuide(file) }
        _guidesRecovery = resultGuides
        return resultGuides
    }

    override fun renameGuide(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        guideContext: GuideContext.Rename
    ): Boolean {
        val path = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = guideContext.guide,
            relativeGuidePath = guideContext.relativeGuidePath,
            kind = PathKind.GUIAS
        )

        val tempFile = File("${path.value}.tmp")

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

            val newPath = filePathResolver.getPathGuidesV2(
                GuideDomainModel(
                    GuideVersion.V2,
                    guideContext.name.value,
                    guideContext.description.value
                ),
                PathKind.GUIAS,
                guideContext.relativeGuidePath
            )
            val isRenamed = tempFile.renameTo(File(newPath))

            if (!isRenamed) {
                tempFile.delete()
                return false
            }

            if (newPath != path.value){
                File(path.value).delete()
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    override fun saveGuide(
        guideDomainModel: GuideDomainModel,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        relativeGuidePath: RelativeGuidePath,
    ): GetSaveGuideResult {
        val currentPath =
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomainModel,
                relativeGuidePath = relativeGuidePath,
                kind = PathKind.GUIAS
            )

        val finalFile = File(currentPath.value)

        val parentDir = finalFile.parentFile
            ?: throw IllegalStateException("El archivo no tiene directorio padre")

        if (!parentDir.exists()) {
            val created = parentDir.mkdirs()
            if (!created) {
                throw IOException("No se pudo crear el directorio: ${parentDir.absolutePath}")
            }
        }

        val tempFile = File("$finalFile.tmp")

        return try {
            val serializer = xmlSerializerFactory.create()
            fileOutputStreamFactory.create(tempFile.path).use { fos ->
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
            }

            val newPath = filePathResolver.getPathGuidesV2(
                guideDomainModel,
                PathKind.GUIAS,
                relativeGuidePath
            )

            try {
                Files.move(
                    tempFile.toPath(),
                    File(newPath).toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            } catch (_: Exception) {
                tempFile.delete()
                return GetSaveGuideResult.Failure(SaveGuideError.ErrorSave)
            }

            if (finalFile.exists() && finalFile.path != File(newPath).path) {
                finalFile.delete()
            }

            GetSaveGuideResult.SaveGuide
        } catch (_: IOException) {
            return GetSaveGuideResult.Failure(SaveGuideError.IOException)
        } catch (_: SecurityException) {
            return GetSaveGuideResult.Failure(SaveGuideError.SecurityException)
        }
    }

    override fun deleteGuide(
        deleteGuide: GuideContext.DeleteGuide
    ): Boolean {
        val pathGuide =
            if (deleteGuide.guide.version == GuideVersion.V2) {
                val relativeGuidePath = filePathResolver.mapToJoinRelativePath(
                    deleteGuide.relativeGuidePath,
                    deleteGuide.guide.nameGuide
                )
                filePathResolver.mapToFolderPath(
                    relativeGuidePath,
                    PathKind.GUIAS
                )
            } else {
                filePathResolver.mapToFilePathSpecificGuide(
                    deleteGuide.guide, deleteGuide.relativeGuidePath,
                    PathKind.GUIAS
                )
            }

        return File(pathGuide.value).deleteRecursively()
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
        guidePath: GuidePath
    ): GetGuideResult {
        val currentPath = guidePath.value

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
        } catch (_: FileNotFoundException) {
            return GetGuideResult.NotFound
        } catch (_: SAXException) {
            return GetGuideResult.InvalidFormat
        } catch (_: Exception) {
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

            if (typeContent == QAType.QUESTION.toTagXml()) {
                qaItemDomain.add(
                    QAItemXmlDto(
                        question = QuestionItemXmlDto(content = contentList),
                        answer = QuestionItemXmlDto(content = emptyList())
                    )
                )
            } else {
                val current = qaItemDomain[i]
                qaItemDomain[i] = current.copy(answer = QuestionItemXmlDto(content = contentList))
            }
        }
    }

    private fun obtenerDatosXMLV1(
        guideDomainModel: GuideDomainModel,
        guidePath: GuidePath
    ): GetGuideResult {
        val listaQA = mutableListOf<QAItemXmlDto>()
        val dbf = DocumentBuilderFactory.newInstance()

        try {
            val db = dbf.newDocumentBuilder()

            val currentPath = guidePath.value
            Log.d("PATH", currentPath)

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
                        text = ques,
                        colorRangeXmlDto = emptyList()
                    )
                }

                preguntaContent.add(preguntaProcesada)

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

                listaQA.add(
                    QAItemXmlDto(
                        question = QuestionItemXmlDto(content = preguntaContent.toList()),
                        answer = QuestionItemXmlDto(content = respuestaContent.toList())
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

    override fun getXMLGuide(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath
    ): GetGuideResult {
        val version = guideDomainModel.version
        val path = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel,
            relativeGuidePath,
            PathKind.GUIAS
        )
        return if (version == GuideVersion.V1)
            obtenerDatosXMLV1(guideDomainModel, path)
        else
            obtenerDatosXMLV2(guideDomainModel, path)
    }

    override fun moveGuide(guideContext: GuideContext.Moving): Boolean {
        val newGuidePath = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = guideContext.guide,
            relativeGuidePath = guideContext.relativeGuidePath,
            kind = PathKind.GUIAS
        )

        val oldGuidePath = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = guideContext.guide,
            relativeGuidePath = guideContext.oldRelativeGuidePath,
            kind = PathKind.GUIAS
        )

        return File(oldGuidePath.value).renameTo(File(newGuidePath.value))
    }

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
}