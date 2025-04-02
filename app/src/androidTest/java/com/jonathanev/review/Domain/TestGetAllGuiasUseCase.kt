package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.R
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Test
import java.io.File

class TestGetAllGuiasUseCase {
    private val getRandomGuiaImageUseCase = mockk<GetRandomGuiaImageUseCase>()
    private val getAllGuiasUseCase = GetAllGuiasUseCase(getRandomGuiaImageUseCase)

    @Test
    fun obtiene_todas_las_carpetas_y_guias_en_ese_orden() {
        val ruta = mockk<File>() // Se simula un directorio
        val file1 = mockk<File>()
        val file2 = mockk<File>()
        val folder1 = mockk<File>()
        val folder2 = mockk<File>()

        // Configuración de mocks para los archivos
        every { file1.name } returns "A"
        every { file1.isDirectory } returns false
        every { folder1.name } returns "B"
        every { folder1.isDirectory } returns true
        every { file2.name } returns "B"
        every { file2.isDirectory } returns false
        every { folder2.name } returns "A"
        every { folder2.isDirectory } returns true

        // Configuración de mock para getRandomGuiaImage
        every { getRandomGuiaImageUseCase.invoke() } returns R.drawable.img_estudiante4

        // Configuración de mock para listFiles
        every { ruta.listFiles() } returns arrayOf(file1, file2, folder1, folder2)

        // Llamada al use case
        val resultado = getAllGuiasUseCase(ruta)

        // Verificación del resultado esperado
        assertEquals(
            listOf(
                GuiaModel("A", R.drawable.img_carpeta, true),
                GuiaModel("B", R.drawable.img_carpeta, true),
                GuiaModel("A", R.drawable.img_estudiante4, false),
                GuiaModel("B", R.drawable.img_estudiante4, false),
            ),
            resultado
        )
        // Así verificas que la injección es correcta, jamás vendrá null esta parte
        // a menos que la syntax de la injección se hiciera mal
        assertNotNull(getAllGuiasUseCase.getRandomGuiaImageUseCase)
    }

    @Test
    fun obtiene_todas_las_carpetas() {
        val ruta = mockk<File>() // Se simula un directorio
        val folder1 = mockk<File>()
        val folder2 = mockk<File>()

        // Configuración de mocks para los archivos
        every { folder1.name } returns "B"
        every { folder1.isDirectory } returns true
        every { folder2.name } returns "A"
        every { folder2.isDirectory } returns true

        // Configuración de mock para getRandomGuiaImage
        every { getRandomGuiaImageUseCase.invoke() } returns R.drawable.img_estudiante4

        // Configuración de mock para listFiles
        every { ruta.listFiles() } returns arrayOf(folder1, folder2)

        // Llamada al use case
        val resultado = getAllGuiasUseCase(ruta)

        // Verificación del resultado esperado
        assertEquals(
            listOf(
                GuiaModel("A", R.drawable.img_carpeta, true),
                GuiaModel("B", R.drawable.img_carpeta, true),
            ),
            resultado
        )
        // Así verificas que la injección es correcta, jamás vendrá null esta parte
        // a menos que la syntax de la injección se hiciera mal
        assertNotNull(getAllGuiasUseCase.getRandomGuiaImageUseCase)
    }

    @Test
    fun obtiene_todas_las_guias() {
        val ruta = mockk<File>() // Se simula un directorio
        val file1 = mockk<File>()
        val file2 = mockk<File>()

        // Configuración de mocks para los archivos
        every { file1.name } returns "A"
        every { file1.isDirectory } returns false
        every { file2.name } returns "B"
        every { file2.isDirectory } returns false

        // Configuración de mock para getRandomGuiaImage
        every { getRandomGuiaImageUseCase.invoke() } returns R.drawable.img_estudiante4

        // Configuración de mock para listFiles
        every { ruta.listFiles() } returns arrayOf(file2, file1)

        // Llamada al use case
        val resultado = getAllGuiasUseCase(ruta)

        // Verificación del resultado esperado
        assertEquals(
            listOf(
                GuiaModel("A", R.drawable.img_estudiante4, false),
                GuiaModel("B", R.drawable.img_estudiante4, false),
            ),
            resultado
        )
        // Así verificas que la injección es correcta, jamás vendrá null esta parte
        // a menos que la syntax de la injección se hiciera mal
        assertNotNull(getAllGuiasUseCase.getRandomGuiaImageUseCase)
    }

    @Test
    fun no_regresa_guias_de_estudio(){
        val ruta = mockk<File>()

        every { ruta.listFiles() } returns null

        val resultado = getAllGuiasUseCase(ruta)
        assertEquals(emptyList<GuiaModel>(), resultado)
        // Así verificas que la injección es correcta, jamás vendrá null esta parte
        // a menos que la syntax de la injección se hiciera mal
        assertNotNull(getAllGuiasUseCase.getRandomGuiaImageUseCase)
    }
}