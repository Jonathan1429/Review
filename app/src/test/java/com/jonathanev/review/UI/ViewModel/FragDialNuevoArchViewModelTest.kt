import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.UI.ViewModel.Fragments.FragDialNuevoArchViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class FragDialNuevoArchViewModelTest {

    // Esta regla hace que todos los cambios en LiveData se ejecuten de inmediato en el hilo del test.
    // Sin esto, LiveData podría usar hilos internos y el test fallaría porque value no se actualiza a tiempo.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Instancia del ViewModel que vamos a probar
    private lateinit var viewModel: FragDialNuevoArchViewModel

    // Mock del repositorio, para no depender de archivos ni datos reales
    private val guiaRepository: GuiaRepository = mockk()

    @Before
    fun setup() {
        // Creamos el ViewModel inyectando el mock del repositorio
        viewModel = FragDialNuevoArchViewModel(guiaRepository)
    }

    @Test
    fun `getAllUpdatedGuides should update guias LiveData with repository data`() {
        // ---- Arrange ----
        // Creamos un archivo dummy para pasar al método
        val file = File("dummy")

        // Lista simulada de guías que el repositorio "devolverá"
        val mockList = listOf(
            GuiaModel("Guia 1", 1),
            GuiaModel("Guia 2", 1)
        )

        // Configuramos el comportamiento del mock: cuando se llame getGuias(file), devuelve mockList
        every { guiaRepository.getGuias(file) } returns mockList

        // Creamos un observer mock para verificar que LiveData notifique los cambios
        val observer = mockk<Observer<List<GuiaModel>>>(relaxed = true)

        // Observamos la LiveData para poder capturar cambios
        viewModel.guias.observeForever(observer)

        // ---- Act ----
        // Llamamos al método que estamos probando
        viewModel.getAllUpdatedGuides(file)

        // ---- Assert ----
        // Verificamos que el LiveData se actualizó con la lista esperada
        assertEquals(mockList, viewModel.guias.value)

        // Verificamos que el observer recibió la notificación con la lista
        verify { observer.onChanged(mockList) }

        // Verificamos que el repositorio fue llamado con el archivo correcto
        verify { guiaRepository.getGuias(file) }
    }
}
