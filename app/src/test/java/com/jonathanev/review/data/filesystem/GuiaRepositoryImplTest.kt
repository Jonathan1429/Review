package com.jonathanev.review.data.filesystem

import com.jonathanev.review.data.infraestructure.RealFileOutputStreamFactory
import com.jonathanev.review.data.util.PathHandler
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.result.GetSaveGuideResult
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * Integration test:
 * - Writes real XML file
 * - Uses filesystem
 */
class GuiaRepositoryImplTest {
    lateinit var tempDir: File
    private lateinit var repository: GuiaRepositoryImpl
    private lateinit var navigationPathRepository: FakeNavigationPathRepository
    private lateinit var filePathsProvider: FakeFilePathsProvider

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("test-guides").toFile()

        filePathsProvider = FakeFilePathsProvider(tempDir.absolutePath)

        navigationPathRepository =
            FakeNavigationPathRepository(
                GuidePath(tempDir.absolutePath)
            )

        val pathHandler = PathHandler()
        val xmlSerializerFactory = FakeXmlSerializerFactory()
        val fileOutputStreamFactory = RealFileOutputStreamFactory()

        repository = GuiaRepositoryImpl(
            pathHandler = pathHandler,
            xmlSerializerFactory = xmlSerializerFactory,
            fileOutputStreamFactory = fileOutputStreamFactory,
            filePathResolver = FakeFilePathResolverService(
                baseDir = tempDir,
                navigationPathRepository = navigationPathRepository,
                filePathsProvider = filePathsProvider
            )
        )
    }


    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun saveGuide_creates_xml_file_successfully() {
        val guide = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "GuiaTest",
            description = "Descripcion"
        )

        val result = repository.saveGuide(
            guideDomainModel = guide,
            preguntas = emptyList(),
            respuestas = emptyList(),
            relativeGuidePath = RelativeGuidePath("")
        )

        // 👇 ubicación real según la regla actual
        val expectedFile = File(
            File(tempDir, "GuiaTest"),
            "GuiaTest.xml"
        )

        assertTrue(result is GetSaveGuideResult.SaveGuide)
        assertTrue(expectedFile.exists())
        assertTrue(expectedFile.readText().contains("<GuiaEstudio"))
        assertTrue(expectedFile.readText().contains("GuiaTest"))
    }


    @Test
    fun saveGuide_fails_if_directory_does_not_exist() {
        val guide = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "GuiaNueva",
            description = "Desc"
        )

        val relativePath = RelativeGuidePath("NuevaCarpeta") // NO existe

        val result = repository.saveGuide(
            guideDomainModel = guide,
            preguntas = emptyList(),
            respuestas = emptyList(),
            relativeGuidePath = relativePath
        )

        assertTrue(result is GetSaveGuideResult.Failure)
    }

    @Test
    fun saveGuide_deletes_old_file_when_version_is_V1() {
        val guide = GuideDomainModel(
            version = GuideVersion.V1,
            nameGuide = "GuiaTest",
            description = "Desc"
        )

        val relativePath = RelativeGuidePath("")

        // 👇 archivo viejo (V1) en raíz
        val oldFile = File(tempDir, "GuiaTest.xml")
        oldFile.writeText("OLD CONTENT")

        val result = repository.saveGuide(
            guideDomainModel = guide,
            preguntas = emptyList(),
            respuestas = emptyList(),
            relativeGuidePath = relativePath
        )

        // 👇 archivo nuevo (V2) EN CARPETA
        val newFile = File(
            File(tempDir, "GuiaTest"),
            "GuiaTest.xml"
        )

        assertTrue(result is GetSaveGuideResult.SaveGuide)
        assertTrue(newFile.exists())
        assertTrue(newFile.readText().contains("GuiaEstudio"))
        assertTrue(!newFile.readText().contains("OLD CONTENT"))
    }


    @Test
    fun saveGuide_writes_valid_xml_structure() {
        val guide = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "GuiaXML",
            description = "Desc"
        )

        repository.saveGuide(
            guideDomainModel = guide,
            preguntas = emptyList(),
            respuestas = emptyList(),
            relativeGuidePath = RelativeGuidePath("")
        )

        // 👇 ubicación real según la regla actual
        val file = File(
            File(tempDir, "GuiaXML"),
            "GuiaXML.xml"
        )

        val xml = file.readText()

        assertTrue(xml.contains("<GuiaEstudio"))
        assertTrue(xml.contains("<Cuestionario"))
        assertTrue(xml.contains("version=\"2.0\""))
    }

}