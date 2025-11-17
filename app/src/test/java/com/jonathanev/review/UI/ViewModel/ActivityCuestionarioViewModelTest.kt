import android.text.Editable
import android.text.SpannableStringBuilder
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.*
import com.jonathanev.review.UI.ViewModel.ActivityCuestionarioViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.ArrayList

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityCuestionarioViewModelTest {

    /*// Ejecuta LiveData de inmediato
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ActivityCuestionarioViewModel
    private val dataStore: DataStoreManager = mockk(relaxed = true)

    // Dependencias mockeadas
    private val testDispatcher = UnconfinedTestDispatcher()
    private val guiaRepository: GuiaRepository = mockk()
    private val setClickRegresarModificandoUseCase: SetClickRegresarModificandoUseCase = mockk(relaxed = true)
    private val setClickSiguienteModificandoUseCase: SetClickSiguienteModificandoUseCase = mockk()
    private val setRollClickedUseCase: SetRollClickedUseCase = mockk()
    private val setClickSaveUseCase: SetClickSaveUseCase = mockk()
    private val deleteCurrentQuestionUseCase: DeleteCurrentQuestionUseCase = mockk()
    private val setCopyImagesUseCase: SetCopyImagesUseCase = mockk()
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase = mockk()
    private val setPintarLetraUseCase: SetPintarLetraUseCase = mockk()
    private val deleteContentInPivUseCase: DeleteContentInPivUseCase = mockk()
    private val filePathsProvider: FilePathsProvider = mockk()
    private val fileRepositoryImpl: FileRepositoryImpl = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Configurar el mock de dataStore
        coEvery { dataStore.getCountImage() } returns flowOf(0)
        coEvery { dataStore.setIncrementCounter() } just Runs
        coEvery { dataStore.resetCounter() } just Runs

        viewModel = ActivityCuestionarioViewModel(
            guiaRepository,
            setClickRegresarModificandoUseCase,
            setClickSiguienteModificandoUseCase,
            setRollClickedUseCase,
            setClickSaveUseCase,
            deleteCurrentQuestionUseCase,
            setCopyImagesUseCase,
            setCifrarRutaImagenUseCase,
            setPintarLetraUseCase,
            deleteContentInPivUseCase,
            filePathsProvider,
            fileRepositoryImpl,
            dataStore,
            ioDispatcher = testDispatcher,
            mainDispatcher = testDispatcher
            //application
        )
    }

    @Test
    fun `procesoActualizacion should call repository and copyImages`() {
        val guiasMock = emptyList<GuiaModel>()

        // Arrange
        every { guiaRepository.getGuias(any()) } returns guiasMock
        every { setCopyImagesUseCase.invoke() } just Runs

        // Act
        val observer = mockk<Observer<List<GuiaModel>>>(relaxed = true)
        viewModel.guias.observeForever(observer)

        viewModel.procesoActualizacion()

        // Assert
        verify { guiaRepository.getGuias(any()) }
        verify { observer.onChanged(guiasMock)}
        verify { setCopyImagesUseCase.invoke() }
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

    @Test
    fun `test llamaCorruIncremento sets LiveData, calls increment and reset counter`() = runTest {
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

        // Act
        viewModel.resetCounter()

        // Assert
        coVerify(exactly = 1) { dataStore.resetCounter() }

        // Cleanup
        viewModel.textoImagenCorrutina.removeObserver(observer)
    }

    @Test
    fun `onClickImgvNext should update LiveData and increment contador`() {
        // Arrange
        val editable: Editable = mockk()
        val response = mockk<ValidacionesGuiaModel> {
            every { estadoUI.isUpdatedAskAns } returns true
        }
        every {
            setClickSiguienteModificandoUseCase(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns response

        val observer = mockk<Observer<ValidacionesGuiaModel>>(relaxed = true)
        viewModel.uiStateBtnNext.observeForever(observer)

        // Act
        viewModel.onClickImgvNext(editable, true, "ruta")

        // Assert
        verify { observer.onChanged(response) }
    }

    @Test
    fun `getUrlImagenCifrada should delegate to useCase`() {
        // Arrange
        every { setCifrarRutaImagenUseCase("url", 1) } returns "cifrada"

        // Act
        val result = viewModel.getUrlImagenCifrada("url", 1)

        // Assert
        assert(result == "cifrada")
    }

    @Test
    fun `setPintarLetra should call useCase`() {
        // Arrange
        val editable: Editable = mockk()
        every { setPintarLetraUseCase(editable, 0, 123) } just Runs

        // Act
        viewModel.setPintarLetra(editable, 0, 123)

        // Assert
        verify { setPintarLetraUseCase(editable, 0, 123) }
    }

    /*@Test
    fun `llamaCorruIncremento should call setIncrementCounter`() = runTest {
        // Como setIncrementCounter llama a dataStore, aquí podemos usar un mock
        // o dejarlo vacío si no quieres probar integración real.
    }*/

    @Test
    fun `Acciones una vez entrando al evento  onClickImgvPrevious`() =
        runTest {
            var expected = ValidacionesGuiaModel()
            var response = ValidacionesGuiaModel()
            val observer = mockk<Observer<ValidacionesGuiaModel>>(relaxed = true)
            val editable: Editable = SpannableStringBuilder("Texto prueba")
            viewModel.setContadorPreguntaTest(0)
            every {
                setClickRegresarModificandoUseCase(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns response
            response = setClickRegresarModificandoUseCase(ArrayList(), ArrayList(), 0, SpannableStringBuilder(""), true, "")
            assertEquals(expected, response)

            // Se simula un cambio al LiveData
            testLiveDataAction(observer, viewModel.uiStateBtnBack, {
                viewModel.onClickImgvPrevious(editable, isEtPregunta = true, ruta = "rutaPrueba")
                expected
            }, expected)
            assertEquals(0, viewModel.contadorPregunta) // incremento ejecutado

            // Respuesta cuando hay más preguntas y se actualiza la última.
            expected = ValidacionesGuiaModel(estadoUI = EstadoUI(isUpdatedAskAns = true))
            every {
                setClickRegresarModificandoUseCase(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns ValidacionesGuiaModel(
                estadoUI = EstadoUI(isUpdatedAskAns = true)
            )
            response = setClickRegresarModificandoUseCase(ArrayList(), ArrayList(), 0, SpannableStringBuilder(""), true, "")
            assertEquals(expected, response)
            // Se simula un cambio al LiveData
            testLiveDataAction(observer, viewModel.uiStateBtnBack, {
                viewModel.onClickImgvPrevious(editable, isEtPregunta = true, ruta = "rutaPrueba")
                expected
            }, expected)
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
                setClickSiguienteModificandoUseCase(
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
                setClickSiguienteModificandoUseCase(
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

            // back sin decremento
            viewModel.onClickImgvNext(editable, isEtPregunta = true, ruta = "rutaPrueba")
            assertEquals(1, viewModel.contadorPregunta) // contador no cambia

            // --- Eliminar con decremento de contadorPregunta ---
            viewModel.setContadorPreguntaTest(1)
            every { deleteCurrentQuestionUseCase(any(), any(), any(), any()) } returns expected
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
                    //didTheGuideAlreadyExist = false,
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
    }*/
}
