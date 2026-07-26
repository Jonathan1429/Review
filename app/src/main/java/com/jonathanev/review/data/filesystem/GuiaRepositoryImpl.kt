package com.jonathanev.review.data.filesystem

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
import com.jonathanev.review.domain.result.ExistGuideV1Result
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.ReadGuideError
import com.jonathanev.review.domain.result.SaveGuideErrors
import com.jonathanev.review.domain.result.UpdateGuideError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
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
        val path = File(filePathResolver.mapToFolderPath(relativeGuidePath, PathKind.GUIAS).value)
        val allItems = path.listFiles().orEmpty()

        val listFiles = allItems
            .filter { file -> file.isFile }
            .filter { file ->
                file.extension == Extensions.XML_EXTENSION
            }

        val listFromFolders = allItems
            .filter { it.isDirectory }
            .flatMap { folder ->
                folder.listFiles().orEmpty()
                    .filter { file -> file.isFile }
                    .filter { file -> file.extension == Extensions.XML_EXTENSION }
            }

        return (listFiles + listFromFolders)
    }

    override fun hasGuides(relativeGuidePath: RelativeGuidePath): Flow<Boolean> {
        val path = File(filePathResolver.mapToFolderPath(relativeGuidePath, PathKind.GUIAS).value)
        val allItems = path.listFiles() ?: return flowOf(false)

        val hasDirectFile = allItems.any { file ->
            file.isFile && file.extension == Extensions.XML_EXTENSION
        }
        if (hasDirectFile) return flowOf(true)

        for (folder in allItems.filter { it.isDirectory }) {
            val hasFolderFile = folder.listFiles().orEmpty().any { file ->
                file.isFile && file.extension == Extensions.XML_EXTENSION
            }
            if (hasFolderFile) return flowOf(true)
        }

        return flowOf(false)
    }

    override fun getGuides(relativeGuidePath: RelativeGuidePath): Flow<List<GuideDomainModel>> {
        val result = listGuides(relativeGuidePath)
        val resultGuides = result.sortedBy { it.name }.mapNotNull { file ->
            when (val guideDomainModel: GuideResource<GuideDomainModel, ReadGuideError> =
                getAttributesGuide(file)) {
                is GuideResource.Error -> null
                is GuideResource.Success -> guideDomainModel.data
            }
        }
        _guidesRecovery = resultGuides
        return flowOf(resultGuides)
    }

    override suspend fun renameGuide(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        guideContext: GuideContext.Rename
    ): GuideResource<GuideDomainModel, UpdateGuideError> {
        val path = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = guideContext.guide,
            kind = PathKind.GUIAS
        )

        val tempFile = File("${path.value}.tmp")

        return try {
            val serializer = xmlSerializerFactory.create()

            fileOutputStreamFactory.create(tempFile.path).use { fos ->
                serializer.setOutput(fos, "UTF-8")

                try {
                    serializer.setFeature(
                        "http://xmlpull.org/v1/doc/features.html#indent-output",
                        true
                    )
                } catch (_: IllegalStateException) {
                }

                serializer.startDocument(null, true)
                serializer.startTag("", Structure.GUIAESTUDIO)
                serializer.attribute("", Attributes.VERSION, Versions.VERSION2)

                serializer.startTag("", Structure.CUESTIONARIO)
                serializer.attribute("", Attributes.NOMBREGUIA, guideContext.name.value)
                serializer.attribute("", Attributes.DESCRIPCION, guideContext.description.value)

                writeQuestionsAnswers(serializer, preguntas, QAType.QUESTION.toTagXml())
                writeQuestionsAnswers(serializer, respuestas, QAType.ANSWER.toTagXml())

                serializer.endTag("", Structure.CUESTIONARIO)
                serializer.endTag("", Structure.GUIAESTUDIO)
                serializer.endDocument()
            }

            val newGuideDomain = GuideDomainModel(
                GuideVersion.V2,
                guideContext.name.value,
                guideContext.description.value
            )
            val newPath = filePathResolver.getPathGuidesV2(
                guideDomainModel = newGuideDomain,
                kind = PathKind.GUIAS,
                relativeGuidePath = guideContext.relativeGuidePath
            )

            try {
                Files.move(
                    /* source = */ tempFile.toPath(),
                    /* target = */ Paths.get(newPath),
                    /* ...options = */ StandardCopyOption.REPLACE_EXISTING
                )
            } catch (_: IOException) {
                tempFile.delete()
                return GuideResource.Error(UpdateGuideError.WriteError)
            }

            if (newPath != path.value) {
                try {
                    File(path.value).delete()
                } catch (_: Exception) {

                }
            }

            GuideResource.Success(newGuideDomain)
        } catch (_: FileNotFoundException) {
            tempFile.delete()
            GuideResource.Error(UpdateGuideError.NotFound)
        } catch (_: Exception) {
            tempFile.delete()
            GuideResource.Error(UpdateGuideError.UnknownError)
        }
    }

    override suspend fun saveGuide(
        guideDomainModel: GuideDomainModel,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        relativeGuidePath: RelativeGuidePath,
    ): GuideResource<GuideDomainModel, SaveGuideErrors> {
        val currentPath = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = guideDomainModel,
            kind = PathKind.GUIAS
        )

        val finalFile = File(currentPath.value)
        val parentDir = finalFile.parentFile
            ?: throw IllegalStateException("El archivo no tiene directorio padre")

        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw IOException("No se pudo crear el directorio: ${parentDir.absolutePath}")
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
                } catch (_: IllegalStateException) {
                }
                serializer.startDocument(null, true)
                serializer.startTag("", Structure.GUIAESTUDIO)
                serializer.attribute("", Attributes.VERSION, Versions.VERSION2)

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

                if (finalFile.path != File(newPath).path) {
                    try {
                        finalFile.delete()
                    } catch (_: Exception) {
                    }
                }
                GuideResource.Success(
                    GuideDomainModel(
                        GuideVersion.V2,
                        guideDomainModel.nameGuide,
                        guideDomainModel.description
                    )
                )
            } catch (_: Exception) {
                tempFile.delete()
                GuideResource.Error(SaveGuideErrors.CommitChangesFailed)
            }
        } catch (_: IOException) {
            tempFile.delete()
            GuideResource.Error(SaveGuideErrors.InsufficientStorageOrDiskError)
        } catch (_: SecurityException) {
            tempFile.delete()
            GuideResource.Error(SaveGuideErrors.StoragePermissionDenied)
        }
    }

    override suspend fun deleteGuide(
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
                    deleteGuide.guide,
                    PathKind.GUIAS
                )
            }

        val pathGuideFile = File(pathGuide.value)

        if (!pathGuideFile.exists()) return false

        return pathGuideFile.deleteRecursively()
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
        val currentPath = File(guidePath.value)

        val qaItemXmlDto = mutableListOf<QAItemXmlDto>()
        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()

        try {
            val doc = db.parse(currentPath)

            getQAXML(
                qaItemXmlDto,
                doc,
                QAType.QUESTION.toTagXml(),
                currentPath.absolutePath,
                guideDomainModel.version
            )
            getQAXML(
                qaItemXmlDto,
                doc,
                QAType.ANSWER.toTagXml(),
                currentPath.absolutePath,
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

        } catch (_: FileNotFoundException) {
            return GetGuideResult.NotFound
        } catch (_: SAXException) {
            return GetGuideResult.InvalidFormat
        } catch (_: Exception) {
            return GetGuideResult.UnknownError
        }

        return GetGuideResult.Success(guideDomainModel, listaQA.map { it.toDomain() })
    }

    override suspend fun getXMLGuide(
        guideDomainModel: GuideDomainModel
    ): GetGuideResult {
        val version = guideDomainModel.version
        val path = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel,
            PathKind.GUIAS
        )
        return if (version == GuideVersion.V1)
            obtenerDatosXMLV1(guideDomainModel, path)
        else
            obtenerDatosXMLV2(guideDomainModel, path)
    }

    override fun existXMLGuideV1(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath
    ): ExistGuideV1Result {
        val pathComplete = File(
            filePathResolver.getPathGuidesV1(
                guideDomainModel,
                PathKind.GUIAS,
                relativeGuidePath
            )
        )

        return when (val guideDomainModel: GuideResource<GuideDomainModel, ReadGuideError> =
            getAttributesGuide(pathComplete)) {
            is GuideResource.Error -> ExistGuideV1Result.NoExistGuide
            is GuideResource.Success -> {
                val version = guideDomainModel.data.version
                if (version != GuideVersion.V1) return ExistGuideV1Result.NoExistGuide
                ExistGuideV1Result.ExistGuide
            }
        }
    }

    override suspend fun moveGuide(guideContext: GuideContext.Moving): Boolean {
        val newGuidePath = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = guideContext.guide,
            kind = PathKind.GUIAS
        )

        val oldGuidePath = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = guideContext.guide,
            kind = PathKind.GUIAS
        )

        return File(oldGuidePath.value).renameTo(File(newGuidePath.value))
    }

    override fun getVersionGuide(
        nameFile: String,
        relativeGuidePath: RelativeGuidePath
    ): GuideResource<GuideDomainModel, ReadGuideError> {
        val targetFile = listGuides(relativeGuidePath).find { file ->
            file.name == nameFile || file.nameWithoutExtension == nameFile
        } ?: return GuideResource.Error(ReadGuideError.FileNotFound)

        // 2. Leemos sus atributos directamente
        return getAttributesGuide(targetFile)
    }

    override fun existGuide(nameFile: String, relativeGuidePath: RelativeGuidePath): Boolean {
        // 2. Verificamos la existencia física (V1 o V2)
        return listGuides(relativeGuidePath).any { file ->
            file.name.equals(nameFile, ignoreCase = true) ||
                    file.nameWithoutExtension.equals(nameFile, ignoreCase = true)
        }
    }

    private fun getAttributesGuide(file: File): GuideResource<GuideDomainModel, ReadGuideError> {
        return try {
            val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = db.parse(file)

            // Normaliza el documento XML (que solo contienen espacios o saltos de línea sueltos entre etiquetas)
            doc.documentElement.normalize()

            // Si la etiqueta no existe, item(0) devuelve null. Usamos as? para evitar un NullPointerException al castear.
            val cuestionarioNode = doc
                .getElementsByTagName(Structure.CUESTIONARIO)
                .item(0) as? Element

            val guiaEstudioNode = doc
                .getElementsByTagName(Structure.GUIAESTUDIO)
                .item(0) as? Element

            val description = cuestionarioNode?.getAttribute(Attributes.DESCRIPCION).orEmpty()
            val version = guiaEstudioNode?.getAttribute(Attributes.VERSION).orEmpty()

            val name = if (version == Versions.VERSION1) {
                file.name.replace(Extensions.POINT_XML_EXTENSION, "")
            } else { // Diferente a V1
                cuestionarioNode?.getAttribute(Attributes.NOMBREGUIA).orEmpty()
            }

            if (listOf(version, name).any { it.isEmpty() }) {
                return GuideResource.Error(ReadGuideError.EmptyOrCorruptFile)
            }

            val domainModel = GuideXmlDto(
                version = version,
                nameGuide = name,
                description = description
            ).toDomain()

            GuideResource.Success(domainModel)
        } catch (_: FileNotFoundException) {
            GuideResource.Error(ReadGuideError.FileNotFound)
        } catch (_: SAXException) {
            GuideResource.Error(ReadGuideError.InvalidXmlFormat)
        } catch (e: Exception) {
            GuideResource.Error(ReadGuideError.UnknownErrorRead(e.localizedMessage))
        }
    }
}