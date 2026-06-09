package com.jonathanev.review.data.filesystem

import com.jonathanev.review.data.util.PathHandler
import com.jonathanev.review.data.xml.Attributes
import com.jonathanev.review.data.xml.Structure
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.FileOutputStreamFactory
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.XmlSerializerFactory
import com.jonathanev.review.domain.result.ExistGuideV1Result
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

    // Regla de JUnit que crea y destruye una carpeta temporal real en cada test
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var repository: GuiaRepositoryImpl

    // Instancia real de tu PathHandler
    private val pathHandler = PathHandler()

    // Mocks de las dependencias
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
    fun sino_existe_la_ruta_donde_mover_el_archivo_regresa_false(){
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
    fun si_el_archivo_que_vas_a_mover_no_existe_regresa_false(){
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
}