package com.jonathanev.review.data.filesystem

import com.jonathanev.review.core.media.MediaPaths
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
import java.io.File

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
    private lateinit var xmlSintaxisV1: String
    private lateinit var folderKotlin: String
    private lateinit var folderTest: String
    private lateinit var folderFiles: String
    private lateinit var folderGuides: String

    @Before
    fun setUp() {
        folderKotlin = "Kotlin"
        folderTest = "Test"
        folderFiles = "files"
        folderGuides = "guides"

        xmlSintaxisV1 = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION1}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Sintaxis">
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="Pregunta 1" ${XmlTagsV1.RESPUESTA}="Respuesta 1"/>
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/1.sqj" ${XmlTagsV1.RESPUESTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/2.sqj"/>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        xmlTestV2 = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Configuracion">
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
    fun regresa_la_lista_de_solo_guias_en_la_ruta() {
        val relativePath = RelativeGuidePath(folderKotlin)
        val rootPath = temporaryFolder.newFolder(folderKotlin)

        every {
            filePathResolver.mapToFolderPath(relativePath, PathKind.GUIAS)
        } returns GuidePath(rootPath.absolutePath)

        File(rootPath, "Test.${Extensions.XML_EXTENSION}").createNewFile()
        File(rootPath, "Documentacion.${Extensions.XML_EXTENSION}").createNewFile()
        File(rootPath, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(rootPath, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        val resultado = repository.getNumGuides(relativePath)

        assertEquals(2, resultado)
    }

    // ListFile interno de listGuides - Guias V2
    @Test
    fun sino_hay_ruta_en_la_cual_mostrar_archivos_regresa_lista_vacia_sublistado_listFromFolders() {
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath

        every {
            filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS)
        } returns GuidePath("$rootPathValue/Kotlin")

        val pathGuideTest = temporaryFolder.newFolder("Kotlin", "Test")
        val pathGuideSintaxis = temporaryFolder.newFolder("Kotlin", "Sintaxis")
        File(pathGuideTest, "Test.${Extensions.XML_EXTENSION}").createNewFile()
        File(pathGuideSintaxis, "Sintaxis.${Extensions.XML_EXTENSION}").createNewFile()

        val resultado = repository.getNumGuides(rutaPrueba)

        assertEquals(2, resultado)
    }

    // getGuides
    @Test
    fun no_regresa_guias_en_la_ruta_actual() {
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath
        val listGuideDomainModel = listOf(
            GuideDomainModel(
                version = GuideVersion.V1,
                nameGuide = "Sintaxis",
                description = "Descripcion de sintaxis"
            ),
            GuideDomainModel(
                version = GuideVersion.V2,
                nameGuide = "Test",
                description = "Descripcion de test"
            )
        )

        every {
            filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS)
        } returns GuidePath("$rootPathValue/Kotlin")

        val pathGuideTest = temporaryFolder.newFolder("Kotlin", "Test")

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} 
                ${Attributes.DESCRIPCION}="Descripcion de test" 
                ${Attributes.NOMBREGUIA}="Test">
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        val xmlSintaxis = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION1}">
            <${Structure.CUESTIONARIO} 
                ${Attributes.DESCRIPCION}="Descripcion de sintaxis" 
                ${Attributes.NOMBREGUIA}="Sintaxis">
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        val fileTest = File(pathGuideTest, "Test.${Extensions.XML_EXTENSION}")
        fileTest.writeText(xmlTest)

        val fileSintaxis =
            File("$rootPathValue/Kotlin", "Sintaxis.${Extensions.XML_EXTENSION}")
        fileSintaxis.writeText(xmlSintaxis)

        val resultado = repository.getGuides(rutaPrueba)

        assertEquals(listGuideDomainModel, resultado)
    }

    // existXMLGuideV1
    @Test
    fun muestra_cuando_no_existe_una_guia_v1() {
        val rutaPrueba = RelativeGuidePath("Kotlin")
        val rootPathValue = temporaryFolder.newFolder(rutaPrueba.value)
        val pathGuide = File(rootPathValue, "Sintaxis${Extensions.POINT_XML_EXTENSION}")
        val pathGuideNat = pathGuide.absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Sintaxis",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, rutaPrueba)
        } returns pathGuideNat

        val pathGuideTest = temporaryFolder.newFolder("Kotlin", "Test")

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} 
                ${Attributes.DESCRIPCION}="Descripcion de test" 
                ${Attributes.NOMBREGUIA}="Test">
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        val xmlSintaxis = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} 
                ${Attributes.DESCRIPCION}="Descripcion de sintaxis" 
                ${Attributes.NOMBREGUIA}="Sintaxis">
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        val fileTest = File(pathGuideTest, "Test.${Extensions.XML_EXTENSION}")
        fileTest.writeText(xmlTest)

        val fileSintaxis =
            File("$rootPathValue/Kotlin", "Sintaxis.${Extensions.XML_EXTENSION}")
        fileSintaxis.writeText(xmlSintaxis)

        val resultado = repository.existXMLGuideV1(guideDomain, rutaPrueba)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun muestra_cuando_existe_una_guia_v1() {
        val relativeGuidePath = RelativeGuidePath(folderKotlin)
        val rootPath = temporaryFolder.newFolder(folderFiles, folderGuides)
        val pathKotlin = File(rootPath, folderKotlin)
        pathKotlin.mkdirs()

        val pathTest = File(pathKotlin, folderTest)
        pathTest.mkdirs()

        val pathGuideSintaxis = File(pathKotlin, "Sintaxis${Extensions.POINT_XML_EXTENSION}")
        val pathGuideSintaxisNat = pathGuideSintaxis.absolutePath
        val pathGuideTest = File(pathTest, "Test${Extensions.POINT_XML_EXTENSION}")

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

    @Test
    fun si_es_un_directorio_regresa_que_la_guia_no_existe() {
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Test",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, rutaPrueba)
        } returns "$rootPathValue/Kotlin/Test"

        temporaryFolder.newFolder("Kotlin", "Test")

        val resultado = repository.existXMLGuideV1(guideDomain, rutaPrueba)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    @Test
    fun si_no_existe_la_guia_v1_regresa_que_la_guia_no_existe() {
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Test",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, rutaPrueba)
        } returns "$rootPathValue/Kotlin/Test.xml"

        temporaryFolder.newFolder("Kotlin")

        val resultado = repository.existXMLGuideV1(guideDomain, rutaPrueba)

        assertEquals(ExistGuideV1Result.NoExistGuide, resultado)
    }

    // moveGuide
    @Test
    fun mover_la_guia_exitosamente() {
        val oldRelativeGuidePath = RelativeGuidePath("Kotlin")
        val newRelativeGuidePath = RelativeGuidePath("Abap")
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test",
            description = ""
        )
        val rootPathValue = temporaryFolder.root.absolutePath
        val oldPath = "$rootPathValue/Kotlin/Test.xml"
        val newPath = "$rootPathValue/Abap/Test.xml"

        temporaryFolder.newFolder("Abap")
        val createOldPath = temporaryFolder.newFolder("Kotlin")
        File(createOldPath, "Test.xml").createNewFile()

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
        val oldRelativeGuidePath = RelativeGuidePath("Kotlin")
        val newRelativeGuidePath = RelativeGuidePath("Abap")
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test",
            description = ""
        )
        val rootPathValue = temporaryFolder.root.absolutePath
        val oldPath = "$rootPathValue/Kotlin/Test.xml"
        val newPath = "$rootPathValue/Abap/Test.xml"

        val createOldPath = temporaryFolder.newFolder("Kotlin")
        File(createOldPath, "Test.xml").createNewFile()

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
        val oldRelativeGuidePath = RelativeGuidePath("Kotlin")
        val newRelativeGuidePath = RelativeGuidePath("Abap")
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Test",
            description = ""
        )
        val rootPathValue = temporaryFolder.root.absolutePath
        val oldPath = "$rootPathValue/Kotlin/Test.xml"
        val newPath = "$rootPathValue/Abap/Test.xml"

        temporaryFolder.newFolder("Kotlin")
        temporaryFolder.newFolder("Abap")

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
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Test", "")
        val relativeGuidePath = RelativeGuidePath("Kotlin")
        val pathFolderGuiasKotlin =
            temporaryFolder.newFolder("files", StorageFolders.GUIAS, relativeGuidePath.value)
        val pathFolderImagesKotlin =
            temporaryFolder.newFolder("files", StorageFolders.IMAGENES, relativeGuidePath.value)
        val rutaImagen1 = File(pathFolderImagesKotlin, "1${Extensions.POINT_PNG_EXTENSION}").absolutePath.toSlashPath()
        val rutaImagen2 = File(pathFolderImagesKotlin, "2${Extensions.POINT_PNG_EXTENSION}").absolutePath.toSlashPath()

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

        val archivoGuia = File(pathFolderGuiasKotlin, "Test.xml").apply { createNewFile() }
        val rutaNativaCompleta = archivoGuia.absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath(rutaNativaCompleta)

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION1}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Test">
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="Pregunta 1" ${XmlTagsV1.RESPUESTA}="Respuesta 1"/>
                <${XmlTagsV2.INTERROGANTE} 
                    ${XmlTagsV1.PREGUNTA}="${MediaPaths.ENCRYPTED_IMAGE_BASE_PATH}gdwd/gdwd/frp.mrqdwkdqhy.uhylhz/ilohv/lpdjhqhv/Nrwolq/1.sqj" 
                    ${XmlTagsV1.RESPUESTA}="${MediaPaths.ENCRYPTED_IMAGE_BASE_PATH}gdwd/gdwd/frp.mrqdwkdqhy.uhylhz/ilohv/lpdjhqhv/Nrwolq/2.sqj"/>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        archivoGuia.writeText(xmlTest)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.Success(guideDomainModel, itemsResponse), response)
    }

    @Test
    fun recuperacion_de_guia_v1_formato_invalido() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Test", "")
        val relativeGuidePath = RelativeGuidePath("Kotlin")
        val rootPathValue = temporaryFolder.newFolder("files", "guias", relativeGuidePath.value)
        val pathGuide = File(rootPathValue, "Test${Extensions.POINT_XML_EXTENSION}")
        val pathGuideNat = pathGuide.absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath(pathGuideNat)

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION1}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Test">
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="Pregunta 1" ${XmlTagsV1.RESPUESTA}="Respuesta 1"//>
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/1.sqj" ${XmlTagsV1.RESPUESTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/2.sqj"/>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        pathGuide.writeText(xmlTest)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.InvalidFormat, response)
    }

    @Test
    fun recuperacion_de_guia_v1_archivo_no_encontrado() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Test", "")
        val relativeGuidePath = RelativeGuidePath("Kotlin")
        val rootPathValue = temporaryFolder.root.absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath, PathKind.GUIAS
            )
        } returns GuidePath("$rootPathValue/$relativeGuidePath/Test.xml")

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.NotFound, response)
    }

    @Test
    fun recuperacion_de_guia_v1_error_desconocido_con_mock() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Test", "")
        val relativeGuidePath = RelativeGuidePath("")
        val pathHandlerMock = mockk<PathHandler>()
        repository = GuiaRepositoryImpl(
            pathHandler = pathHandlerMock,
            xmlSerializerFactory = xmlSerializerFactory,
            fileOutputStreamFactory = fileOutputStreamFactory,
            filePathResolver = filePathResolver
        )
        val carpetaKotlin = temporaryFolder.newFolder("Kotlin")
        val fileSintaxis = File(carpetaKotlin, "Test.${Extensions.XML_EXTENSION}")

        fileSintaxis.writeText(xmlTestV2)

        every {
            filePathResolver.mapToFilePathSpecificGuide(any(), any(), any())
        } returns GuidePath(fileSintaxis.absolutePath)

        every {
            pathHandlerMock.encrypt(any())
        } throws Exception("Fallo forzado para UnknownError")

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.UnknownError, response)
    }

    @Test
    fun recuperacion_de_guia_v2() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Configuracion", "")
        val folderGit = "Git"
        val folderConfiguracion = "Configuracion"
        val relativeNative = File(folderGit, folderConfiguracion).absolutePath
        val relativeGuidePath = RelativeGuidePath(relativeNative)
        val pathFolderGuidesConfiguracion =
            temporaryFolder.newFolder("files", StorageFolders.GUIAS, folderGit, folderConfiguracion)
        val pathFolderImagesConfiguracion =
            temporaryFolder.newFolder("files", StorageFolders.IMAGENES, folderGit, folderConfiguracion)
        val rutaImagen1 = File(
            pathFolderImagesConfiguracion,
            "1${Extensions.POINT_PNG_EXTENSION}"
        ).absolutePath.toSlashPath()
        val rutaImagen2 = File(
            pathFolderImagesConfiguracion,
            "2${Extensions.POINT_PNG_EXTENSION}"
        ).absolutePath.toSlashPath()

        val rutaCompleta =
            File(pathFolderGuidesConfiguracion, "Configuracion${Extensions.POINT_XML_EXTENSION}")
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

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Configuracion">
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

        rutaCompleta.writeText(xmlTest)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.Success(guideDomainModel, itemsResponse), response)
    }

    @Test
    fun recuperacion_de_guia_v2_formato_invalido() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val folderKotlin = "Kotlin"
        val folderTest = "Test"
        val relativeNative = File(folderKotlin, folderTest).absolutePath
        val relativeGuidePath = RelativeGuidePath(relativeNative)
        val pathFolderGuiasTest =
            temporaryFolder.newFolder("files", StorageFolders.GUIAS, folderKotlin, folderTest)
        val pathGuidesComplete = File(pathFolderGuiasTest, "Test${Extensions.POINT_XML_EXTENSION}")
        val pathGuidesCompleteNat = pathGuidesComplete.absolutePath

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath(pathGuidesCompleteNat)

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Configuracion">
                <${XmlTagsV2.QUESTION} posQuestion="0">  
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Pregunta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="1${Extensions.POINT_PNG_EXTENSION}"//>
                </${XmlTagsV2.QUESTION}> 
                <${XmlTagsV2.ANSWER} posAnswer="0"> 
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Respuesta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="2${Extensions.POINT_PNG_EXTENSION}"/>
                </${XmlTagsV2.ANSWER}>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        pathGuidesComplete.writeText(xmlTest)

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
        val relativeGuidePath = RelativeGuidePath("Kotlin/Test")
        val pathHandlerMock = mockk<PathHandler>()

        repository = GuiaRepositoryImpl(
            pathHandler = pathHandlerMock,
            xmlSerializerFactory = xmlSerializerFactory,
            fileOutputStreamFactory = fileOutputStreamFactory,
            filePathResolver = filePathResolver
        )

        val carpetaKotlin = temporaryFolder.newFolder("Kotlin", "Test")
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
        val relativeGuidePath = RelativeGuidePath("Kotlin")

        val pathFolder = temporaryFolder.newFolder("files", StorageFolders.GUIAS, "Kotlin")
        val archivoGuia = File(pathFolder, "Test.xml").apply { createNewFile() }

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
    private fun String.toXMLInvalid(): String = this.replace("</${Structure.GUIAESTUDIO}>", "<//${Structure.GUIAESTUDIO}>")
}