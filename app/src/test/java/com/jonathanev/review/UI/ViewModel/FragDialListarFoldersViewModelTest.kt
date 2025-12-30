//@RunWith(RobolectricTestRunner::class)
class FragDialListarFoldersViewModelTest {
/*
    // Necesario para que LiveData ejecute sincrónicamente
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FragDialListarFoldersViewModel
    private val guiaRepositoryImpl: GuiaRepositoryImpl = mockk()
    private val guiaProvider: GuiaProvider = mockk()
    private val getFolderPosicionUseCase: GetFolderPosicionUseCase = mockk()
    private val getAllFoldersUseCase: GetAllFoldersUseCase = mockk()
    private val getFoldersWithNumGuidesUseCase: GetFoldersWithNumGuidesUseCase = mockk()
    private val filePathsProvider: FilePathsProvider = mockk()
    private val fileRepository: FileRepository = mockk()
    private val getFoldersCreatedUseCase: GetFoldersCreatedUseCase = mockk()
    private val deleteContentGuidesUseCase: DeleteContentGuidesUseCase = mockk()
    private val fileHelperImpl: FileHelperImpl = mockk()

    @Before
    fun setup() {
        viewModel = FragDialListarFoldersViewModel(
            guiaRepositoryImpl,
            guiaProvider,
            getFolderPosicionUseCase,
            getAllFoldersUseCase,
            filePathsProvider,
            fileRepository,
            getFoldersCreatedUseCase,
            getFoldersWithNumGuidesUseCase,
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
    }*/
}
