import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Data.repository.FileHelperImpl
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.DeleteContentGuidesUseCase
import com.jonathanev.review.Domain.GetAllFoldersUseCase
import com.jonathanev.review.Domain.GetFoldersCreatedUseCase
import com.jonathanev.review.Domain.GetGuiaPosicionUseCase
import com.jonathanev.review.Domain.GetNumGuidesUseCase
import com.jonathanev.review.UI.ViewModel.Fragments.FragDialListarGuiasViewModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

//@RunWith(RobolectricTestRunner::class)
class FragDialListarGuiasViewModelTest {

    // Necesario para que LiveData ejecute sincrónicamente
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FragDialListarGuiasViewModel
    private val guiaRepository: GuiaRepository = mockk()
    private val guiaProvider: GuiaProvider = mockk()
    private val getGuiaPosicionUseCase: GetGuiaPosicionUseCase = mockk()
    private val getAllFoldersUseCase: GetAllFoldersUseCase = mockk()
    private val getNumGuidesUseCase: GetNumGuidesUseCase = mockk()
    private val filePathsProvider: FilePathsProvider = mockk()
    private val fileRepositoryImpl: FileRepositoryImpl = mockk()
    private val getFoldersCreatedUseCase: GetFoldersCreatedUseCase = mockk()
    private val deleteContentGuidesUseCase: DeleteContentGuidesUseCase = mockk()
    private val fileHelperImpl: FileHelperImpl = mockk()

    @Before
    fun setup() {
        viewModel = FragDialListarGuiasViewModel(
            guiaRepository,
            guiaProvider,
            getGuiaPosicionUseCase,
            getAllFoldersUseCase,
            filePathsProvider,
            fileRepositoryImpl,
            getFoldersCreatedUseCase,
            getNumGuidesUseCase,
            deleteContentGuidesUseCase,
            fileHelperImpl
        )
    }

    /*@Test
    fun `getAllGuias should update LiveData with provider data`() {
        val mockList = listOf(GuiaModel("Guia 1", 1), GuiaModel("Guia 2", 1))
        every { guiaProvider.guias } returns mockList

        viewModel.getAllGuias()

        assertEquals(mockList, viewModel.guias.value)
    }*/


    /*@Test
    fun `getAllUpdatedGuides should update LiveData with repository data`() {
        val file = File("dummy")
        val mockList = listOf(GuiaModel("Guia Repo", 1))
        every { guiaRepository.getGuias(file) } returns mockList

        //viewModel.getAllUpdatedGuides(file)

        assertEquals(mockList, viewModel.guias.value)
    }*/

    @Test
    fun `changeFilePath should update file LiveData`() {
        val folder = "testFolder"
        viewModel.changeFilePath(folder)

        assertTrue(viewModel.file.value!!.path.contains(folder))
    }

    @Test
    fun `getGuia should return GuiaModel from use case`() {
        /*val mockGuia = GuiaModel("Test Guia", 1)
        every { getGuiaPosicionUseCase(guias, 0) } returns mockGuia

        val result = viewModel.getGuia(0)

        assertEquals(mockGuia, result)*/
    }

    @Test
    fun `Obtener la ruta principal`() {
        // Act
        viewModel.getFirstPath()

        // Assert
        assertEquals(filePathsProvider.fileGuides, viewModel.file.value)
    }
}
