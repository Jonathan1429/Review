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

    @Before
    fun setUp() {
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
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath

        every {
            filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS)
        } returns GuidePath("$rootPathValue/Kotlin")

        val folderCreated = temporaryFolder.newFolder("Kotlin")
        File(folderCreated, "Test.${Extensions.XML_EXTENSION}").createNewFile()
        File(folderCreated, "Documentacion.${Extensions.XML_EXTENSION}").createNewFile()
        File(folderCreated, "Imagen1.${Extensions.PNG_EXTENSION}").createNewFile()
        File(folderCreated, "Imagen2.${Extensions.PNG_EXTENSION}").createNewFile()

        val resultado = repository.getNumGuides(rutaPrueba)

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
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Sintaxis",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, rutaPrueba)
        } returns "$rootPathValue/Kotlin/Sintaxis.xml"

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
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "Sintaxis",
            description = ""
        )

        every {
            filePathResolver.getPathGuidesV1(guideDomain, PathKind.GUIAS, rutaPrueba)
        } returns "$rootPathValue/Kotlin/Sintaxis.xml"

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

        val resultado = repository.existXMLGuideV1(guideDomain, rutaPrueba)

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
        val separator = "/"
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Test", "")
        val relativeGuidePath = RelativeGuidePath("Kotlin")
        val rootPathValue =
            "${temporaryFolder.root.absolutePath}${separator}files${separator}${StorageFolders.GUIAS}".replace(
                "\\",
                "/"
            )
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
                            uri = "${rootPathValue}${separator}${relativeGuidePath.value}${separator}1.png"
                                .replace(
                                    oldValue = StorageFolders.GUIAS,
                                    newValue = StorageFolders.IMAGENES
                                ),
                            nameFile = "1.png"
                        )
                    )
                ),
                answer = QuestionItemDomain(
                    content = listOf(
                        QuestionContentDomain.Image(
                            uri = "${rootPathValue}${separator}${relativeGuidePath.value}${separator}2.png"
                                .replace(
                                    oldValue = StorageFolders.GUIAS,
                                    newValue = StorageFolders.IMAGENES
                                ),
                            nameFile = "2.png"
                        )
                    )
                )
            )
        )

        temporaryFolder.newFolder("files${separator}guias${separator}Kotlin")

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath("$rootPathValue${separator}${relativeGuidePath.value}${separator}Test.xml")

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

        val fileSintaxis =
            File(
                "$rootPathValue${separator}${relativeGuidePath.value}",
                "Test.${Extensions.XML_EXTENSION}"
            )
        fileSintaxis.writeText(xmlTest)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.Success(guideDomainModel, itemsResponse), response)
    }

    @Test
    fun recuperacion_de_guia_v1_formato_invalido() {
        val separator = File.separator
        val guideDomainModel = GuideDomainModel(GuideVersion.V1, "Test", "")
        val relativeGuidePath = RelativeGuidePath("Kotlin")
        val rootPathValue = "${temporaryFolder.root}${separator}files${separator}guias"

        temporaryFolder.newFolder("files${separator}guias${separator}Kotlin")

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath("$rootPathValue${separator}${relativeGuidePath.value}${separator}Test.xml")

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION1}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Test">
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="Pregunta 1" ${XmlTagsV1.RESPUESTA}="Respuesta 1"//>
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/1.sqj" ${XmlTagsV1.RESPUESTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/2.sqj"/>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        val fileSintaxis =
            File("$rootPathValue/${relativeGuidePath.value}", "Test.${Extensions.XML_EXTENSION}")
        fileSintaxis.writeText(xmlTest)

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

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION1}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Test">
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="Pregunta 1" ${XmlTagsV1.RESPUESTA}="Respuesta 1"/>
                <${XmlTagsV2.INTERROGANTE} ${XmlTagsV1.PREGUNTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/1.sqj" ${XmlTagsV1.RESPUESTA}="frqwhqw://phgld/slfnhu/orqx/fjxdbkbp/2.sqj"/>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()
        fileSintaxis.writeText(xmlTest)

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
        val separator = "/"
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Configuracion", "")
        val relativeGuidePath = RelativeGuidePath("GIT${separator}Configuracion")
        val rootPathValue =
            "${temporaryFolder.root.absolutePath}${separator}files${separator}${StorageFolders.GUIAS}".replace(
                "\\",
                "/"
            )
        val itemsResponse = listOf(
            QAItemDomain(
                question = QuestionItemDomain(
                    content = listOf(
                        QuestionContentDomain.Text("Pregunta 1", emptyList()),
                        QuestionContentDomain.Image(
                            "${
                                rootPathValue.replace(
                                    StorageFolders.GUIAS,
                                    StorageFolders.IMAGENES
                                )
                            }${separator}${relativeGuidePath.value}${separator}1.png",
                            "1.png"
                        )
                    )
                ),
                answer = QuestionItemDomain(
                    content = listOf(
                        QuestionContentDomain.Text("Respuesta 1", emptyList()),
                        QuestionContentDomain.Image(
                            "${
                                rootPathValue.replace(
                                    StorageFolders.GUIAS,
                                    StorageFolders.IMAGENES
                                )
                            }${separator}${relativeGuidePath.value}${separator}2.png",
                            "2.png"
                        )
                    )
                )
            )
        )

        temporaryFolder.newFolder("files${separator}guias${separator}${relativeGuidePath.value}")

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath("${rootPathValue}${separator}${relativeGuidePath.value}${separator}Configuracion.xml")

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Configuracion">
                <${XmlTagsV2.QUESTION} posQuestion="0">  
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Pregunta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="1.png"/>
                </${XmlTagsV2.QUESTION}> 
                <${XmlTagsV2.ANSWER} posAnswer="0"> 
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Respuesta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="2.png"/>
                </${XmlTagsV2.ANSWER}>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        val fileSintaxis =
            File(
                "$rootPathValue${separator}${relativeGuidePath.value}",
                "Configuracion.${Extensions.XML_EXTENSION}"
            )
        fileSintaxis.writeText(xmlTest)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.Success(guideDomainModel, itemsResponse), response)
    }

    @Test
    fun recuperacion_de_guia_v2_formato_invalido() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relativeGuidePath = RelativeGuidePath("Kotlin/Test")
        val rootPathValue =
            "${temporaryFolder.root.absolutePath}/files/${StorageFolders.GUIAS}".replace(
                "\\",
                "/"
            )

        temporaryFolder.newFolder("files", "guias", "Kotlin", "Test")

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath,
                PathKind.GUIAS
            )
        } returns GuidePath("$rootPathValue/${relativeGuidePath.value}/Test.${Extensions.XML_EXTENSION}")

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Configuracion">
                <${XmlTagsV2.QUESTION} posQuestion="0">  
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Pregunta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="1.png"//>
                </${XmlTagsV2.QUESTION}> 
                <${XmlTagsV2.ANSWER} posAnswer="0"> 
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Respuesta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="2.png"/>
                </${XmlTagsV2.ANSWER}>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        val fileSintaxis =
            File("$rootPathValue/${relativeGuidePath.value}", "Test.${Extensions.XML_EXTENSION}")
        fileSintaxis.writeText(xmlTest)

        val response = repository.getXMLGuide(guideDomainModel, relativeGuidePath)

        assertEquals(GetGuideResult.InvalidFormat, response)
    }

    @Test
    fun recuperacion_de_guia_v2_archivo_no_encontrado() {
        val guideDomainModel = GuideDomainModel(GuideVersion.V2, "Test", "")
        val relativeGuidePath = RelativeGuidePath("Kotlin/Test")
        val rootPathValue =
            "${temporaryFolder.root.absolutePath}/files/${StorageFolders.GUIAS}".replace(
                "\\",
                "/"
            )

        temporaryFolder.newFolder("Kotlin", "Test")

        every {
            filePathResolver.mapToFilePathSpecificGuide(
                guideDomainModel, relativeGuidePath, PathKind.GUIAS
            )
        } returns GuidePath("$rootPathValue/$relativeGuidePath/Test.xml")

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

        val xmlTest = """
        <${Structure.GUIAESTUDIO} ${Attributes.VERSION}="${Versions.VERSION2}">
            <${Structure.CUESTIONARIO} ${Attributes.NOMBREGUIA}="Configuracion">
                <${XmlTagsV2.QUESTION} posQuestion="0">  
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Pregunta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="1.png"/>
                </${XmlTagsV2.QUESTION}> 
                <${XmlTagsV2.ANSWER} posAnswer="0"> 
                    <${XmlTagsV2.TEXTO} ${XmlTagsV2.TEXTO}= "Respuesta 1"/>
                    <${XmlTagsV2.IMAGEN} ${Attributes.URI}= "" ${Attributes.NAMEFILE}="2.png"/>
                </${XmlTagsV2.ANSWER}>
            </${Structure.CUESTIONARIO}>
        </${Structure.GUIAESTUDIO}>
    """.trimIndent()

        fileSintaxis.writeText(xmlTest)

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
}