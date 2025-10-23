package com.jonathanev.review.UI.ViewModel

import android.text.Editable
import android.text.SpannableStringBuilder
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.PreguntaRespuestaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.DeleteContentInPiv
import com.jonathanev.review.Domain.GetGuiaUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetClickEliminarUseCase
import com.jonathanev.review.Domain.SetClickRegresarModificandoUseCase
import com.jonathanev.review.Domain.SetClickSaveUseCase
import com.jonathanev.review.Domain.SetClickSiguienteModificandoUseCase
import com.jonathanev.review.Domain.SetCopyImagesUseCase
import com.jonathanev.review.Domain.SetPintarLetraUseCase
import com.jonathanev.review.Domain.SetPintarTextosUseCase
import com.jonathanev.review.Domain.SetRollClickedUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModificarViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher() //StandardTestDispatcher()
    private val setRollClickedUseCase = mockk<SetRollClickedUseCase>()
    private val setClickRegresarModificandoUseCase = mockk<SetClickRegresarModificandoUseCase>()
    private val setClickSiguienteModicandoUseCase = mockk<SetClickSiguienteModificandoUseCase>()
    private val setClickEliminarUseCase = mockk<SetClickEliminarUseCase>()
    private val setClickSaveUseCase = mockk<SetClickSaveUseCase>()
    private val setCifrarRutaImagenUseCase = mockk<SetCifrarRutaImagenUseCase>()
    private val setPintarLetraUseCase = mockk<SetPintarLetraUseCase>(relaxed = true)
    private val getObtenerDatosXMLUseCase = mockk<GetObtenerDatosXMLUseCase>()
    private val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()
    private val setCopyImagesUseCase = mockk<SetCopyImagesUseCase>()
    private val getGuiaUseCase = mockk<GetGuiaUseCase>()
    private val filePathsProvider = mockk<FilePathsProvider>()
    private val deleteContentInPiv = mockk<DeleteContentInPiv>()
    private val dataStore: DataStoreManager = mockk(relaxed = true)
    private val fileRepositoryImpl = mockk<FileRepositoryImpl>()
    private lateinit var viewModel: ModificarViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Configurar el mock de dataStore
        coEvery { dataStore.getCountImage() } returns flowOf(0)
        coEvery { dataStore.setIncrementCounter() } just Runs
        coEvery { dataStore.resetCounter() } just Runs

        viewModel = ModificarViewModel(
            setRollClickedUseCase,
            setClickRegresarModificandoUseCase,
            setClickSiguienteModicandoUseCase,
            setClickEliminarUseCase,
            setClickSaveUseCase,
            setCifrarRutaImagenUseCase,
            setPintarLetraUseCase,
            getObtenerDatosXMLUseCase,
            setPintarTextosUseCase,
            setCopyImagesUseCase,
            getGuiaUseCase,
            deleteContentInPiv,
            dataStore,
            fileRepositoryImpl,
            filePathsProvider,
            ioDispatcher = testDispatcher,
            mainDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test contImagenes LiveData emits values`() = runTest {
        // Mock de dataStore
        coEvery { dataStore.getCountImage() } returns flowOf(5)

        val observer = mockk<Observer<Int>>(relaxed = true)
        viewModel.contImagenes.observeForever(observer)

        // Llamamos explícitamente a getCountImage()
        viewModel.getCountImage()
        advanceUntilIdle()

        // Verificar que se emitió el valor
        verify { observer.onChanged(5) }

        // Limpiar
        viewModel.contImagenes.removeObserver(observer)
    }

    // TEST 1: Verificar el getter de LiveData
    @Test
    fun `test textoImagenCorrutina LiveData getter returns correct LiveData`() {
        // Act
        val result = viewModel.textoImagenCorrutina

        // Assert
        assertNotNull(result)
    }

    // TEST 3: Probar llamaCorruIncremento() - ESTE ES EL MÁS IMPORTANTE
    @Test
    fun `test llamaCorruIncremento sets LiveData and calls increment`() = runTest {
        // Arrange
        val testCifrado = "test_encrypted_string"
        val observer = mockk<Observer<String>>(relaxed = true)

        // Observar el LiveData
        viewModel.textoImagenCorrutina.observeForever(observer)

        // Act
        viewModel.llamaCorruIncremento(testCifrado)

        // Assert
        // Verificar que se llama setIncrementCounter
        coVerify(exactly = 1) { dataStore.setIncrementCounter() }

        // Verificar que se actualiza el LiveData
        verify { observer.onChanged(testCifrado) }

        // Cleanup
        viewModel.textoImagenCorrutina.removeObserver(observer)
    }

    // TEST 4: Probar resetCounter()
    @Test
    fun `test resetCounter calls dataStore resetCounter`() = runTest {
        // Act
        viewModel.resetCounter()

        // Assert
        coVerify(exactly = 1) { dataStore.resetCounter() }
    }

    // TEST 5: Probar setIncrementCounter() indirectamente a través de llamaCorruIncremento
    @Test
    fun `test setIncrementCounter is called with IO dispatcher`() = runTest {
        // Arrange
        val testCifrado = "test"

        // Act
        viewModel.llamaCorruIncremento(testCifrado)

        // Assert - setIncrementCounter se llama internamente
        coVerify(exactly = 1) { dataStore.setIncrementCounter() }
    }

    // ======= Test para getGuia =======
    @Test
    fun `getGuia actualiza guiaModel`() {
        val ruta = "ruta/falsa"
        val guiaMock = mockk<GuiaModel>()
        every { getGuiaUseCase(ruta) } returns guiaMock

        val observer = mockk<Observer<GuiaModel>>(relaxed = true)
        viewModel.guiaModel.observeForever(observer)

        viewModel.getGuia(ruta)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { observer.onChanged(guiaMock) }
    }

    @Test
    fun `getObtenerDatosXML maneja correctamente una sola pregunta`() = runBlocking {
        val pregunta1 = mockk<PreguntaRespuestaModel> {
            every { pregunta } returns "Pregunta 1"
            every { respuesta } returns "Respuesta 1"
        }
        val preguntas = listOf(pregunta1)
        every { getObtenerDatosXMLUseCase("archivo.xml", "/ruta/falsa") } returns preguntas

        val estadoUI = EstadoUI(isThereMoreAsks = true, isUpdatedAskAns = false)
        val validaciones = ValidacionesGuiaModel(estadoUI = estadoUI)

        every {
            setPintarTextosUseCase(
                isEtPregunta = true,
                preguntas = any(),
                respuestas = any(),
                contadorPregunta = any(),
                ruta = any()
            )
        } returns validaciones

        val resultado = viewModel.getObtenerDatosXML("archivo.xml", "/ruta/falsa")

        assertEquals(validaciones, resultado)
        assertEquals(listOf("Pregunta 1"), viewModel.preguntas)
        assertEquals(listOf("Respuesta 1"), viewModel.respuestas)
    }

    @Test
    fun `getObtenerDatosXML maneja correctamente varias preguntas`() = runBlocking {
        val pregunta1 = mockk<PreguntaRespuestaModel> {
            every { pregunta } returns "Pregunta 1"
            every { respuesta } returns "Respuesta 1"
        }
        val pregunta2 = mockk<PreguntaRespuestaModel> {
            every { pregunta } returns "Pregunta 2"
            every { respuesta } returns "Respuesta 2"
        }
        val preguntas = listOf(pregunta1, pregunta2)
        every { getObtenerDatosXMLUseCase("archivo.xml", "/ruta/falsa") } returns preguntas

        val estadoUI = EstadoUI(isThereMoreAsks = true, isUpdatedAskAns = false)
        val validaciones = ValidacionesGuiaModel(estadoUI = estadoUI)

        every {
            setPintarTextosUseCase(
                isEtPregunta = true,
                preguntas = any(),
                respuestas = any(),
                contadorPregunta = any(),
                ruta = any()
            )
        } returns validaciones

        val resultado = viewModel.getObtenerDatosXML("archivo.xml", "/ruta/falsa")

        assertEquals(validaciones, resultado)
        assertEquals(listOf("Pregunta 1", "Pregunta 2"), viewModel.preguntas)
        assertEquals(listOf("Respuesta 1", "Respuesta 2"), viewModel.respuestas)
    }

    // ======= Test para getUrlImagenCifrada =======
    @Test
    fun `getUrlImagenCifrada retorna ruta cifrada`() {
        val url = "url.png"
        val cifrado = "cifrado"
        every { setCifrarRutaImagenUseCase(url, 1) } returns cifrado

        val result = viewModel.getUrlImagenCifrada(url, 1)
        assertEquals(cifrado, result)
    }

    // ======= Test para onClickImgvPrevious =======
    @Test
    fun `onClickImgvPrevious actualiza contadorPregunta y LiveData`() {
        val editable = mockk<Editable>(relaxed = true)
        val ruta = "ruta"
        val response = ValidacionesGuiaModel("ok", estadoUI = EstadoUI(isUpdatedAskAns = true))
        every {
            setClickRegresarModificandoUseCase(
                any(),
                any(),
                any(),
                editable,
                any(),
                ruta
            )
        } returns response

        val observer = mockk<Observer<ValidacionesGuiaModel>>(relaxed = true)
        viewModel.uiStateBtnBack.observeForever(observer)

        viewModel.onClickImgvPrevious(editable, true, ruta)

        assertEquals(-1, viewModel.contadorPregunta) // decrementa porque isUpdatedAskAns = true
        verify { observer.onChanged(response) }
    }

    @Test
    fun `onClickImgvPrevious actualiza contadorPregunta, LiveData y no guarda el valor`() {
        val editable = mockk<Editable>(relaxed = true)
        val ruta = "ruta"
        val response = ValidacionesGuiaModel("ok", estadoUI = EstadoUI(isUpdatedAskAns = false))
        every {
            setClickRegresarModificandoUseCase(
                any(),
                any(),
                any(),
                editable,
                any(),
                ruta
            )
        } returns response

        val observer = mockk<Observer<ValidacionesGuiaModel>>(relaxed = true)
        viewModel.uiStateBtnBack.observeForever(observer)

        viewModel.onClickImgvPrevious(editable, true, ruta)

        assertEquals(0, viewModel.contadorPregunta) // no decrementa porque isUpdatedAskAns = false
        verify { observer.onChanged(response) }
    }

    // ======= Test para setPintarLetra =======
    @Test
    fun `setPintarLetra llama a useCase`() {
        val editable = mockk<Editable>(relaxed = true)
        viewModel.setPintarLetra(editable, 0, 0)
        verify { setPintarLetraUseCase(editable, 0, 0) }
    }

    @Test
    fun `toggleShowMessageMoreQuestions cambia el valor`() {
        // Act & Assert
        // Valor inicial (llama al getter)
        assertTrue(viewModel.showMessageMoreQuestions)

        // Cambiamos el estado (llama al setter interno)
        viewModel.toggleShowMessageMoreQuestions()

        // Verificamos que el getter refleja el cambio
        assertFalse(viewModel.showMessageMoreQuestions)

        // Lo volvemos a cambiar
        viewModel.toggleShowMessageMoreQuestions()

        // Otra vez debería estar en true
        assertTrue(viewModel.showMessageMoreQuestions)
    }

    @Test
    fun `actualizacion de eventos MutableLiveData y contadorPregunta - cobertura completa`() =
        runTest {
            val expected = ValidacionesGuiaModel()
            val observer = mockk<Observer<ValidacionesGuiaModel>>(relaxed = true)
            val editable: Editable = SpannableStringBuilder("Texto prueba")

            // --- Roll ---
            every {
                setRollClickedUseCase(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns expected
            testLiveDataAction(observer, viewModel.uiStateBtnRoll, {
                viewModel.clickedRoll(editable, isEtPregunta = true, ruta = "rutaPrueba")
                expected
            }, expected)

            // --- Next con incremento de contadorPregunta ---
            viewModel.setContadorPreguntaTest(0)
            every {
                setClickSiguienteModicandoUseCase(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns mockk {
                every { estadoUI } returns mockk {
                    every { isUpdatedAskAns } returns true
                    every { isThereMoreAsks } returns true
                }
            }
            testLiveDataAction(observer, viewModel.uiStateBtnNext, {
                viewModel.onClickImgvNext(editable, isEtPregunta = true, ruta = "rutaPrueba")
                expected
            }, expected)
            assertEquals(1, viewModel.contadorPregunta) // incremento ejecutado

            // --- Next sin incremento (isUpdatedAskAns = false) ---
            viewModel.setContadorPreguntaTest(1)
            every {
                setClickSiguienteModicandoUseCase(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns mockk {
                every { estadoUI } returns mockk {
                    every { isUpdatedAskAns } returns false
                    every { isThereMoreAsks } returns true
                }
            }
            viewModel.onClickImgvNext(editable, isEtPregunta = true, ruta = "rutaPrueba")
            assertEquals(1, viewModel.contadorPregunta) // contador no cambia

            // --- Eliminar con decremento de contadorPregunta ---
            viewModel.setContadorPreguntaTest(1)
            every { setClickEliminarUseCase(any(), any(), any(), any()) } returns expected
            testLiveDataAction(observer, viewModel.uiStateBtnEliminar, {
                viewModel.onClickEliminar("rutaPrueba")
                expected
            }, expected)
            assertEquals(0, viewModel.contadorPregunta) // decremento ejecutado

            // --- Eliminar sin decremento (contadorPregunta <= 0) ---
            viewModel.setContadorPreguntaTest(0)
            viewModel.onClickEliminar("rutaPrueba")
            assertEquals(0, viewModel.contadorPregunta) // no decrementa

            // --- Save ---
            every {
                setClickSaveUseCase(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns expected
            testLiveDataAction(observer, viewModel.uiStateBtnSave, {
                viewModel.onClickImgvSave(
                    editable,
                    nombreArchivo = "archivo.xml",
                    isEtPregunta = true,
                    didTheGuideAlreadyExist = false,
                    ruta = "rutaPrueba"
                )
                expected
            }, expected)
        }

    private fun <T> testLiveDataAction(
        observer: Observer<T>,
        liveData: LiveData<T>,
        action: () -> T,
        expected: T
    ) {
        liveData.observeForever(observer)
        action()
        verify { observer.onChanged(expected) }
        liveData.removeObserver(observer)
    }
}