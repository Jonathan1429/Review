package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.FilePathsProvider
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.rules.TemporaryFolder
import java.io.File

class SetCopyImagesUseCaseTest {
    private val filePathsProvider = FilePathsProvider(mockk())

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var sourceDir: File
    private lateinit var destDir: File
    private val useCase = SetCopyImagesUseCase(filePathsProvider)

    @Before
    fun setUp() {
        // Creación de carpetas temporales "imgPiv" y "imagenes"
        sourceDir = tempFolder.newFolder("imgPiv")
        destDir   = tempFolder.newFolder("imagenes")
    }

    @Test
    fun `copia y borra imagenes cuando hay archivos`() {
        // Arrange: creo dos archivos dentro de sourceDir
        File(sourceDir, "imagen1.jpg").apply { writeText("contenido1") }
        File(sourceDir, "imagen2.jpg").apply { writeText("contenido2") }

        // Act
        useCase.invoke(imgPiv = sourceDir, imagenes = destDir)

        // Assert: en origen NO deben existir
        assertFalse(File(sourceDir, "imagen1.jpg").exists())
        assertFalse(File(sourceDir, "imagen2.jpg").exists())

        // Assert: en destino SÍ deben existir
        assertTrue(File(destDir, "imagen1.jpg").exists())
        assertTrue(File(destDir, "imagen2.jpg").exists())
    }

    @Test
    fun `no hace nada cuando no hay archivos para copiar`() {
        // sourceDir está vacío — Act
        useCase.invoke(imgPiv = sourceDir, imagenes = destDir)

        // Assert: destino sigue vacío
        val filesInDest = destDir.listFiles()
        assertNotNull(filesInDest)
        assertEquals(0, filesInDest!!.size)
    }

    @Test
    fun `invoke usa parametros por defecto sin lanzar excepcion`() {
        // Este test llama invoke() sin argumentos, para cubrir la firma con valores por defecto.
        // Como esas rutas no existen, listFiles() devolverá null y no se ejecutará el for.
        // Lo único que comprobamos es que no arroje ninguna excepción.
        useCase.invoke()
    }
}
