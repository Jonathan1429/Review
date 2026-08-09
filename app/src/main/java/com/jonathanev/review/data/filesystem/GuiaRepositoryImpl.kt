package com.jonathanev.review.data.filesystem

import com.jonathanev.review.core.media.MediaPaths
import com.jonathanev.review.data.mapper.xml.toDomain
import com.jonathanev.review.data.mapper.xml.toTagXml
import com.jonathanev.review.data.model.xml.GuideXmlDto
import com.jonathanev.review.data.model.xml.QAItemXmlDto
import com.jonathanev.review.data.model.xml.QuestionContentXmlDto
import com.jonathanev.review.data.model.xml.QuestionItemXmlDto
import com.jonathanev.review.data.util.LabelsHandler
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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
    private val labelsHandler: LabelsHandler,
    private val xmlSerializerFactory: XmlSerializerFactory,
    private val fileOutputStreamFactory: FileOutputStreamFactory,
    private val filePathResolver: FilePathResolver
) : GuiaRepository {
    private var _guidesRecovery = emptyList<GuideDomainModel>()
    override val guidesRecovery: List<GuideDomainModel>
        get() = _guidesRecovery
    private val refreshGuides = MutableStateFlow(System.currentTimeMillis())

    private suspend fun listGuides(): List<File> {
        val guidePath = filePathResolver.mapToFolderPath(PathKind.GUIAS)

        val path = File(guidePath.value)
        val allItems = path.listFiles().orEmpty()

        val listFiles = allItems
            .filter { file -> file.isFile }
            .filter { file -> file.extension == Extensions.XML_EXTENSION }

        val listFromFolders = allItems
            .filter { it.isDirectory }
            .flatMap { folder ->
                folder.listFiles().orEmpty()
                    .filter { file -> file.isFile }
                    .filter { file -> file.extension == Extensions.XML_EXTENSION }
            }

        return listFiles + listFromFolders
    }

    override fun hasGuides(): Flow<Boolean> = refreshGuides.map {
        val path = File(filePathResolver.mapToFolderPath(PathKind.GUIAS).value)
        val allItems = path.listFiles() ?: return@map false

        val hasDirectFile = allItems.any { file ->
            file.isFile && file.extension == Extensions.XML_EXTENSION
        }

        if (hasDirectFile) return@map true

        val hastFile = allItems.filter { it.isDirectory }.any { folder ->
            folder.listFiles().orEmpty().any { file ->
                file.isFile && file.extension == Extensions.XML_EXTENSION
            }
        }
        return@map hastFile
    }.flowOn(Dispatchers.IO)

    override fun getGuides(): Flow<List<GuideDomainModel>> {
        return refreshGuides.map {
            val result = listGuides()
            result.sortedBy { it.name }.mapNotNull { file ->
                when (val guideResource = getAttributesGuide(file)) {
                    is GuideResource.Error -> null
                    is GuideResource.Success -> guideResource.data
                }
            }.also { _guidesRecovery = it }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun renameGuide(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        guideContext: GuideContext.Rename
    ): GuideResource<GuideDomainModel, UpdateGuideError> = withContext(Dispatchers.IO) {
        val oldPath = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = guideContext.guide,
            kind = PathKind.GUIAS
        )

        val tempFile = File("${oldPath.value}.tmp")

        try {
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
            val newPathValue =
                filePathResolver.mapToFilePathSpecificGuide(newGuideDomain, PathKind.GUIAS).value
            val targetPath = Paths.get(newPathValue)

            targetPath.parent?.let { parentDir ->
                if (Files.notExists(parentDir)) {
                    Files.createDirectories(parentDir)
                }
            }

            Files.move(
                tempFile.toPath(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
            )

            if (newPathValue != oldPath.value) {
                val oldFile = File(oldPath.value)
                val oldParentFolder = oldFile.parentFile

                oldFile.delete()

                if (guideContext.guide.version == GuideVersion.V2 &&
                    oldParentFolder != null &&
                    oldParentFolder.isDirectory &&
                    oldParentFolder.listFiles()?.isEmpty() == true
                ) {
                    oldParentFolder.delete()
                }
            }

            refreshGuides.value = System.currentTimeMillis()
            GuideResource.Success(newGuideDomain)

        } catch (_: FileNotFoundException) {
            GuideResource.Error(UpdateGuideError.NotFound)
        } catch (_: IOException) {
            GuideResource.Error(UpdateGuideError.WriteError)
        } catch (_: Exception) {
            GuideResource.Error(UpdateGuideError.UnknownError)
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    override suspend fun saveGuide(
        guideDomainModel: GuideDomainModel,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
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

            val newPath =
                filePathResolver.mapToFilePathSpecificGuide(
                    guideDomainModel = GuideDomainModel(
                        version = GuideVersion.V2,
                        nameGuide = guideDomainModel.nameGuide,
                        description = guideDomainModel.description
                    ),
                    kind = PathKind.GUIAS
                ).value

            try {
                withContext(Dispatchers.IO) {
                    Files.move(
                        tempFile.toPath(),
                        File(newPath).toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )
                }

                if (finalFile.path != File(newPath).path) {
                    try {
                        finalFile.delete()
                    } catch (_: Exception) {
                    }
                }

                refreshGuides.value = System.currentTimeMillis()
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
        val pathGuide = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = deleteGuide.guide,
            kind = PathKind.GUIAS
        )

        val pathGuideFile = File(pathGuide.value)

        if (!pathGuideFile.exists()) return false

        refreshGuides.value = System.currentTimeMillis()
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
                val simpleText = labelsHandler.sanitizeLabels(textValue)
                val processAndSanitizeLabels = labelsHandler.processAndSanitizeLabels(simpleText)
                contentList.add(processAndSanitizeLabels)
            }

            val images = element.getElementsByTagName(ContentType.IMAGE.toTagXml())
            for (j in 0 until images.length) {
                val img = images.item(j) as Element
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
                    val simpleText = labelsHandler.sanitizeLabels(ques)

                    QuestionContentXmlDto.Text(
                        text = simpleText,
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
                    val simpleText = labelsHandler.sanitizeLabels(ans)
                    QuestionContentXmlDto.Text(
                        text = simpleText,
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

    override suspend fun existXMLGuideV1(
        guideDomainModel: GuideDomainModel
    ): ExistGuideV1Result {
        val pathComplete = File(
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            ).value
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

    override suspend fun moveGuide(
        guideContext: GuideContext.Moving
    ): Boolean {
        val newGuidePath = filePathResolver.mapToFilePathSpecificGuide(
            guideDomainModel = guideContext.guide,
            kind = PathKind.GUIAS
        )

        val oldGuidePath = filePathResolver.mapToOldFolderPathSpecificGuide(
            guideDomainModel = guideContext.guide,
            kind = PathKind.GUIAS,
            originContext = guideContext
        )

        return withContext(Dispatchers.IO) {
            try {
                val source = Paths.get(oldGuidePath.value)
                val target = Paths.get(newGuidePath.value)

                target.parent?.let { Files.createDirectories(it) }

                Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
                )

                refreshGuides.value = System.currentTimeMillis()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    override suspend fun getVersionGuide(
        nameFile: String,
    ): GuideResource<GuideDomainModel, ReadGuideError> {
        val targetFile = listGuides().find { file ->
            file.name == nameFile || file.nameWithoutExtension == nameFile
        } ?: return GuideResource.Error(ReadGuideError.FileNotFound)

        // 2. Leemos sus atributos directamente
        return getAttributesGuide(targetFile)
    }

    override suspend fun existGuide(
        nameFile: String
    ): Boolean {
        // 2. Verificamos la existencia física (V1 o V2)
        return listGuides().any { file ->
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