package com.jonathanev.review.Domain

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.File

class TestGetAllFoldersUseCase {
    private val getAllFoldersUseCase = GetAllFoldersUseCase()

    @Test
    fun `te regresa todo vacio ya que no hay directorios ni archivos`() {
        val mockFile = mockk<File>()

        every { mockFile.listFiles() } returns emptyArray()

        val result = getAllFoldersUseCase(mockFile)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `regresa lista vacía si listFiles regresa null`() {
        val mockFile = mockk<File>()

        every { mockFile.listFiles() } returns null

        val result = getAllFoldersUseCase(mockFile)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `te regresa solo directorios`() {
        val mockFile = mockk<File>()
        val folder1 = mockk<File>()
        val folder2 = mockk<File>()

        every { folder1.name } returns "A"
        every { folder1.isDirectory } returns true

        every { folder2.name } returns "B"
        every { folder2.isDirectory } returns true

        every { mockFile.listFiles() } returns arrayOf(folder1, folder2)

        val result = getAllFoldersUseCase(mockFile)

        assertEquals(listOf("A", "B"), result)
    }

    @Test
    fun `te regresa solo archivos`() {
        val mockFile = mockk<File>()
        val file1 = mockk<File>()
        val file2 = mockk<File>()

        every { file1.name } returns "B.txt"
        every { file1.isDirectory } returns false

        every { file2.name } returns "A.txt"
        every { file2.isDirectory } returns false

        every { mockFile.listFiles() } returns arrayOf(file1, file2)

        val result = getAllFoldersUseCase(mockFile)

        assertEquals(listOf("A.txt", "B.txt"), result)
    }

    @Test
    fun `te va a regresar las carpetas y archivos ordenados alfabeticamente`() {
        val mockFile = mockk<File>()  // Simulamos un directorio
        val folder1 = mockk<File>()   // Simulamos una carpeta llamada "B"
        val folder2 = mockk<File>()   // Simulamos una carpeta llamada "A"
        val file = mockk<File>()      // Simulamos un archivo (NO carpeta)

        every { file.name } returns "A.txt"
        every { file.isDirectory } returns false     // Es un archivo, no una carpeta

        every { folder2.name } returns "A"
        every { folder2.isDirectory } returns true   // Es una carpeta

        every { folder1.name } returns "B"
        every { folder1.isDirectory } returns true   // Es una carpeta


        every { mockFile.listFiles() } returns arrayOf(folder1, folder2, file)

        val result = getAllFoldersUseCase(mockFile)

        assertEquals(listOf("A", "B", "A.txt"), result)  // Verificamos que las carpetas están ordenadas
    }
}