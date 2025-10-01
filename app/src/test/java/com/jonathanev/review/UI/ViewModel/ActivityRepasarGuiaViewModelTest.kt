package com.jonathanev.review.UI.ViewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.gms.common.internal.Asserts
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.PreguntaRespuestaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Domain.GetClickRegresarUseCase
import com.jonathanev.review.Domain.GetClickSiguienteUseCase
import com.jonathanev.review.Domain.GetGuiaUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.SetPintarTextosUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActivityRepasarGuiaViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ActivityRepasarGuiaViewModel

    // Dependencias mockeadas, se mockean así ya que no necesito exactamente un resultado
    private val setPintarTextosUseCase: SetPintarTextosUseCase = mockk()
    private val getGuiaUseCase: GetGuiaUseCase = mockk()
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase = mockk()
    private val getClickRegresarUseCase: GetClickRegresarUseCase = mockk()
    private val getClickSiguienteUseCase: GetClickSiguienteUseCase = mockk()

    @Before
    fun setup() {
        viewModel = ActivityRepasarGuiaViewModel(
            setPintarTextosUseCase,
            getGuiaUseCase,
            getObtenerDatosXMLUseCase,
            getClickRegresarUseCase,
            getClickSiguienteUseCase
        )
    }

    @Test
    fun `obtener la guia actual`() {
        val guiaMock = GuiaModel("Guia", 0)

        every { getGuiaUseCase.invoke(any()) } returns guiaMock

        val observer = mockk<Observer<GuiaModel>>(relaxed = true)
        viewModel.guiaModel.observeForever(observer)

        viewModel.getGuia("guia")

        verify { getGuiaUseCase.invoke(any()) }
        verify { observer.onChanged(guiaMock) }
    }

    // getObtenerDatosXML
    @Test
    fun `obtener los datos de la guia a objetos`() {
        // Inicialmente entra al if
        val modelPregResp: List<PreguntaRespuestaModel> =
            listOf(
                PreguntaRespuestaModel("p", "r"),
                PreguntaRespuestaModel("pre", "res")
            )
        val valGuiaModel = ValidacionesGuiaModel()

        every { getObtenerDatosXMLUseCase(any(), any()) } returns modelPregResp
        every { setPintarTextosUseCase(any(), any(), any(), any(), any()) } returns valGuiaModel

        viewModel.getObtenerDatosXML("nombre", "ruta")

        verify { getObtenerDatosXMLUseCase.invoke(any(), any()) }
        verify { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) }

        // No entra al if
        val respuestas: ArrayList<String> = ArrayList()
        respuestas.add("prueba")

        viewModel.setRespuestas(respuestas)
        viewModel.getObtenerDatosXML("nombre", "ruta")

        every { setPintarTextosUseCase(any(), any(), any(), any(), any()) } returns valGuiaModel

        verify { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) }
    }

   // OnClickRoll
   @Test
   fun `evento para pasar a pregunta-respuesta`(){
       val valGuiaModel = ValidacionesGuiaModel()
       val observer = mockk<Observer<ValidacionesGuiaModel>>(relaxed = true)
       val expected = ValidacionesGuiaModel()

       every { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) } returns valGuiaModel

       // Se simula un cambio al LiveData
       testLiveDataAction(observer, viewModel.uiStateBtnRoll, {
           viewModel.onClickRoll(isEtPregunta = true, ruta = "rutaPrueba")
           expected
       }, expected)

       viewModel.onClickRoll(true, "ruta")

       verify { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) }
   }

    // onClickNext
    @Test
    fun `evento para pasar a la siguiente pregunta-respuesta`(){
        val observer = mockk<Observer<ValidacionesGuiaModel>>(relaxed = true)
        val expected = ValidacionesGuiaModel()

        var response = ValidacionesGuiaModel()

        every { getClickSiguienteUseCase.invoke(any(), any(), any(), any()) } returns response

        // Se simula un cambio al LiveData
        testLiveDataAction(observer, viewModel.uiStateBtnNext, {
            viewModel.onClickNext(ruta = "ruta")
            expected
        }, expected)

        viewModel.onClickNext("ruta")

        verify { getClickSiguienteUseCase.invoke(any(), any(), any(), any()) }
        assertFalse(response.estadoUI.isUpdatedAskAns)

        // Simulación en la cual hay mas preguntas
        response = mockk<ValidacionesGuiaModel>{
            every { estadoUI.isUpdatedAskAns } returns true
        }

        every { getClickSiguienteUseCase.invoke(any(), any(), any(), any()) } returns response

        viewModel.onClickNext("ruta")

        verify { getClickSiguienteUseCase.invoke(any(), any(), any(), any()) }
        assertTrue(response.estadoUI.isUpdatedAskAns)
    }

    // onClickBefore
    @Test
    fun `evento para pasar a la anterior pregunta-respuesta`(){
        val observer = mockk<Observer<ValidacionesGuiaModel>>(relaxed = true)
        val expected = ValidacionesGuiaModel()

        var response = ValidacionesGuiaModel()

        every { getClickRegresarUseCase.invoke(any(), any(), any(), any()) } returns response

        // Se simula un cambio al LiveData
        testLiveDataAction(observer, viewModel.uiStateBtnBack, {
            viewModel.onClickBefore(ruta = "ruta")
            expected
        }, expected)

        viewModel.onClickBefore("ruta")

        verify { getClickRegresarUseCase.invoke(any(), any(), any(), any()) }
        assertFalse(response.estadoUI.isUpdatedAskAns)

        // Simulación en la cual hay mas preguntas
        response = mockk<ValidacionesGuiaModel>{
            every { estadoUI.isUpdatedAskAns } returns true
        }

        every { getClickRegresarUseCase.invoke(any(), any(), any(), any()) } returns response

        viewModel.onClickBefore("ruta")

        verify { getClickRegresarUseCase.invoke(any(), any(), any(), any()) }
        assertTrue(response.estadoUI.isUpdatedAskAns)
    }

    // getReinicioGuia
    @Test
    fun `reiniciar la guia`(){
        val valGuiaModel = ValidacionesGuiaModel()
        every { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) } returns valGuiaModel
        viewModel.getReinicioGuia(true, "ruta")

        verify { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) }
    }

    // getReinicioDeContador
    @Test
    fun `reinicio de contador`(){
        viewModel.onResetContadorPreg()

        assertEquals(0, viewModel.contadorPregunta)
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