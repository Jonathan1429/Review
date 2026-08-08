package com.jonathanev.review.data.filesystem

import app.cash.turbine.test
import com.jonathanev.review.data.storage.StorageFolders
import com.jonathanev.review.data.util.LabelsHandler
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
import com.jonathanev.review.domain.model.OptionalAttrGuide
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.model.RequiredAttrGuide
import com.jonathanev.review.domain.repository.FileOutputStreamFactory
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.XmlSerializerFactory
import com.jonathanev.review.domain.result.ExistGuideV1Result
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.SaveGuideErrors
import com.jonathanev.review.domain.result.UpdateGuideError
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.w3c.dom.Element
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class GuiaRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var repository: GuiaRepositoryImpl

    // Instancia real de tu PathHandler
    private val pathHandler = PathHandler()
    private val labelsHandler = LabelsHandler()
    private val xmlSerializerFactory: XmlSerializerFactory = mockk()
    private val fileOutputStreamFactory: FileOutputStreamFactory = mockk()
    private val filePathResolver: FilePathResolver = mockk()

    private lateinit var xmlTestV2: String
    private lateinit var xmlTestIntegracionV2: String
    private lateinit var xmlSintaxisV1: String
    private lateinit var xmlBuclesV1: String
    private lateinit var folderKotlin: String
    private lateinit var folderBucles: String
    private lateinit var folderTest: String
    private lateinit var folderFiles: String

    @Before
    fun setUp() {
        folderKotlin = "Kotlin"
        folderTest = "Test"
        folderBucles = "Bucles"
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
            filePathResolver = filePathResolver,
            labelsHandler = labelsHandler
        )
    }

    // getNumGuides
    // Guias V1
    @Test
    fun regresa_el_num_de_guias_V1_y_V2_dentro_de_la_ruta() = runTest {
        val pathKotlin = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val folderTest = File(pathKotlin, folderTest)
        folderTest.mkdirs()

        coEvery {
            filePathResolver.mapToFolderPath(PathKind.GUIAS)
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

        repository.hasGuides().test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun regresa_0_ya_que_no_se_encuentran_guias_V1_o_V2() = runTest {
        val pathKotlin = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val folderTest = File(pathKotlin, folderTest)
        folderTest.mkdirs()

        coEvery {
            filePathResolver.mapToFolderPath(PathKind.GUIAS)
        } returns GuidePath(pathKotlin.absolutePath)

        // Archivos ruta 1
        File(pathKotlin, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        // Archivos ruta 2
        File(folderTest, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(folderTest, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        repository.hasGuides().test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun regresa_0_si_la_ruta_principal_devuelve_null_o_falla() = runTest {
        val rootPath = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS)
        val pathNonExistent = File(rootPath, folderKotlin).absolutePath

        coEvery {
            filePathResolver.mapToFolderPath(PathKind.GUIAS)
        } returns GuidePath(pathNonExistent)

        repository.hasGuides().test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun regresa_solamente_2_guias_v1_si_la_ruta_para_guias_v2_es_null_o_falla() = runTest {
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath

        coEvery {
            filePathResolver.mapToFolderPath(PathKind.GUIAS)
        } returns GuidePath(pathKotlin)

        // Archivos GuiaV1
        File(pathKotlin, "Sintaxis.${Extensions.XML_EXTENSION}").createNewFile()
        File(pathKotlin, "Documentacion.${Extensions.XML_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        repository.hasGuides().test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // getGuides
    @Test
    fun regresa_las_guias_v1_y_v2() = runTest {
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

        val listGuideDomainModel =
            listOf(
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

        coEvery {
            filePathResolver.mapToFolderPath(PathKind.GUIAS)
        } returns GuidePath(pathRootKotlin.absolutePath)

        pathGuideSintaxis.writeText(xmlSintaxisV1)
        pathGuideBucles.writeText(xmlBuclesV1)
        pathGuideTest.writeText(xmlTestV2)
        pathGuideTestIntegracion.writeText(xmlTestIntegracionV2)

        repository.getGuides().test {
            assertEquals(listGuideDomainModel, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun regresa_la_lista_vacia_sino_se_encuentran_guias_V1_o_V2() = runTest {
        val pathKotlin = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val folderTest = File(pathKotlin, folderTest)
        folderTest.mkdirs()

        coEvery {
            filePathResolver.mapToFolderPath(PathKind.GUIAS)
        } returns GuidePath(pathKotlin.absolutePath)

        // Archivos ruta 1
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        // Archivos ruta 2
        File(folderTest, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(folderTest, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        repository.getGuides().test {
            assertEquals(emptyList<GuideDomainModel>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun regresa_una_lista_vacia_si_la_ruta_principal_devuelve_null_o_falla() = runTest {
        val rootPath = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS)
        val pathNonExistent = File(rootPath, folderKotlin).absolutePath

        coEvery {
            filePathResolver.mapToFolderPath(PathKind.GUIAS)
        } returns GuidePath(pathNonExistent)

        repository.getGuides().test {
            assertEquals(emptyList<GuideDomainModel>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun regresa_una_lista_con_2_guias_v1_si_la_ruta_para_guias_v2_es_null_o_falla() = runTest {
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

        coEvery {
            filePathResolver.mapToFolderPath(PathKind.GUIAS)
        } returns GuidePath(pathKotlin.absolutePath)

        // Archivos GuiaV1
        val pathGuideSintaxis = File(pathKotlin, "Sintaxis${Extensions.POINT_XML_EXTENSION}")
        val pathGuideBucles = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideSintaxis.writeText(xmlSintaxisV1)
        pathGuideBucles.writeText(xmlBuclesV1)

        repository.getGuides().test {
            assertEquals(listGuideDomainModel, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cuando_hay_un_error_al_recuperar_una_guia_no_se_toma_en_cuenta_para_regresarla() = runTest {
        val relativePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath
        val listGuideDomainModel = listOf(
            GuideDomainModel(
                version = GuideVersion.V1,
                nameGuide = "Sintaxis",
                description = ""
            )
        )

        coEvery {
            filePathResolver.mapToFolderPath(PathKind.GUIAS)
        } returns GuidePath(pathKotlin)

        // Archivos GuiaV1
        val pathGuideSintaxis =
            File(pathKotlin, "Sintaxis${Extensions.POINT_XML_EXTENSION}")
        val pathGuideBucles = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideSintaxis.writeText(xmlSintaxisV1)
        pathGuideBucles.writeText(xmlBuclesV1.toXMLInvalid())

        repository.getGuides().test {
            assertEquals(listGuideDomainModel, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // existXMLGuideV1
    @Test
    fun si_la_guia_v1_no_existe_regresa_NoExistGuide() = runTest {
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath
        val pathGuide = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}").absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Bucles",
            description = ""
        )

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(guideDomain, PathKind.GUIAS)
        } returns GuidePath(pathGuide)

        // Archivos GuiaV1
        val pathGuideTestIntegracion =
            File(pathKotlin, "Test Integracion${Extensions.POINT_XML_EXTENSION}")
        val pathGuideTest = File(pathKotlin, "Test${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideTestIntegracion.writeText(xmlTestIntegracionV2)
        pathGuideTest.writeText(xmlTestV2)

        val resultado = repository.existXMLGuideV1(guideDomain)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun si_la_guia_v1_tiene_un_formato_invalido_regresa_NoExistGuide() = runTest {
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath
        val pathGuide = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}").absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Bucles",
            description = ""
        )

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(guideDomain, PathKind.GUIAS)
        } returns GuidePath(pathGuide)

        // Archivos GuiaV1
        val pathGuideTestIntegracion =
            File(pathKotlin, "Test Integracion${Extensions.POINT_XML_EXTENSION}")
        val pathGuideBucles = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideTestIntegracion.writeText(xmlTestIntegracionV2)
        pathGuideBucles.writeText(xmlBuclesV1.toXMLInvalid())

        val resultado = repository.existXMLGuideV1(guideDomain)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun recuperacion_de_guia_v1_y_tira_un_error_desconocido_regresa_NoExistGuide() = runTest {
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

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(guideDomain, PathKind.GUIAS)
        } returns GuidePath(pathGuide)

        mockkStatic(DocumentBuilderFactory::class)
        val mockFactory = mockk<DocumentBuilderFactory>()

        every { DocumentBuilderFactory.newInstance() } returns mockFactory
        every { mockFactory.newDocumentBuilder() } throws RuntimeException("Fallo forzado para UnknownError")

        val resultado = repository.existXMLGuideV1(guideDomain)

        unmockkStatic(DocumentBuilderFactory::class)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun si_la_etiqueta_CUESTIONARIO_es_null_es_un_archivo_corrupto_y_regresa_NoExistGuide() =
        runTest {
            val pathKotlin =
                temporaryFolder.newFolder(
                    folderFiles,
                    StorageFolders.GUIAS,
                    folderKotlin
                ).absolutePath
            val pathGuide = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}").absolutePath
            val guideDomain = GuideDomainModel(
                version = GuideVersion.V1,
                nameGuide = "Bucles",
                description = ""
            )

            coEvery {
                filePathResolver.mapToFilePathSpecificGuide(guideDomain, PathKind.GUIAS)
            } returns GuidePath(pathGuide)

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

            val resultado = repository.existXMLGuideV1(guideDomain)

            assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
        }

    @Test
    fun si_la_guia_es_v2_debe_de_regresar_NoExistGuide() = runTest {
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin).absolutePath
        val pathGuide =
            File(pathKotlin, "Test Integracion${Extensions.POINT_XML_EXTENSION}").absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test Integracion",
            description = ""
        )

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(guideDomain, PathKind.GUIAS)
        } returns GuidePath(pathGuide)

        // Archivos GuiaV1
        val pathGuideTestIntegracion =
            File(pathKotlin, "Test Integracion${Extensions.POINT_XML_EXTENSION}")
        val pathGuideBucles = File(pathKotlin, "Bucles${Extensions.POINT_XML_EXTENSION}")
        File(pathKotlin, "Imagen1${Extensions.POINT_PNG_EXTENSION}").createNewFile()
        File(pathKotlin, "Imagen2${Extensions.POINT_PNG_EXTENSION}").createNewFile()

        pathGuideTestIntegracion.writeText(xmlTestIntegracionV2)
        pathGuideBucles.writeText(xmlBuclesV1.toXMLInvalid())

        val resultado = repository.existXMLGuideV1(guideDomain)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun muestra_cuando_existe_una_guia_v1() = runTest {
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

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(guideDomain, PathKind.GUIAS)
        } returns GuidePath(pathGuideSintaxisNat)

        pathGuideSintaxis.writeText(xmlSintaxisV1)
        pathGuideTest.writeText(xmlTestV2)

        val resultado = repository.existXMLGuideV1(guideDomain)

        assertEquals(ExistGuideV1Result.ExistGuide, resultado)
    }

    // moveGuide
    @Test
    fun mover_la_guia_exitosamente() = runTest {
        val folderAbap = "Abap"
        val oldRelativeGuidePath = RelativeGuidePath(folderKotlin)
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test",
            description = ""
        )
        val guideContext = GuideContext.Moving(guideDomain, oldRelativeGuidePath)
        val rootPathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val rootPathAbap = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderAbap)
        val oldPath = File(rootPathKotlin, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath
        val newPath = File(rootPathAbap, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath

        File(rootPathKotlin, "Test.xml").createNewFile()

        coEvery {
            filePathResolver.mapToOldFolderPathSpecificGuide(
                guideDomainModel = guideDomain,
                originContext = guideContext,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldPath)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomain,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(newPath)

        val resultado = repository.moveGuide(guideContext = guideContext)

        assertTrue("El repositorio debería devolver true al mover con éxito", resultado)
        assertTrue("El archivo debería existir ahora en la ruta nueva", File(newPath).exists())
        assertFalse(
            "El archivo debería NO debería existir en la ruta vieja",
            File(oldPath).exists()
        )
    }

    @Test
    fun sino_existe_la_ruta_donde_mover_el_archivo_regresa_false() = runTest {
        val folderAbap = "Abap"
        val oldRelativeGuidePath = RelativeGuidePath(folderKotlin)
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test",
            description = ""
        )
        val guideContext = GuideContext.Moving(guideDomain, oldRelativeGuidePath)
        val pathKotlin = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val rootPath = File(folderFiles, StorageFolders.GUIAS)
        val abapFolder = File(rootPath, folderAbap)
        val oldPath = File(pathKotlin, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath
        val newPath = File(abapFolder, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath

        File(pathKotlin, "Test${Extensions.POINT_XML_EXTENSION}").createNewFile()

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(guideDomain, PathKind.GUIAS)
        } returns GuidePath(oldPath)

        coEvery {
            filePathResolver.mapToOldFolderPathSpecificGuide(
                guideDomainModel = guideDomain,
                originContext = guideContext,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(newPath)

        val resultado = repository.moveGuide(guideContext)

        assertFalse("El repositorio debería devolver false al NO mover con éxito", resultado)
        assertFalse("El archivo NO debería existir ahora en la ruta nueva", File(newPath).exists())
    }

    @Test
    fun si_el_archivo_que_vas_a_mover_no_existe_regresa_false() = runTest {
        val folderAbap = "Abap"
        val oldRelativeGuidePath = RelativeGuidePath(folderKotlin)
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test",
            description = ""
        )
        val guideContext = GuideContext.Moving(guideDomain, oldRelativeGuidePath)
        val kotlinFolder =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val abapFolder = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderAbap)
        val oldPath = File(kotlinFolder, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath
        val newPath = File(abapFolder, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(guideDomain, PathKind.GUIAS)
        } returns GuidePath(oldPath)

        coEvery {
            filePathResolver.mapToOldFolderPathSpecificGuide(
                guideDomainModel = guideDomain,
                originContext = guideContext,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(newPath)

        val resultado = repository.moveGuide(guideContext)

        assertFalse("El repositorio debería devolver false al NO mover con éxito", resultado)
        assertFalse("El archivo NO debería existir ahora en la ruta nueva", File(newPath).exists())
    }

    // getXMLGuide
    @Test
    fun recuperacion_de_guia_v1() = runTest {
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

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            )
        } returns GuidePath(rutaNativaCompleta)

        archivoGuia.writeText(xmlBuclesV1)

        val response = repository.getXMLGuide(guideDomainModel)

        assertEquals(GetGuideResult.Success(guideDomainModel, itemsResponse), response)
    }

    @Test
    fun recuperacion_de_guia_v1_formato_invalido() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Bucles", "")
        val relativeGuidePath = RelativeGuidePath(folderKotlin)
        val rootPathValue =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, relativeGuidePath.value)
        val pathGuide = File(rootPathValue, "Bucles${Extensions.POINT_XML_EXTENSION}")
        val pathGuideNat = pathGuide.absolutePath

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            )
        } returns GuidePath(pathGuideNat)

        pathGuide.writeText(xmlBuclesV1.toXMLInvalid())

        val response = repository.getXMLGuide(guideDomainModel)

        assertEquals(GetGuideResult.InvalidFormat, response)
    }

    @Test
    fun recuperacion_de_guia_v1_inexistente_y_regresa_NotFound() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Test", "")
        val relativeGuidePath = RelativeGuidePath(folderKotlin)
        val pathKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, relativeGuidePath.value)
        val pathFile = File(pathKotlin, "Test${Extensions.POINT_XML_EXTENSION}").absolutePath

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, PathKind.GUIAS
            )
        } returns GuidePath(pathFile)

        val response = repository.getXMLGuide(guideDomainModel)

        assertEquals(GetGuideResult.NotFound, response)
    }

    @Test
    fun al_recuperar_la_guia_v1_tira_error_desconocido_y_regresa_UnknownError() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Bucles", "")
        val pathHandlerMock = mockk<PathHandler>()

        repository = GuiaRepositoryImpl(
            pathHandler = pathHandlerMock,
            xmlSerializerFactory = xmlSerializerFactory,
            fileOutputStreamFactory = fileOutputStreamFactory,
            filePathResolver = filePathResolver,
            labelsHandler = labelsHandler
        )

        val carpetaKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val fileSintaxis = File(carpetaKotlin, "Bucles.${Extensions.XML_EXTENSION}")

        fileSintaxis.writeText(xmlBuclesV1)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            )
        } returns GuidePath(fileSintaxis.absolutePath)

        every {
            pathHandlerMock.getSubstringPath(any(), any(), any(), any())
        } throws Exception("Fallo forzado para UnknownError")

        val response = repository.getXMLGuide(guideDomainModel)

        assertEquals(GetGuideResult.UnknownError, response)
    }

    @Test
    fun recuperacion_de_guia_v2() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
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

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            )
        } returns GuidePath(rutaCompletaNativa)

        rutaCompleta.writeText(xmlTestV2)

        val response = repository.getXMLGuide(guideDomainModel)

        assertEquals(GetGuideResult.Success(guideDomainModel, itemsResponse), response)
    }

    @Test
    fun recuperacion_de_guia_v2_formato_invalido() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val pathFolderGuiasTest =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderTest)
        val pathGuidesComplete = File(pathFolderGuiasTest, "Test${Extensions.POINT_XML_EXTENSION}")
        val pathGuidesCompleteNat = pathGuidesComplete.absolutePath

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            )
        } returns GuidePath(pathGuidesCompleteNat)

        pathGuidesComplete.writeText(xmlTestV2.toXMLInvalid())

        val response = repository.getXMLGuide(guideDomainModel)

        assertEquals(GetGuideResult.InvalidFormat, response)
    }

    @Test
    fun recuperacion_de_guia_v2_archivo_no_encontrado() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val folderKotlin = "Kotlin"
        val folderTest = "Test"
        val pathFolderGuidesTest =
            temporaryFolder.newFolder("files", StorageFolders.GUIAS, folderKotlin, folderTest)
        val pathGuidesComplete = File(pathFolderGuidesTest, "Test${Extensions.POINT_XML_EXTENSION}")
        val pathGuidesCompleteNat = pathGuidesComplete.absolutePath

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, PathKind.GUIAS
            )
        } returns GuidePath(pathGuidesCompleteNat)

        val response = repository.getXMLGuide(guideDomainModel)

        assertEquals(GetGuideResult.NotFound, response)
    }

    @Test
    fun recuperacion_de_guia_v2_error_desconocido_con_mock() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val pathHandlerMock = mockk<PathHandler>()

        repository = GuiaRepositoryImpl(
            pathHandler = pathHandlerMock,
            xmlSerializerFactory = xmlSerializerFactory,
            fileOutputStreamFactory = fileOutputStreamFactory,
            filePathResolver = filePathResolver,
            labelsHandler = labelsHandler
        )

        val carpetaKotlin =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderTest)
        val fileSintaxis = File(carpetaKotlin, "Test.${Extensions.XML_EXTENSION}")

        fileSintaxis.writeText(xmlTestV2)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            )
        } returns GuidePath(fileSintaxis.absolutePath)

        every {
            pathHandlerMock.getSubstringPath(any(), any(), any(), any())
        } throws Exception("Fallo forzado para UnknownError")

        val response = repository.getXMLGuide(guideDomainModel)

        assertEquals(GetGuideResult.UnknownError, response)
    }

    // deleteGuide
    @Test
    fun sino_existe_la_guia_v1_regresa_false() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Bucles", "")
        val pathFolder = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val archivoGuia =
            File(pathFolder, "Bucles${Extensions.POINT_XML_EXTENSION}")

        val rutaNativaCompleta = archivoGuia.absolutePath

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, PathKind.GUIAS
            )
        } returns GuidePath(rutaNativaCompleta)

        assertFalse("El archivo NO debería existir antes de la eliminación", archivoGuia.exists())

        val response =
            repository.deleteGuide(GuideContext.DeleteGuide(guideDomainModel))

        assertFalse("La ruta del archivo NO existe por lo tanto regresará false", response)
        assertFalse(
            "El archivo JAMAS existió y seguirá regresando que no existe",
            archivoGuia.exists()
        )
    }

    @Test
    fun sino_existe_la_guia_v2_regresa_false() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val pathFolderTest =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderTest)
        val archivoGuia =
            File(pathFolderTest, "Test${Extensions.POINT_XML_EXTENSION}")
        val pathGuide = archivoGuia.absolutePath

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            )
        } returns GuidePath(pathGuide)

        assertFalse(
            "El archivo NO debería existir, incluso antes de la eliminación",
            archivoGuia.exists()
        )

        val response =
            repository.deleteGuide(GuideContext.DeleteGuide(guideDomainModel))

        assertFalse(
            "La respuesta es false porque no borró ningun archivo",
            response
        )
        assertFalse(
            "El archivo JAMAS existió y seguirá regresando que no existe",
            archivoGuia.exists()
        )
    }

    @Test
    fun se_borra_una_guia_v2() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val pathFolderTest =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderTest)
        val archivoGuia =
            File(pathFolderTest, "Test${Extensions.POINT_XML_EXTENSION}").apply { createNewFile() }
        val pathGuide = archivoGuia.absolutePath

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            )
        } returns GuidePath(pathGuide)

        assertTrue("El archivo debería existir antes de la eliminación", archivoGuia.exists())

        val response =
            repository.deleteGuide(GuideContext.DeleteGuide(guideDomainModel))

        assertTrue("El método debería retornar true al eliminar con éxito", response)
        assertFalse("El archivo físico debería haber sido borrado del disco", archivoGuia.exists())
    }

    @Test
    fun borrar_guia_v1() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Bucles", "")

        val pathFolder = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val archivoGuia =
            File(pathFolder, "Bucles${Extensions.POINT_XML_EXTENSION}").apply { createNewFile() }

        val rutaNativaCompleta = archivoGuia.absolutePath

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, PathKind.GUIAS
            )
        } returns GuidePath(rutaNativaCompleta)

        assertTrue("El archivo debería existir antes de la eliminación", archivoGuia.exists())

        val response =
            repository.deleteGuide(GuideContext.DeleteGuide(guideDomainModel))

        assertTrue("El método debería retornar true al eliminar con éxito", response)
        assertFalse("El archivo físico debería haber sido borrado del disco", archivoGuia.exists())
    }

    // renameGuide
    @Test
    fun ocurre_un_error_inesperado_al_renombrar_una_guia_y_regresa_unknownError() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val pathFileTest = temporaryFolder.newFolder(
            folderFiles,
            StorageFolders.GUIAS,
            folderKotlin,
            folderTest
        )
        val guideContext = GuideContext.Rename(
            guide = GuideDomainModel(
                version = GuideVersion.V2,
                nameGuide = guideDomainModel.nameGuide,
                description = ""
            ),
            name = RequiredAttrGuide("Test 2"),
            description = OptionalAttrGuide("")
        )
        val oldGuidePath =
            File(
                pathFileTest,
                "${guideDomainModel.nameGuide}${Extensions.POINT_XML_EXTENSION}"
            )
        oldGuidePath.createNewFile()
        val tempGuidePath = File(pathFileTest, "Test${Extensions.POINT_XML_EXTENSION}.tmp")

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldGuidePath.absolutePath)

        every {
            xmlSerializerFactory.create()
        } throws Exception("Fallo forzado para UnknownError")

        assertTrue("Debe existir el archivo con el nombre anterior", oldGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())

        val response = repository.renameGuide(emptyList(), emptyList(), guideContext)

        assertTrue("Debe existir el archivo con el nombre anterior", oldGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())
        assertEquals(GuideResource.Error(UpdateGuideError.UnknownError), response)
    }

    @Test
    fun tira_un_error_al_renombrar_y_devuelve_WriteError() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val pathFolderTest = temporaryFolder.newFolder(
            folderFiles,
            StorageFolders.GUIAS,
            folderKotlin,
            folderTest
        ).absolutePath
        val relGuidePathFile = File(folderKotlin, folderTest)
        val guideContext = GuideContext.Rename(
            guide = guideDomainModel,
            name = RequiredAttrGuide("Test 2"),
            description = OptionalAttrGuide("")
        )
        val newGuideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = guideContext.name.value,
            description = guideContext.description.value
        )
        val oldGuidePath =
            File(pathFolderTest, "Test${Extensions.POINT_XML_EXTENSION}")
        oldGuidePath.writeText(xmlTestV2)
        val newGuidePath =
            File(
                pathFolderTest,
                "${guideContext.name.value}${Extensions.POINT_XML_EXTENSION}"
            )
        val tempGuidePath = File(relGuidePathFile, "Test${Extensions.POINT_XML_EXTENSION}.tmp")

        val mockSerializer = mockk<XmlSerializer>(relaxed = true)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideContext.guide,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldGuidePath.absolutePath)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = newGuideDomain,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldGuidePath.absolutePath)

        // 2. Le decimos a la fábrica que devuelva nuestro mock
        every { xmlSerializerFactory.create() } returns mockSerializer

        // No crea ningún archivo real, lo crea en memoria RAM
        val mockOutputStream = ByteArrayOutputStream()
        every { fileOutputStreamFactory.create(any()) } returns mockOutputStream

        assertTrue("Debe existir el archivo con el nombre anterior", oldGuidePath.exists())
        assertFalse("No debe existir el archivo con el nuevo nombre", newGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())

        val response = repository.renameGuide(emptyList(), emptyList(), guideContext)

        assertTrue("Debe existir el archivo con el nombre anterior", oldGuidePath.exists())
        assertFalse("No debe existir el archivo con el nuevo nombre", newGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())
        assertEquals(GuideResource.Error(UpdateGuideError.WriteError), response)
    }

    @Test
    fun no_se_encuentra_el_archivo_en_la_ruta_especificada_y_regresa_NotFound() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val pathFolderKotlin = temporaryFolder.newFolder(
            folderFiles,
            StorageFolders.GUIAS,
            folderKotlin,
        ).absolutePath
        val relGuideInexistent = File(pathFolderKotlin, folderTest).absolutePath
        val relGuideFile = File(folderKotlin, folderTest)
        val relativeGuidePath = RelativeGuidePath(relGuideFile.path)
        val guideContext = GuideContext.Rename(
            guide = guideDomainModel,
            name = RequiredAttrGuide("Test 2"),
            description = OptionalAttrGuide("")
        )
        val oldGuidePath =
            File(relGuideInexistent, "Test${Extensions.POINT_XML_EXTENSION}")
        val newGuidePath =
            File(
                relGuideInexistent,
                "${guideContext.name.value}${Extensions.POINT_XML_EXTENSION}"
            )
        val tempGuidePath = File(relGuideInexistent, "Test${Extensions.POINT_XML_EXTENSION}.tmp")
        val mockSerializer = mockk<XmlSerializer>(relaxed = true)
        val newGuideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = guideContext.name.value,
            description = guideContext.description.value
        )

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideContext.guide,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldGuidePath.absolutePath)

        every {
            filePathResolver.getPathGuidesV2(
                guideDomainModel = newGuideDomain,
                kind = PathKind.GUIAS,
                relativeGuidePath = relativeGuidePath
            )
        } returns newGuidePath.absolutePath

        // 2. Le decimos a la fábrica que devuelva nuestro mock
        every { xmlSerializerFactory.create() } returns mockSerializer

        // En lugar de usar ByteArrayOutputStream en memoria, usa un FileOutputStream real
        every { fileOutputStreamFactory.create(any()) } answers {
            val filePath = firstArg<String>()
            FileOutputStream(filePath)
        }

        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())
        val response = repository.renameGuide(emptyList(), emptyList(), guideContext)

        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())
        assertEquals(GuideResource.Error(UpdateGuideError.NotFound), response)
    }

    @Test
    fun no_hay_ningun_problema_renombra_la_guia_de_estudio_y_regresa_un_success() = runTest {
        val preguntas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Pregunta 1", emptyList()),
                    QuestionContentDomain.Image("", "1${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )
        val respuestas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Respuesta 1", emptyList()),
                    QuestionContentDomain.Image("", "2${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )

        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val baseGuidePath = temporaryFolder.newFolder(
            folderFiles,
            StorageFolders.GUIAS,
            folderKotlin,
            folderTest
        ).absolutePath
        val guideContext = GuideContext.Rename(
            guide = guideDomainModel,
            name = RequiredAttrGuide("Test 2"),
            description = OptionalAttrGuide("")
        )
        val oldGuidePath =
            File(baseGuidePath, "Test${Extensions.POINT_XML_EXTENSION}")
        oldGuidePath.writeText(xmlTestV2)
        val newGuidePath =
            File(
                baseGuidePath,
                "${guideContext.name.value}${Extensions.POINT_XML_EXTENSION}"
            )
        val tempGuidePath = File(baseGuidePath, "Test${Extensions.POINT_XML_EXTENSION}.tmp")
        val mockSerializer = mockk<XmlSerializer>(relaxed = true)
        val newGuideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = guideContext.name.value,
            description = guideContext.description.value
        )

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideContext.guide,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldGuidePath.absolutePath)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = newGuideDomain,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(newGuidePath.absolutePath)

        // 2. Le decimos a la fábrica que devuelva nuestro mock
        every { xmlSerializerFactory.create() } returns mockSerializer

        // En lugar de usar ByteArrayOutputStream en memoria, usa un FileOutputStream real
        every { fileOutputStreamFactory.create(any()) } answers {
            val filePath = firstArg<String>()
            FileOutputStream(filePath)
        }

        assertTrue("Debe existir el archivo con el nombre anterior", oldGuidePath.exists())
        assertFalse("No debe existir el archivo con el nuevo nombre", newGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())

        val response = repository.renameGuide(preguntas, respuestas, guideContext)

        assertFalse("No debe existir el archivo con el nombre anterior", oldGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())
        assertTrue("Debe existir el archivo con el nuevo nombre", newGuidePath.exists())
        assertEquals(GuideResource.Success(newGuideDomain), response)
    }

    @Test
    fun renombrar_guia_con_el_mismo_nombre_mantiene_la_ruta_y_regresa_un_success() = runTest {
        val preguntas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Pregunta 1", emptyList()),
                    QuestionContentDomain.Image("", "1${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )
        val respuestas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Respuesta 1", emptyList()),
                    QuestionContentDomain.Image("", "2${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )

        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val baseFolderTest = temporaryFolder.newFolder(
            folderFiles,
            StorageFolders.GUIAS,
            folderKotlin,
            folderTest
        ).absolutePath
        val guideContext = GuideContext.Rename(
            guide = guideDomainModel,
            name = RequiredAttrGuide("Test"),
            description = OptionalAttrGuide("Test de prueba")
        )
        val oldGuidePath =
            File(baseFolderTest, "Test${Extensions.POINT_XML_EXTENSION}")
        oldGuidePath.writeText(xmlTestV2)
        val newGuidePath =
            File(
                baseFolderTest,
                "${guideContext.name.value}${Extensions.POINT_XML_EXTENSION}"
            )
        val tempGuidePath = File(baseFolderTest, "Test${Extensions.POINT_XML_EXTENSION}.tmp")
        val mockSerializer = mockk<XmlSerializer>(relaxed = true)
        val newGuideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = guideContext.name.value,
            description = guideContext.description.value
        )

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideContext.guide,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldGuidePath.absolutePath)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = newGuideDomain,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(newGuidePath.absolutePath)

        // 2. Le decimos a la fábrica que devuelva nuestro mock
        every { xmlSerializerFactory.create() } returns mockSerializer

        // En lugar de usar ByteArrayOutputStream en memoria, usa un FileOutputStream real
        every { fileOutputStreamFactory.create(any()) } answers {
            val filePath = firstArg<String>()
            FileOutputStream(filePath)
        }

        assertTrue("Debe existir el archivo con el nombre anterior", oldGuidePath.exists())
        assertTrue(
            "Debe existir el archivo ya que es el mismo nombre y ruta",
            newGuidePath.exists()
        )
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())

        val response = repository.renameGuide(preguntas, respuestas, guideContext)

        assertTrue(
            "Debe existir el archivo con el mismo nombre porque es igual que la nueva",
            oldGuidePath.exists()
        )
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())
        assertTrue(
            "Debe existir el archivo con el mismo nombre porque es igual que el viejo",
            newGuidePath.exists()
        )
        assertEquals(GuideResource.Success(newGuideDomain), response)
    }

    // saveGuide

    @Test
    fun lanzar_excepcion_cuando_archivo_no_tiene_directorio_padre() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relativeGuidePath = RelativeGuidePath(folderKotlin)

        every { xmlSerializerFactory.create() } returns mockk(relaxed = true)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS
            )
        } returns GuidePath("*")

        every {
            filePathResolver.getPathGuidesV2(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS,
                relativeGuidePath = relativeGuidePath
            )
        } returns "*"

        try {
            repository.saveGuide(guideDomainModel, emptyList(), emptyList())
            fail("Se esperaba IllegalStateException debido a la falta de directorio padre")
        } catch (e: IllegalStateException) {
            assertEquals(
                /* message = */ "El mensaje de error debe ser el esperado",
                /* expected = */ "El archivo no tiene directorio padre",
                /* actual = */ e.message
            )
        }
    }

    @Test
    fun crear_directorio_padre_satisfactoriamente_si_no_existe() = runTest {
        val preguntas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Pregunta 1", emptyList()),
                    QuestionContentDomain.Image("", "1${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )
        val respuestas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Respuesta 1", emptyList()),
                    QuestionContentDomain.Image("", "2${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relativeGuidePath = RelativeGuidePath(File(folderKotlin, folderTest).path)

        val basePath = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS)
        val fullBasePath = File(basePath, relativeGuidePath.value)
        val pathGuideV2 = File(fullBasePath, "Test${Extensions.POINT_XML_EXTENSION}")
        val mockSerializer = mockk<XmlSerializer>(relaxed = true)

        every { xmlSerializerFactory.create() } returns mockSerializer

        every { fileOutputStreamFactory.create(any()) } answers {
            val filePath = firstArg<String>()
            FileOutputStream(filePath)
        }

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel,
                PathKind.GUIAS
            )
        } returns GuidePath(pathGuideV2.absolutePath)

        every {
            filePathResolver.getPathGuidesV2(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS,
                relativeGuidePath = relativeGuidePath
            )
        } returns pathGuideV2.absolutePath

        assertFalse(
            /* message = */ "El directorio padre no debería existir en el disco antes de llamar al repositorio",
            /* condition = */ fullBasePath.exists()
        )

        val response =
            repository.saveGuide(guideDomainModel, preguntas, respuestas)

        assertTrue(
            /* message = */ "El repositorio debió haber creado el directorio padre con mkdirs()",
            /* condition = */ fullBasePath.exists()
        )
        assertTrue(
            /* message = */ "El archivo final debió crearse dentro del nuevo directorio",
            /* condition = */ pathGuideV2.exists()
        )
        assertEquals(
            /* message = */ "Debe retornar que se guardó exitosamente",
            /* expected = */ GuideResource.Success(guideDomainModel),
            /* actual = */ response
        )
    }

    @Test
    fun lanzar_IOException_si_no_se_puede_crear_el_directorio_padre() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relativeGuidePath =
            RelativeGuidePath(File(folderKotlin, "$folderTest\u0000").path)

        val basePath = temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS)
        val fullBasePath = File(basePath, relativeGuidePath.value)
        val pathGuideV2 = File(fullBasePath, "Test${Extensions.POINT_XML_EXTENSION}")

        val mockSerializer = mockk<XmlSerializer>(relaxed = true)
        every { xmlSerializerFactory.create() } returns mockSerializer

        every { fileOutputStreamFactory.create(any()) } answers {
            val filePath = firstArg<String>()
            FileOutputStream(filePath)
        }

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(pathGuideV2.absolutePath)

        every {
            filePathResolver.getPathGuidesV2(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS,
                relativeGuidePath = relativeGuidePath
            )
        } returns pathGuideV2.absolutePath

        assertFalse(pathGuideV2.exists())

        val excepcionLanzada = assertFailsWith<IOException> {
            repository.saveGuide(guideDomainModel, emptyList(), emptyList())
        }

        val mensajeEsperado = "No se pudo crear el directorio: ${fullBasePath.absolutePath}"
        assertEquals(
            /* message = */ "No debe crearse el directorio y lanzar una excepcion",
            /* expected = */ mensajeEsperado,
            /* actual = */ excepcionLanzada.message
        )
    }

    @Test
    fun tira_un_error_al_actualizar_guia_v2_y_devuelve_CommitChangesFailed() = runTest {
        val preguntas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Pregunta 1", emptyList()),
                    QuestionContentDomain.Image("", "1${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )
        val respuestas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Respuesta 1", emptyList()),
                    QuestionContentDomain.Image("", "2${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )

        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relGuidePathFile = File(folderKotlin, folderTest)
        val relativeGuidePath = RelativeGuidePath(relGuidePathFile.path)
        val pathFolderTest =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderTest)
        val oldGuidePath =
            File(pathFolderTest, "Test${Extensions.POINT_XML_EXTENSION}")
        oldGuidePath.writeText(xmlTestV2)
        val newGuidePath =
            File(pathFolderTest, "Test${Extensions.POINT_XML_EXTENSION}")
        val tempGuidePath = File(pathFolderTest, "Test${Extensions.POINT_XML_EXTENSION}.tmp")

        val mockSerializer = mockk<XmlSerializer>(relaxed = true)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldGuidePath.absolutePath)

        every {
            filePathResolver.getPathGuidesV2(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS,
                relativeGuidePath = relativeGuidePath
            )
        } returns newGuidePath.absolutePath

        // 2. Le decimos a la fábrica que devuelva nuestro mock
        every { xmlSerializerFactory.create() } returns mockSerializer

        // No crea ningún archivo real, lo crea en memoria RAM
        val mockOutputStream = ByteArrayOutputStream()
        every { fileOutputStreamFactory.create(any()) } returns mockOutputStream

        assertTrue("Debe existir el archivo con el nombre anterior", oldGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())

        val response =
            repository.saveGuide(guideDomainModel, preguntas, respuestas)

        assertTrue("Debe existir el archivo con el nombre anterior", oldGuidePath.exists())
        assertTrue("Debe existir el archivo con el nuevo nombre", newGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())
        assertEquals(GuideResource.Error(SaveGuideErrors.CommitChangesFailed), response)
    }

    @Test
    fun no_hay_suficiente_espacio_al_guardar_una_guia_y_regresa_InsufficientStorageOrDiskError() =
        runTest {
            val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
            val relGuideFile = File(folderKotlin, folderTest)
            val baseTestFolder = temporaryFolder.newFolder(
                folderFiles,
                StorageFolders.GUIAS,
                folderKotlin,
                folderTest
            ).absolutePath
            val relativeGuidePath = RelativeGuidePath(relGuideFile.path)
            val newGuidePath =
                File(
                    baseTestFolder,
                    "${guideDomainModel.nameGuide}${Extensions.POINT_XML_EXTENSION}"
                )
            newGuidePath.createNewFile()
            val tempGuidePath =
                File(
                    baseTestFolder,
                    "${guideDomainModel.nameGuide}${Extensions.POINT_XML_EXTENSION}.tmp"
                )

            coEvery {
                filePathResolver.mapToFilePathSpecificGuide(
                    guideDomainModel = guideDomainModel,
                    kind = PathKind.GUIAS
                )
            } returns GuidePath(newGuidePath.absolutePath)

            every {
                filePathResolver.getPathGuidesV2(
                    guideDomainModel = guideDomainModel,
                    kind = PathKind.GUIAS,
                    relativeGuidePath = relativeGuidePath
                )
            } returns newGuidePath.absolutePath

            every {
                xmlSerializerFactory.create()
            } throws IOException("Fallo forzado de espacio insuficiente")

            assertTrue("Debe existir el archivo con el nombre anterior", newGuidePath.exists())
            assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())

            val response =
                repository.saveGuide(guideDomainModel, emptyList(), emptyList())

            assertTrue("Debe existir el archivo con el nombre anterior", newGuidePath.exists())
            assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())
            assertEquals(
                GuideResource.Error(SaveGuideErrors.InsufficientStorageOrDiskError),
                response
            )
        }

    @Test
    fun sino_hay_permisos_de_escritura_disponibles_regresa_StoragePermissionDenied() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relGuideFile = File(folderKotlin, folderTest)
        val baseTestFolder = temporaryFolder.newFolder(
            folderFiles,
            StorageFolders.GUIAS,
            folderKotlin,
            folderTest
        ).absolutePath
        val relativeGuidePath = RelativeGuidePath(relGuideFile.path)
        val newGuidePath =
            File(
                baseTestFolder,
                "${guideDomainModel.nameGuide}${Extensions.POINT_XML_EXTENSION}"
            )
        newGuidePath.createNewFile()
        val tempGuidePath =
            File(
                baseTestFolder,
                "${guideDomainModel.nameGuide}${Extensions.POINT_XML_EXTENSION}.tmp"
            )

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(newGuidePath.absolutePath)

        every {
            filePathResolver.getPathGuidesV2(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS,
                relativeGuidePath = relativeGuidePath
            )
        } returns newGuidePath.absolutePath

        every {
            xmlSerializerFactory.create()
        } throws SecurityException("Fallo forzado de StoragePermissionDenied")

        assertTrue("Debe existir el archivo con el nombre anterior", newGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())

        val response =
            repository.saveGuide(guideDomainModel, emptyList(), emptyList())

        assertTrue("Debe existir el archivo con el nombre anterior", newGuidePath.exists())
        assertFalse("No debe existir el archivo temporal", tempGuidePath.exists())
        assertEquals(GuideResource.Error(SaveGuideErrors.StoragePermissionDenied), response)
    }

    @Test
    fun actualizar_satisfactoriamente_guia_v1_a_guia_v2() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Bucles", "")
        val newGuideDomainModel = GuideDomainModel(GuideVersion.V2, "Bucles", "")

        val preguntas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Pregunta 1", emptyList()),
                    QuestionContentDomain.Image("", "1${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )
        val respuestas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Respuesta 1", emptyList()),
                    QuestionContentDomain.Image("", "2${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )

        val basePath =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val basePathV2 =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderBucles)
        val oldPathGuideV1 = File(basePath, "Bucles${Extensions.POINT_XML_EXTENSION}")
        oldPathGuideV1.createNewFile()
        val newPathGuideV2 = File(basePathV2, "Bucles${Extensions.POINT_XML_EXTENSION}")
        val mockSerializer = mockk<XmlSerializer>(relaxed = true)
        val tempGuidePath = File(oldPathGuideV1, "Bucles${Extensions.POINT_XML_EXTENSION}.tmp")

        // 2. Le decimos a la fábrica que devuelva nuestro mock
        every { xmlSerializerFactory.create() } returns mockSerializer

        // En lugar de usar ByteArrayOutputStream en memoria, usa un FileOutputStream real
        every { fileOutputStreamFactory.create(any()) } answers {
            val filePath = firstArg<String>()
            FileOutputStream(filePath)
        }

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldPathGuideV1.absolutePath)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(newGuideDomainModel, PathKind.GUIAS)
        } returns GuidePath(newPathGuideV2.absolutePath)

        assertTrue(
            /* message = */ "Debe existir el archivo con el nombre anterior",
            /* condition = */ oldPathGuideV1.exists()
        )
        assertFalse(
            /* message = */ "No debe existir el archivo con el nuevo nombre y ruta nueva",
            /* condition = */ newPathGuideV2.exists()
        )
        assertFalse(
            /* message = */ "No debe existir el archivo temporal",
            /* condition = */tempGuidePath.exists()
        )

        val response =
            repository.saveGuide(guideDomainModel, preguntas, respuestas)

        assertFalse(
            /* message = */ "No debe existir el archivo en la ruta anterior",
            /* condition = */ oldPathGuideV1.exists()
        )
        assertTrue(
            /* message = */ "Debe existir el archivo con el nuevo nombre y ruta nueva",
            /* condition = */ newPathGuideV2.exists()
        )
        assertFalse(
            /* message = */ "No debe existir el archivo temporal",
            /* condition = */tempGuidePath.exists()
        )

        assertEquals(
            /* message = */ "Debe de regresar que se guardó la guia correctamente",
            /* expected = */GuideResource.Success(newGuideDomainModel),
            /* actual = */response
        )
    }

    @Test
    fun actualizar_satisfactoriamente_guia_v2() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")

        val preguntas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Pregunta 1", emptyList()),
                    QuestionContentDomain.Image("", "1${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )
        val respuestas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Respuesta 1", emptyList()),
                    QuestionContentDomain.Image("", "2${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )

        val basePath =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin)
        val basePathV2 =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderTest)
        val oldPathGuideV2 = File(basePath, "Test${Extensions.POINT_XML_EXTENSION}")
        oldPathGuideV2.createNewFile()
        val newPathGuideV2 = File(basePathV2, "Test${Extensions.POINT_XML_EXTENSION}")
        val mockSerializer = mockk<XmlSerializer>(relaxed = true)
        val tempGuidePath = File(oldPathGuideV2, "Test${Extensions.POINT_XML_EXTENSION}.tmp")

        // 2. Le decimos a la fábrica que devuelva nuestro mock
        every { xmlSerializerFactory.create() } returns mockSerializer

        // En lugar de usar ByteArrayOutputStream en memoria, usa un FileOutputStream real
        every { fileOutputStreamFactory.create(any()) } answers {
            val filePath = firstArg<String>()
            FileOutputStream(filePath)
        }

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(oldPathGuideV2.absolutePath)

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(guideDomainModel, PathKind.GUIAS)
        } returns GuidePath(newPathGuideV2.absolutePath)

        assertTrue(
            /* message = */ "Debe existir el archivo con el nombre anterior",
            /* condition = */ oldPathGuideV2.exists()
        )
        assertFalse(
            /* message = */ "No debe existir el archivo con el nuevo nombre y ruta nueva",
            /* condition = */ newPathGuideV2.exists()
        )
        assertFalse(
            /* message = */ "No debe existir el archivo temporal",
            /* condition = */tempGuidePath.exists()
        )

        val response =
            repository.saveGuide(guideDomainModel, preguntas, respuestas)

        assertTrue(
            /* message = */ "Debe existir el archivo con el nombre anterior",
            /* condition = */ oldPathGuideV2.exists()
        )
        assertTrue(
            /* message = */ "Debe existir el archivo con el nuevo nombre y ruta nueva",
            /* condition = */ newPathGuideV2.exists()
        )
        assertFalse(
            /* message = */ "No debe existir el archivo temporal",
            /* condition = */tempGuidePath.exists()
        )

        assertEquals(
            /* message = */ "Debe de regresar que se guardó la guia correctamente",
            /* expected = */GuideResource.Success(guideDomainModel),
            /* actual = */response
        )
    }

    @Test
    fun guardar_satisfactoriamente_guia_v2() = runTest {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")

        val preguntas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Pregunta 1", emptyList()),
                    QuestionContentDomain.Image("", "1${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )
        val respuestas = listOf(
            element = QuestionItemDomain(
                content = listOf(
                    QuestionContentDomain.Text("Respuesta 1", emptyList()),
                    QuestionContentDomain.Image("", "2${Extensions.POINT_PNG_EXTENSION}")
                )
            )
        )

        val relativeGuidePath = RelativeGuidePath(folderKotlin)
        val basePath =
            temporaryFolder.newFolder(folderFiles, StorageFolders.GUIAS, folderKotlin, folderTest)
        val pathGuideV2 = File(basePath, "Test${Extensions.POINT_XML_EXTENSION}")
        val tempGuidePath = File(pathGuideV2, "Test${Extensions.POINT_XML_EXTENSION}.tmp")
        val mockSerializer = mockk<XmlSerializer>(relaxed = true)

        // 2. Le decimos a la fábrica que devuelva nuestro mock
        every { xmlSerializerFactory.create() } returns mockSerializer

        // En lugar de usar ByteArrayOutputStream en memoria, usa un FileOutputStream real
        every { fileOutputStreamFactory.create(any()) } answers {
            val filePath = firstArg<String>()
            FileOutputStream(filePath)
        }

        coEvery {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS
            )
        } returns GuidePath(pathGuideV2.absolutePath)

        every {
            filePathResolver.getPathGuidesV2(
                guideDomainModel = guideDomainModel,
                kind = PathKind.GUIAS,
                relativeGuidePath = relativeGuidePath
            )
        } returns pathGuideV2.absolutePath

        assertFalse(
            /* message = */ "No debe existir el archivo con el nuevo nombre y ruta nueva",
            /* condition = */ pathGuideV2.exists()
        )
        assertFalse(
            /* message = */ "No debe existir el archivo temporal",
            /* condition = */tempGuidePath.exists()
        )

        val response =
            repository.saveGuide(guideDomainModel, preguntas, respuestas)

        assertTrue(
            /* message = */ "Debe existir el archivo con el nuevo nombre y ruta nueva",
            /* condition = */ pathGuideV2.exists()
        )
        assertFalse(
            /* message = */ "No debe existir el archivo temporal",
            /* condition = */tempGuidePath.exists()
        )

        assertEquals(
            /* message = */ "Debe de regresar que se guardó la guia correctamente",
            /* expected = */GuideResource.Success(guideDomainModel),
            /* actual = */response
        )
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