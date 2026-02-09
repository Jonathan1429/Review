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
    private lateinit var guideDir: File
    private lateinit var repository: GuiaRepositoryImpl
    private lateinit var navigationPathRepository: FakeNavigationPathRepository
    private lateinit var filePathsProvider: FakeFilePathsProvider

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("test-guides").toFile()
        filePathsProvider = FakeFilePathsProvider(tempDir.absolutePath)
        navigationPathRepository = FakeNavigationPathRepository(GuidePath(tempDir.absolutePath))
        guideDir = File(tempDir, "GuiaTest")
        guideDir.mkdirs()

        val pathHandler = PathHandler()
        val xmlSerializerFactory = FakeXmlSerializerFactory()
        val fileOutputStreamFactory = RealFileOutputStreamFactory()

        repository = GuiaRepositoryImpl(
            pathHandler = pathHandler,
            xmlSerializerFactory = xmlSerializerFactory,
            fileOutputStreamFactory = fileOutputStreamFactory,
            filePathResolver = FakeFilePathResolverService(
                tempDir,
                navigationPathRepository,
                filePathsProvider
            )
        )
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `saveGuide creates xml file successfully`() {
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

        val expectedFile = File(guideDir, "GuiaTest.xml")

        assertTrue(result is GetSaveGuideResult.SaveGuide)
        println("Temp dir contents:")
        tempDir.walkTopDown().forEach {
            println(it.absolutePath)
        }
        assertTrue(expectedFile.exists())
        assertTrue(expectedFile.readText().contains("GuiaTest"))
    }
}