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
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.xml.Attributes
import com.jonathanev.review.data.xml.Structure
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.data.xml.XmlTagsV1
import com.jonathanev.review.data.xml.XmlTagsV2
import com.jonathanev.review.domain.DirectoryManager
import com.jonathanev.review.domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.domain.SetSubstringPathUseCase
import com.jonathanev.review.domain.model.ContentType
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideSource
import com.jonathanev.review.domain.model.QAType
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.folders.model.FolderAction
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
    private val navigationPathRepository: NavigationPathRepository,
    private val filePathsProvider: FilePathsProvider,
    private val imagesRepository: ImagesRepository,
    private val directoryManager: DirectoryManager
) : GuiaRepository {
    private var _guidesRecovery = emptyList<GuideXmlModel>()
    override val guidesRecovery: List<GuideXmlModel>
        get() = _guidesRecovery

    override fun getNumGuides(): Int {
        return navigationPathRepository.currentPathGuides.listFiles()?.size ?: 0
    }

    override fun getGuides(): List<GuideXmlModel> {
        val listFiles = navigationPathRepository.currentPathGuides.listFiles()
            ?.filter { file ->
                file.isFile &&
                        file.extension == Extensions.XML_EXTENSION
            } ?: emptyList()

        val listFromFolders = navigationPathRepository.currentPathGuides
            .listFiles()
            ?.filter { it.isDirectory }?.map { folder ->
                folder.listFiles()?.find { file ->
                    file.isFile &&
                            file.extension == Extensions.XML_EXTENSION &&
                            file.nameWithoutExtension == (file.parentFile?.name ?: "")
                }
            } ?: emptyList()

        val result = (listFiles + listFromFolders)
        val resultGuides = result.map { file -> getAttributesGuide(file!!) }
        _guidesRecovery = resultGuides
        return resultGuides
    }

    override fun moveGuide(mode: FolderAction.MovingFile): Boolean {
        val oldPathGuide = if (mode.guideDomain.version == Versions.VERSION1) {
            filePathsProvider.buildGuide(
                mode.pathGuides,
                mode.guideDomain.nameGuide
            )
        } else {
            filePathsProvider.buildFolderGuide(
                mode.pathGuides,
                mode.guideDomain.nameGuide,
                mode.guideDomain.nameGuide
            )
        }

        val oldPathImages = if (mode.guideDomain.version == Versions.VERSION1) {
            mode.pathImages
        } else {
            filePathsProvider.buildFolder(
                mode.pathImages,
                mode.guideDomain.nameGuide
            )
        }

        val currentPath = if (mode.guideDomain.version == Versions.VERSION1) {
            filePathsProvider.buildGuide(
                navigationPathRepository.currentPathGuides,
                mode.guideDomain.nameGuide
            )
        } else {
            filePathsProvider.buildFolderGuide(
                navigationPathRepository.currentPathGuides,
                mode.guideDomain.nameGuide,
                mode.guideDomain.nameGuide
            )
        }

        val data = if (mode.guideDomain.version == Versions.VERSION1) {
            obtenerDatosXMLV1(mode.guideDomain, GuideSource.CustomPath(oldPathGuide.path))
        } else {
            obtenerDatosXMLV2(mode.guideDomain, GuideSource.CustomPath(oldPathGuide.path))
        }

        val tempQuestions =
            data.mapNotNull { (it.question as? ResponseXml.Filled)?.item }.toList()
        val tempAnswers =
            data.mapNotNull { (it.answer as? ResponseXml.Filled)?.item }.toList()

        val listImagesXml = (tempQuestions + tempAnswers).flatMap { it.content }
            .filterIsInstance<QuestionContentXml.Image>()

        val listImagesDomain = listImagesXml.map { it.toDomain() }
        if (mode.guideDomain.version == Versions.VERSION2) {
            directoryManager.createPathGuide(
                nameGuide = mode.guideDomain.nameGuide
            )
        }

        val successGuide = oldPathGuide.renameTo(currentPath)
        if (!successGuide) return false

        if (mode.guideDomain.version == Versions.VERSION2) {
            directoryManager.deleteFolderEmpty(oldPathGuide.parentFile!!.toString())
            directoryManager.createPathImages(
                guideDomainModel = mode.guideDomain,
                isNewFile = true
            )
        }

        directoryManager.moveImages(
            listImagesDomain,
            mode.guideDomain.nameGuide,
            mode.guideDomain.version,
            oldPathImages
        )
        return true
    }

    private fun QuestionContentXml.Image.toDomain(): QuestionContentDomain.Image {
        return QuestionContentDomain.Image(this.uri, this.nameFile)
    }

    override fun renameGuide(
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel
    ): Boolean {
        val currentPath = filePathsProvider.buildFolderGuide(
            navigationPathRepository.currentPathGuides,
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
                val pathV1 = filePathsProvider.buildGuide(
                    navigationPathRepository.currentPathGuides,
                    fileName
                )

                pathV1.delete()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    override fun saveGuide(
        nameGuide: String,
        description: String,
        version: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean {
        val currentPath = filePathsProvider.buildFolderGuide(
            navigationPathRepository.currentPathGuides,
            nameGuide,
            nameGuide
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

            writeQuestionsAnswers(serializer, preguntas, toXmlQA(QAType.QUESTION))
            writeQuestionsAnswers(serializer, respuestas, toXmlQA(QAType.ANSWER))

            serializer.endTag("", Structure.CUESTIONARIO)
            serializer.endTag("", Structure.GUIAESTUDIO)
            serializer.endDocument()

            fos.close()

            val isRenamed = tempFile.renameTo(currentPath)
            if (!isRenamed) {
                tempFile.delete()
                return false
            }

            if (version == Versions.VERSION1) {
                val pathV1 = filePathsProvider.buildGuide(
                    navigationPathRepository.currentPathGuides,
                    nameGuide
                )

                pathV1.delete()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    override fun deleteGuide(
        guideDomainModel: GuideDomainModel,
        listImages: List<QuestionContentDomain.Image>
    ): UIStopEvent {
        val pathGuide = if (guideDomainModel.version == Versions.VERSION1) {
            filePathsProvider.buildGuide(
                navigationPathRepository.currentPathGuides,
                guideDomainModel.nameGuide
            )
        } else {
            filePathsProvider.buildFolder(
                navigationPathRepository.currentPathGuides,
                guideDomainModel.nameGuide
            ) // Borrar desde la carpeta
        }

        return if (pathGuide.deleteRecursively()) {
            imagesRepository.deleteImages(guideDomainModel, listImages)
            UIStopEvent.DeleteGuideSuccess("Guia eliminada correctamente")
        } else {
            UIStopEvent.ShowMessage("No se pudo eliminar la guia")
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

    override fun moveGuides() {
        val currentPath = navigationPathRepository.currentPathGuides

        val guidesInDivice = currentPath.listFiles()
            ?.filter { it.isFile && it.extension == Extensions.XML_EXTENSION } ?: emptyList()

        if (guidesInDivice.isNotEmpty()) {
            val otrosDir = File(currentPath, StorageFolders.OTROS)
            if (!otrosDir.exists()) {
                otrosDir.mkdir()
            }

            guidesInDivice.forEach { file ->
                val newPath = File(otrosDir, file.name)
                file.renameTo(newPath)
            }

            imagesRepository.movingImagesToOtros()
        }
    }

    private fun obtenerDatosXMLV2(
        guideDomainModel: GuideDomainModel,
        guideSource: GuideSource = GuideSource.CurrentPath
    ): List<QAItemXml> {
        val currentPath = when (guideSource) {
            GuideSource.CurrentPath -> {
                filePathsProvider.buildFolderGuide(
                    navigationPathRepository.currentPathGuides,
                    guideDomainModel.nameGuide,
                    guideDomainModel.nameGuide
                )
            }

            is GuideSource.CustomPath -> {
                File(guideSource.path)
            }
        }

        val qaItemXml = mutableListOf<QAItemXml>()

        val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = db.parse(currentPath)

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

    private fun obtenerDatosXMLV1(
        guideDomainModel: GuideDomainModel,
        guideSource: GuideSource = GuideSource.CurrentPath
    ): List<QAItemXml> {
        val listaQA = mutableListOf<QAItemXml>()

        val dbf = DocumentBuilderFactory.newInstance()

        try {
            val db = dbf.newDocumentBuilder()

            val currentPath = when (guideSource) {
                GuideSource.CurrentPath -> {
                    filePathsProvider.buildGuide(
                        navigationPathRepository.currentPathGuides,
                        guideDomainModel!!.nameGuide
                    )
                }

                is GuideSource.CustomPath -> {
                    File(guideSource.path)
                }
            }

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

    override fun getXMLGuide(guideDomainModel: GuideDomainModel?): List<QAItemXml> {
        val version = guideDomainModel!!.version

        //return obtenerDatosXMLV1(ruta)
        return if (version == Versions.VERSION1)
            obtenerDatosXMLV1(guideDomainModel)
        else
            obtenerDatosXMLV2(guideDomainModel)
    }

    private fun getAttributesGuide(file: File): GuideXmlModel {
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
        val name = if(version == Versions.VERSION1) {
            file.name.replace(Extensions.POINT_XML_EXTENSION, "")
        } else {
            cuestionarioNode.getAttribute(Attributes.NOMBREGUIA)
        }

        return GuideXmlModel(
            version = version,
            nameGuide = name,
            description = description
        )
    }
}