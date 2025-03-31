package com.jonathanev.review.Domain

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.File

class TestGetAllFoldersUseCase {
    private val getAllFoldersUseCase = GetAllFoldersUseCase()

    @Test
    fun `should return sorted folder names when directory contains folders`() {
        val mockFile = mockk<File>()  // Simulamos un directorio
        val folder1 = mockk<File>()   // Simulamos una carpeta llamada "B"
        val folder2 = mockk<File>()   // Simulamos una carpeta llamada "A"
        val file = mockk<File>()      // Simulamos un archivo (NO carpeta)

        every { folder1.name } returns "B"
        every { folder1.isDirectory } returns true   // Es una carpeta

        every { folder2.name } returns "A"
        every { folder2.isDirectory } returns true   // Es una carpeta

        every { file.isDirectory } returns false     // Es un archivo, no una carpeta

        every { mockFile.listFiles() } returns arrayOf(folder1, folder2, file)

        val result = getAllFoldersUseCase(mockFile)

        assertEquals(listOf("A", "B"), result)  // Verificamos que las carpetas están ordenadas
    }

    @Test
    fun `should return empty list when directory is empty`() {
        val mockFile = mockk<File>()

        every { mockFile.listFiles() } returns emptyArray()

        val result = getAllFoldersUseCase(mockFile)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `should return empty list when directory contains only files`() {
        val mockFile = mockk<File>()
        val file1 = mockk<File>()
        val file2 = mockk<File>()

        every { file1.isDirectory } returns false
        every { file2.isDirectory } returns false

        every { mockFile.listFiles() } returns arrayOf(file1, file2)

        val result = getAllFoldersUseCase(mockFile)

        assertEquals(emptyList<String>(), result)
    }
}