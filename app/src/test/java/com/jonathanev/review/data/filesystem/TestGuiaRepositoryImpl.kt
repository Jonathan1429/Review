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

    /*@Test
    fun getNumGuides_debeContarXmlEnRaizYSubcarpetasYRetornarElTotal() {
        // 1. GIVEN (Preparar carpetas y archivos simulados usando TemporaryFolder)
        val rutaPrueba = RelativeGuidePath("guias/productividad")

        // Creamos la ruta raíz que simulará el almacenamiento de Android
        val rootPathValue = temporaryFolder.root.absolutePath

        // Mockeamos la interfaz para que devuelva la ruta temporal que creamos
        // (Ajusta 'PathValue' si tu clase de envoltorio de string tiene otro nombre de propiedad)
        every { filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS) } returns GuidePath(
            rootPathValue
        )

        // --- Simulamos el escenario creando archivos reales dentro de la carpeta temporal ---
        // Caso A: 2 archivos XML en la raíz
        temporaryFolder.newFile("guia1.${Extensions.XML_EXTENSION}")
        temporaryFolder.newFile("guia2.${Extensions.XML_EXTENSION}")
        temporaryFolder.newFile("leeme.txt") // Este no debe contarse (extensión diferente)

        // Caso B: 1 subcarpeta con 1 archivo XML adentro
        val subcarpeta = temporaryFolder.newFolder("subcategoria")
        File(subcarpeta, "guia3.${Extensions.XML_EXTENSION}").createNewFile()
        File(subcarpeta, "imagen.png").createNewFile() // Este no debe contarse

        // 2. WHEN (Ejecutamos el método bajo prueba)
        // Llama a listGuides internamente, por lo que debería encontrar 3 archivos XML en total
        val resultado = repository.getNumGuides(rutaPrueba)

        // 3. THEN (Verificamos que la suma de (2 en raíz + 1 en subcarpeta) sea 3)
        assertEquals(3, resultado)
    }

    @Test
    fun regresa_lista_vacia_por_tener_solo_carpetas_visibles() {
        // 1. GIVEN (Preparar carpetas y archivos simulados usando TemporaryFolder)
        val rutaPrueba = RelativeGuidePath("guias/productividad")

        // Creamos la ruta raíz que simulará el almacenamiento de Android
        val rootPathValue = temporaryFolder.root.absolutePath

        // Mockeamos la interfaz para que devuelva la ruta temporal que creamos
        every {
            filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS)
        } returns GuidePath(rootPathValue)

        // --- Simulamos el escenario creando archivos reales dentro de la carpeta temporal ---
        // Caso A: 2 archivos XML en la raíz
        temporaryFolder.newFolder("Prueba")

        // 2. WHEN (Ejecutamos el método bajo prueba)
        // Llama a listGuides internamente, por lo que debería encontrar 3 archivos XML en total
        val resultado = repository.getNumGuides(rutaPrueba)

        // 3. THEN (Verificamos que la suma de (2 en raíz + 1 en subcarpeta) sea 3)
        assertEquals(0, resultado)
    }*/

    @Test
    fun sino_hay_ruta_en_la_cual_mostrar_archivos_regresa_lista_vacia() {
        val rutaPrueba = RelativeGuidePath("guias/ruta_invalida")

        val archivoFalsoComoDirectorio = temporaryFolder.newFile("un_archivo_cualquiera.txt")
        val rutaComoArchivo = archivoFalsoComoDirectorio.absolutePath

        every {
            filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS)
        } returns GuidePath(rutaComoArchivo)

        val resultado = repository.getNumGuides(rutaPrueba)

        assertEquals(0, resultado)
    }

    @Test
    fun regresa_lista_vacia_por_solo_existir_carpetas_en_la_ruta() {
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath

        every {
            filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS)
        } returns GuidePath(rootPathValue)

        temporaryFolder.newFolder("Kotlin")

        val resultado = repository.getNumGuides(rutaPrueba)

        assertEquals(0, resultado)
    }

    @Test
    fun regresa_la_lista_de_guias_existentes_en_la_ruta() {
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath

        every {
            filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS)
        } returns GuidePath(rootPathValue)

        temporaryFolder.newFile("Kotlin.${Extensions.XML_EXTENSION}")
        temporaryFolder.newFile("Abap.${Extensions.XML_EXTENSION}")

        val resultado = repository.getNumGuides(rutaPrueba)

        assertEquals(2, resultado)
    }

    fun regresa_lista_vacia_por_existir_archivos_distintos_a_una_guia() {
        val rutaPrueba = RelativeGuidePath("guias/productividad")
        val rootPathValue = temporaryFolder.root.absolutePath

        every {
            filePathResolver.mapToFolderPath(rutaPrueba, PathKind.GUIAS)
        } returns GuidePath(rootPathValue)

        temporaryFolder.newFile("Kotlin.${Extensions.PNG_EXTENSION}")

        val resultado = repository.getNumGuides(rutaPrueba)

        assertEquals(0, resultado)
    }
}