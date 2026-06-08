package com.jonathanev.review.data.filesystem

import com.jonathanev.review.data.util.PathHandler
import com.jonathanev.review.domain.constants.Extensions
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.PathKind
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.FileOutputStreamFactory
import com.jonathanev.review.domain.repository.FilePathResolver
import com.jonathanev.review.domain.repository.XmlSerializerFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
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

    // ListFile interno de listGuides
    @Test
    fun sino_hay_ruta_en_la_cual_mostrar_archivos_regresa_lista_vacia_sublistado_listFromFolders() {
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath

        every {
            filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS)
        } returns GuidePath("$rootPathValue/Kotlin")

        val pathGuideTest = temporaryFolder.newFolder("Kotlin", "Test")
        val pathGuideSintaxis = temporaryFolder.newFolder("Kotlin", "Sintaxis")
        File(pathGuideTest, "Test.${Extensions.PNG_EXTENSION}").createNewFile()
        File(pathGuideSintaxis, "Sintaxis.${Extensions.PNG_EXTENSION}").createNewFile()

        val resultado = repository.getNumGuides(rutaPrueba)

        assertEquals(0, resultado)
    }
}