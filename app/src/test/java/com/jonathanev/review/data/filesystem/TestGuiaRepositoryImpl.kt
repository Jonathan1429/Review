package com.jonathanev.review.data.filesystem

import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.util.PathHandler
import com.jonathanev.review.data.xml.Attributes
import com.jonathanev.review.data.xml.Structure
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.data.xml.XmlTagsV1
import com.jonathanev.review.data.xml.XmlTagsV2
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.FileOutputStreamFactory
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.XmlSerializerFactory
import com.jonathanev.review.domain.result.ExistGuideV1Result
import com.jonathanev.review.domain.result.GetGuideResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.File
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class TestGuiaRepositoryImpl {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var repository: GuiaRepositoryImpl

    // Instancia real de tu PathHandler
    private val pathHandler = PathHandler()

    private val xmlSerializerFactory: XmlSerializerFactory = mockk()
    private val fileOutputStreamFactory: FileOutputStreamFactory = mockk()
    private val filePathResolver: FilePathResolver = mockk()
    private lateinit var xmlTestV2: String
    private lateinit var xmlTestIntegracionV2: String
    private lateinit var xmlSintaxisV1: String
    private lateinit var xmlBuclesV1: String
    private lateinit var folderKotlin: String
    private lateinit var folderTest: String
    private lateinit var folderFiles: String

    @Before
    fun setUp() {
        folderKotlin = "Kotlin"
        folderTest = "Test"
        folderFiles = "files"
        xmlSintaxisV1 = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION1}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Sintaxis">
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="Pregunta 1" ${XmlTagsV1.RESPUESTA}="Respuesta 1"/>
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/1.sqj" ${XmlTagsV1.RESPUESTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/2.sqj"/>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()
        xmlBuclesV1 = xmlSintaxisV1.replace("Sintaxis", "Bucles")
        xmlTestV2 = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Test">
                <${XmlTagsV2.QUESTION} posQuestion="0">  
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Pregunta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="1${Extensions.POINT_PNG_EXTENSION}"/>
                </${XmlTagsV2.QUESTION}> 
                <${XmlTagsV2.ANSWER} posAnswer="0"> 
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Respuesta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="2${Extensions.POINT_PNG_EXTENSION}"/>
                </${XmlTagsV2.ANSWER}>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()
        xmlTestIntegracionV2 = xmlTestV2.replace("Test", "Test Integracion")
        repository = GuiaRepositoryImpl(
            pathHandler = pathHandler,
            xmlSerializerFactory = xmlSerializerFactory,
            fileOutputStreamFactory = fileOutputStreamFactory,
            filePathResolver = filePathResolver
        )
    }

    // getNumGuides
    // Guias V1
    @Test
    fun regresa_el_num_de_guias_V1_y_V2_dentro_de_la_ruta() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val folderTest = File(pathKotlin, folderTest)
        folderTest.mkdirs()

        every {
            filePathResolver.mapToFolderPath(relativePath, PathKind.GUIAS)
        } returns GuidePath(pathKotlin.absolutePath)

        //Guides V1
        File(pathKotlin, "Sintaxis.${Extensions.XML_EXTENSION}").createNewFile()
        File(pathKotlin, "Documentacion.${Extensions.XML_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        //Guides V2
        File(folderTest, "Test.${Extensions.XML_EXTENSION}").createNewFile()
        File(folderTest, "Documentacion.${Extensions.XML_EXTENSION}").createNewFile()
        File(folderTest, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(folderTest, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        val resultado = repository.getNumGuides(relativePath)

        assertEquals(4, resultado)
    }

    @Test
    fun regresa_0_ya_que_no_se_encuentran_guias_V1_o_V2() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val folderTest = File(pathKotlin, folderTest)
        folderTest.mkdirs()

        every {
            filePathResolver.mapToFolderPath(relativePath, PathKind.GUIAS)
        } returns GuidePath(pathKotlin.absolutePath)

        // Archivos ruta 1
        File(pathKotlin, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        // Archivos ruta 2
        File(folderTest, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(folderTest, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        val resultado = repository.getNumGuides(relativePath)

        assertEquals(0, resultado)
    }

    @Test
    fun regresa_0_si_la_ruta_principal_devuelve_null_o_falla() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val rootPath = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS)
        val pathNonExistent = File(rootPath, folderKotlin).absolutePath

        every {
            filePathResolver.mapToFolderPath(relativePath, PathKind.GUIAS)
        } returns GuidePath(pathNonExistent)

        val resultado = repository.getNumGuides(relativePath)

        assertEquals(0, resultado)
    }

    @Test
    fun regresa_solamente_2_guias_v1_si_la_ruta_para_guias_v2_es_null_o_falla() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath

        every {
            filePathResolver.mapToFolderPath(relativePath, PathKind.GUIAS)
        } returns GuidePath(pathKotlin)

        // Archivos GuiaV1
        File(pathKotlin, "Sintaxis.${Extensions.XML_EXTENSION}").createNewFile()
        File(pathKotlin, "Documentacion.${Extensions.XML_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        val resultado = repository.getNumGuides(relativePath)

        assertEquals(2, resultado)
    }

    // getGuides
    @Test
    fun regresa_las_guias_v1_y_v2() {
        val relativeGuidePath = RelativeGuidePath(folderKotlin)
        val rootPath = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS)
        val pathRootKotlin = File(rootPath, folderKotlin)
        pathRootKotlin.mkdirs()

        val secondaryPathTest = File(pathRootKotlin, folderTest)
        secondaryPathTest.mkdirs()

        val pathGuideSintaxis = File(pathRootKotlin, "Sintaxis${Extensions.POINT_XML_EXTENSION}")
        val pathGuideBucles = File(pathRootKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}")
        val pathGuideTest = File(secondaryPathTest, "Test${Extensions.POINT_XML_EXTENSION}")
        val pathGuideTestIntegracion =
            File(secondaryPathTest, "Test Integracion${Extensions.POINT_XML_EXTENSION}")

        val listGuideDomainModel = listOf(
            GuideDomainModel(
                version = GuideVersion.V1,
                nameGuide = "Bucles",
                description = ""
            ),
            GuideDomainModel(
                version = GuideVersion.V1,
                nameGuide = "Sintaxis",
                description = ""
            ),
            GuideDomainModel(
                version = GuideVersion.V2,
                nameGuide = "Test Integracion",
                description = ""
            ),
            GuideDomainModel(
                version = GuideVersion.V2,
                nameGuide = "Test",
                description = ""
            )
        )

        every {
            filePathResolver.mapToFolderPath(relativeGuidePath, PathKind.GUIAS)
        } returns GuidePath(pathRootKotlin.absolutePath)

        pathGuideSintaxis.writeText(xmlSintaxisV1)
        pathGuideBucles.writeText(xmlBuclesV1)
        pathGuideTest.writeText(xmlTestV2)
        pathGuideTestIntegracion.writeText(xmlTestIntegracionV2)

        val resultado = repository.getGuides(relativeGuidePath)

        assertEquals(listGuideDomainModel, resultado)
    }

    @Test
    fun regresa_la_lista_vacia_sino_se_encuentran_guias_V1_o_V2() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val folderTest = File(pathKotlin, folderTest)
        folderTest.mkdirs()

        every {
            filePathResolver.mapToFolderPath(relativePath, PathKind.GUIAS)
        } returns GuidePath(pathKotlin.absolutePath)

        // Archivos ruta 1
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        // Archivos ruta 2
        File(folderTest, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(folderTest, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        val resultado = repository.getGuides(relativePath)

        assertEquals(emptyList<GuideDomainModel>(), resultado)
    }

    @Test
    fun regresa_una_lista_vacia_si_la_ruta_principal_devuelve_null_o_falla() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val rootPath = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS)
        val pathNonExistent = File(rootPath, folderKotlin).absolutePath

        every {
            filePathResolver.mapToFolderPath(relativePath, PathKind.GUIAS)
        } returns GuidePath(pathNonExistent)

        val resultado = repository.getGuides(relativePath)

        assertEquals(emptyList<GuideDomainModel>(), resultado)
    }

    @Test
    fun regresa_una_lista_con_2_guias_v1_si_la_ruta_para_guias_v2_es_null_o_falla() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val listGuideDomainModel = listOf(
            GuideDomainModel(
                version = GuideVersion.V1,
                nameGuide = "Bucles",
                description = ""
            ),
            GuideDomainModel(
                version = GuideVersion.V1,
                nameGuide = "Sintaxis",
                description = ""
            )
        )

        every {
            filePathResolver.mapToFolderPath(relativePath, PathKind.GUIAS)
        } returns GuidePath(pathKotlin.absolutePath)

        // Archivos GuiaV1
        val pathGuideSintaxis = File(pathKotlin, "Sintaxis${Extensions.POINT_XML_EXTENSION}")
        val pathGuideBucles = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideSintaxis.writeText(xmlSintaxisV1)
        pathGuideBucles.writeText(xmlBuclesV1)

        val resultado = repository.getGuides(relativePath)

        assertEquals(listGuideDomainModel, resultado)
    }

    // existXMLGuideV1
    @Test
    fun si_la_guia_v1_no_existe_regresa_NoExistGuide() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath
        val pathGuide = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}").absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Bucles",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, relativePath)
        } returns pathGuide

        // Archivos GuiaV1
        val pathGuideTestIntegracion =
            File(pathKotlin, "Test Integracion${Extensions.POINT_XML_EXTENSION}")
        val pathGuideTest = File(pathKotlin, "Test${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideTestIntegracion.writeText(xmlTestIntegracionV2)
        pathGuideTest.writeText(xmlTestV2)

        val resultado = repository.existXMLGuideV1(guideDomain, relativePath)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun si_la_guia_v1_tiene_un_formato_invalido_regresa_NoExistGuide() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath
        val pathGuide = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}").absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Bucles",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, relativePath)
        } returns pathGuide

        // Archivos GuiaV1
        val pathGuideTestIntegracion =
            File(pathKotlin, "Test Integracion${Extensions.POINT_XML_EXTENSION}")
        val pathGuideBucles = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideTestIntegracion.writeText(xmlTestIntegracionV2)
        pathGuideBucles.writeText(xmlBuclesV1.toXMLInvalid())

        val resultado = repository.existXMLGuideV1(guideDomain, relativePath)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun recuperacion_de_guia_v1_y_tira_un_error_desconocido_regresa_NoExistGuide() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath
        val pathGuide = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}").absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Bucles",
            description = ""
        )

        val fileBucles = File(pathGuide)
        fileBucles.writeText(xmlBuclesV1)

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, relativePath)
        } returns pathGuide

        mockkStatic(DocumentBuilderFactory::class)
        val mockFactory = mockk<DocumentBuilderFactory>()

        every { DocumentBuilderFactory.newInstance() } returns mockFactory
        every { mockFactory.newDocumentBuilder() } throws RuntimeException("Fallo forzado para UnknownError")

        val resultado = repository.existXMLGuideV1(guideDomain, relativePath)

        unmockkStatic(DocumentBuilderFactory::class)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun si_la_etiqueta_CUESTIONARIO_es_null_es_un_archivo_corrupto_y_regresa_NoExistGuide() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath
        val pathGuide = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}").absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Bucles",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, relativePath)
        } returns pathGuide

        // Archivos GuiaV1
        val pathGuideTestIntegracion =
            File(pathKotlin, "Test Integracion${Extensions.POINT_XML_EXTENSION}")
        val pathGuideBucles = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideTestIntegracion.writeText(xmlTestIntegracionV2)
        pathGuideBucles.writeText(
            xmlBuclesV1
                .withoutTagCuestionario()
                .withoutTagGuiaEstudio()
        )

        val resultado = repository.existXMLGuideV1(guideDomain, relativePath)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun si_la_guia_es_v2_debe_de_regresar_NoExistGuide() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath
        val pathGuide =
            File(pathKotlin, "Test Integracion${Extensions.POINT_XML_EXTENSION}").absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test Integracion",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, relativePath)
        } returns pathGuide

        // Archivos GuiaV1
        val pathGuideTestIntegracion =
            File(pathKotlin, "Test Integracion${Extensions.POINT_XML_EXTENSION}")
        val pathGuideBucles = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideTestIntegracion.writeText(xmlTestIntegracionV2)
        pathGuideBucles.writeText(xmlBuclesV1.toXMLInvalid())

        val resultado = repository.existXMLGuideV1(guideDomain, relativePath)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun muestra_cuando_existe_una_guia_v1() {
        val relativeGuidePath = RelativeGuidePath(folderKotlin)
        val rootPath = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS)
        val pathKotlin = File(rootPath, folderKotlin)
        pathKotlin.mkdirs()

        val pathGuideSintaxis = File(pathKotlin, "Sintaxis${Extensions.POINT_XML_EXTENSION}")
        val pathGuideSintaxisNat = pathGuideSintaxis.absolutePath
        val pathGuideTest = File(pathKotlin, "Test${Extensions.POINT_XML_EXTENSION}")

        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Sintaxis",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, relativeGuidePath)
        } returns pathGuideSintaxisNat

        pathGuideSintaxis.writeText(xmlSintaxisV1)
        pathGuideTest.writeText(xmlTestV2)

        val resultado = repository.existXMLGuideV1(guideDomain, relativeGuidePath)

        assertEquals(ExistGuideV1Result.ExistGuide, resultado)
    }

    // moveGuide
    @Test
    fun mover_la_guia_exitosamente() {
        val folderAbap = "Abap"
        val oldRelativeGuidePath = RelativeGuidePath(folderKotlin)
        val newRelativeGuidePath = RelativeGuidePath(folderAbap)
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test",
            description = ""
        )
        val rootPathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val rootPathAbap = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderAbap)
        val oldPath = File(rootPathKotlin, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath
        val newPath = File(rootPathAbap, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath

        File(rootPathKotlin, "Test.xml").createNewFile()

        every {
            filePathResolver.mapToFilePathSpecificGuide(any(), oldRelativeGuidePath, any())
        } returns GuidePath(oldPath)

        every {
            filePathResolver.mapToFilePathSpecificGuide(any(), newRelativeGuidePath, any())
        } returns GuidePath(newPath)

        val resultado = repository.moveGuide(
            GuideContext.Moving(guideDomain, oldRelativeGuidePath, newRelativeGuidePath)
        )

        assertTrue("El repositorio debería devolver true al mover con éxito", resultado)
        assertTrue("El archivo debería existir ahora en la ruta nueva", File(newPath).exists())
    }

    @Test
    fun sino_existe_la_ruta_donde_mover_el_archivo_regresa_false() {
        val folderAbap = "Abap"
        val oldRelativeGuidePath = RelativeGuidePath(folderKotlin)
        val newRelativeGuidePath = RelativeGuidePath(folderAbap)
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test",
            description = ""
        )
        val pathKotlin = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val rootPath = File(folderFiles, StorageFolders.GUIAS)
        val abapFolder = File(rootPath, folderAbap)
        val oldPath = File(pathKotlin, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath
        val newPath = File(abapFolder, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath

        File(pathKotlin, "Test${Extensions.POINT_XML_EXTENSION}").createNewFile()

        every {
            filePathResolver.mapToFilePathSpecificGuide(any(), oldRelativeGuidePath, any())
        } returns GuidePath(oldPath)

        every {
            filePathResolver.mapToFilePathSpecificGuide(any(), newRelativeGuidePath, any())
        } returns GuidePath(newPath)

        val resultado = repository.moveGuide(
            GuideContext.Moving(guideDomain, oldRelativeGuidePath, newRelativeGuidePath)
        )

        assertFalse("El repositorio debería devolver false al NO mover con éxito", resultado)
        assertFalse("El archivo NO debería existir ahora en la ruta nueva", File(newPath).exists())
    }

    @Test
    fun si_el_archivo_que_vas_a_mover_no_existe_regresa_false() {
        val folderAbap = "Abap"
        val oldRelativeGuidePath = RelativeGuidePath(folderKotlin)
        val newRelativeGuidePath = RelativeGuidePath(folderAbap)
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test",
            description = ""
        )
        val kotlinFolder =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val abapFolder = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderAbap)
        val oldPath = File(kotlinFolder, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath
        val newPath = File(abapFolder, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(any(), oldRelativeGuidePath, any())
        } returns GuidePath(oldPath)

        every {
            filePathResolver.mapToFilePathSpecificGuide(any(), newRelativeGuidePath, any())
        } returns GuidePath(newPath)

        val resultado = repository.moveGuide(
            GuideContext.Moving(guideDomain, oldRelativeGuidePath, newRelativeGuidePath)
        )

        assertFalse("El repositorio debería devolver false al NO mover con éxito", resultado)
        assertFalse("El archivo NO debería existir ahora en la ruta nueva", File(newPath).exists())
    }

    // getXMLGuide
    @Test
    fun recuperacion_de_guia_v1() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Bucles", "")
        val relativeGuidePath = RelativeGuidePath(folderKotlin)
        val pathFolderGuiasKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, relativeGuidePath.value)
        val pathFolderImagesKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.IMAGENES, relativeGuidePath.value)
        val rutaImagen1 = File(
            pathFolderImagesKotlin,
            "1${Extensions.POINT_PNG_EXTENSION}"
        ).absolutePath.toSlashPath()
        val rutaImagen2 = File(
            pathFolderImagesKotlin,
            "2${Extensions.POINT_PNG_EXTENSION}"
        ).absolutePath.toSlashPath()

        val itemsResponse = listOf(
            QAItemDomain(
                question = QuestionItemDomain(
                    content = listOf(
                        QuestionContentDomain.Text("Pregunta 1", emptyList()),
                    )
                ),
                answer = QuestionItemDomain(
                    content = listOf(
                        QuestionContentDomain.Text("Respuesta 1", emptyList()),
                    )
                )
            ),
            QAItemDomain(
                question = QuestionItemDomain(
                    content = listOf(
                        QuestionContentDomain.Image(
                            uri = rutaImagen1,
                            nameFile = "1${Extensions.POINT_PNG_EXTENSION}"
                        )
                    )
                ),
                answer = QuestionItemDomain(
                    content = listOf(
                        QuestionContentDomain.Image(
                            uri = rutaImagen2,
                            nameFile = "2${Extensions.POINT_PNG_EXTENSION}"
                        )
                    )
                )
            )
        )

        val archivoGuia = File(
            pathFolderGuiasKotlin,
            "Bucles${Extensions.POINT_XML_EXTENSION}"
        ).apply { createNewFile() }
        val rutaNativaCompleta = archivoGuia.absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath(rutaNativaCompleta)

        archivoGuia.writeText(xmlBuclesV1)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.Success(guideDomainModel, itemsResponse), response)
    }

    @Test
    fun recuperacion_de_guia_v1_formato_invalido() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Bucles", "")
        val relativeGuidePath = RelativeGuidePath(folderKotlin)
        val rootPathValue =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, relativeGuidePath.value)
        val pathGuide = File(rootPathValue, "Bucles${Extensions.POINT_XML_EXTENSION}")
        val pathGuideNat = pathGuide.absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath(pathGuideNat)

        pathGuide.writeText(xmlBuclesV1.toXMLInvalid())

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.InvalidFormat, response)
    }

    @Test
    fun recuperacion_de_guia_v1_inexistente_y_regresa_NotFound() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Test", "")
        val relativeGuidePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, relativeGuidePath.value)
        val pathFile = File(pathKotlin, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath, PathKind.GUIAS
            )
        } returns GuidePath(pathFile)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.NotFound, response)
    }

    @Test
    fun al_recuperar_la_guia_v1_tira_error_desconocido_y_regresa_UnknownError() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Bucles", "")
        val relativeFile = File(folderKotlin, folderTest)
        val relativeGuidePath = RelativeGuidePath(relativeFile.absolutePath)
        val pathHandlerMock = mockk<PathHandler>()

        repository = GuiaRepositoryImpl(
            pathHandler = pathHandlerMock,
            xmlSerializerFactory = xmlSerializerFactory,
            fileOutputStreamFactory = fileOutputStreamFactory,
            filePathResolver = filePathResolver
        )

        val carpetaKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val fileSintaxis = File(carpetaKotlin, "Bucles.${Extensions.XML_EXTENSION}")

        fileSintaxis.writeText(xmlBuclesV1)

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath(fileSintaxis.absolutePath)

        every {
            pathHandlerMock.getSubstringPath(any(), any(), any(), any())
        } throws Exception("Fallo forzado para UnknownError")

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.UnknownError, response)
    }

    @Test
    fun recuperacion_de_guia_v2() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relativeGuidePath = RelativeGuidePath(File(folderKotlin, folderTest).absolutePath)
        val pathFolderTest =
            temporaryFolder.newFolder(
                folderFiles,
                StorageFolders.GUIAS,
                folderKotlin,
                folderTest
            )
        val pathFolderTestImages =
            temporaryFolder.newFolder(
                folderFiles,
                StorageFolders.IMAGENES,
                folderKotlin,
                folderTest
            )

        val rutaImagen1 = File(
            pathFolderTestImages,
            "1${Extensions.POINT_PNG_EXTENSION}"
        ).absolutePath.toSlashPath()
        val rutaImagen2 = File(
            pathFolderTestImages,
            "2${Extensions.POINT_PNG_EXTENSION}"
        ).absolutePath.toSlashPath()

        val rutaCompleta =
            File(pathFolderTest, "Test${Extensions.POINT_XML_EXTENSION}")
        val rutaCompletaNativa = rutaCompleta.absolutePath

        val itemsResponse = listOf(
            QAItemDomain(
                question = QuestionItemDomain(
                    content = listOf(
                        QuestionContentDomain.Text("Pregunta 1", emptyList()),
                        QuestionContentDomain.Image(
                            uri = rutaImagen1,
                            nameFile = "1${Extensions.POINT_PNG_EXTENSION}"
                        )
                    )
                ),
                answer = QuestionItemDomain(
                    content = listOf(
                        QuestionContentDomain.Text("Respuesta 1", emptyList()),
                        QuestionContentDomain.Image(
                            uri = rutaImagen2,
                            nameFile = "2${Extensions.POINT_PNG_EXTENSION}"
                        )
                    )
                )
            )
        )

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath(rutaCompletaNativa)

        rutaCompleta.writeText(xmlTestV2)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.Success(guideDomainModel, itemsResponse), response)
    }

    @Test
    fun recuperacion_de_guia_v2_formato_invalido() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relativeNative = File(folderKotlin, folderTest).absolutePath
        val relativeGuidePath = RelativeGuidePath(relativeNative)
        val pathFolderGuiasTest =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderTest)
        val pathGuidesComplete = File(pathFolderGuiasTest, "Test${Extensions.POINT_XML_EXTENSION}")
        val pathGuidesCompleteNat = pathGuidesComplete.absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath(pathGuidesCompleteNat)

        pathGuidesComplete.writeText(xmlTestV2.toXMLInvalid())

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.InvalidFormat, response)
    }

    @Test
    fun recuperacion_de_guia_v2_archivo_no_encontrado() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val folderKotlin = "Kotlin"
        val folderTest = "Test"
        val relativeGuide = File(folderKotlin, folderTest).absolutePath
        val relativeGuidePath = RelativeGuidePath(relativeGuide)
        val pathFolderGuidesTest =
            temporaryFolder.newFolder("files", StorageFolders.GUIAS, folderKotlin, folderTest)
        val pathGuidesComplete = File(pathFolderGuidesTest, "Test${Extensions.POINT_XML_EXTENSION}")
        val pathGuidesCompleteNat = pathGuidesComplete.absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath, PathKind.GUIAS
            )
        } returns GuidePath(pathGuidesCompleteNat)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.NotFound, response)
    }

    @Test
    fun recuperacion_de_guia_v2_error_desconocido_con_mock() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relativeGuidePath = RelativeGuidePath(File(folderKotlin, folderTest).absolutePath)
        val pathHandlerMock = mockk<PathHandler>()

        repository = GuiaRepositoryImpl(
            pathHandler = pathHandlerMock,
            xmlSerializerFactory = xmlSerializerFactory,
            fileOutputStreamFactory = fileOutputStreamFactory,
            filePathResolver = filePathResolver
        )

        val carpetaKotlin = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderTest)
        val fileSintaxis = File(carpetaKotlin, "Test.${Extensions.XML_EXTENSION}")

        fileSintaxis.writeText(xmlTestV2)

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath(fileSintaxis.absolutePath)

        every {
            pathHandlerMock.getSubstringPath(any(), any(), any(), any())
        } throws Exception("Fallo forzado para UnknownError")

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.UnknownError, response)
    }

    @Test
    fun borrar_guia_v1() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Test", "")
        val relativeGuidePath = RelativeGuidePath(folderKotlin)

        val pathFolder = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val archivoGuia = File(pathFolder, "Test${Extensions.POINT_XML_EXTENSION}").apply { createNewFile() }

        val rutaNativaCompleta = archivoGuia.absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath, PathKind.GUIAS
            )
        } returns GuidePath(rutaNativaCompleta)

        assertTrue("El archivo debería existir antes de la eliminación", archivoGuia.exists())

        val response =
            repository.deleteGuide(GuideContext.DeleteGuide(guideDomainModel, relativeGuidePath))

        assertTrue("El método debería retornar true al eliminar con éxito", response)
        assertFalse("El archivo físico debería haber sido borrado del disco", archivoGuia.exists())
    }

    private fun String.toSlashPath(): String = this.replace("\\", "/")
    private fun String.toXMLInvalid(): String =
        this.replace("</${Structure.GUIAESTUDIO}>", "<//${Structure.GUIAESTUDIO}>")

    fun String.withoutTagGuiaEstudio(): String = this
        .replace(Structure.GUIAESTUDIO, "${Structure.GUIAESTUDIO}O")

    fun String.withoutTagCuestionario(): String = this
        .replace(Structure.CUESTIONARIO, "${Structure.CUESTIONARIO}O")

    fun String.withoutElement(tagName: String): String {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        // Parseamos el XML que sí es válido
        val doc = dBuilder.parse(ByteArrayInputStream(this.toByteArray(Charsets.UTF_8)))

        val element = doc.getElementsByTagName(tagName).item(0) as? Element
        // Si existe, lo removemos de su padre
        element?.parentNode?.removeChild(element)

        // Convertimos el documento modificado de vuelta a String
        val tf = TransformerFactory.newInstance()
        val transformer = tf.newTransformer()
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))

        return writer.toString()
    }
}