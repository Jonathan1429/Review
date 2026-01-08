package com.jonathanev.review.domain

class UpdateImagesUseCaseTest {
    /*private val pathProvider = mockk<PathProvider>()
    private val directoryManager = mockk<DirectoryManager>(relaxed = true)
    private val saveGuideImagesUseCase = mockk<SaveGuideImagesUseCase>(relaxed = true)
    private val getVersionUseCase = mockk<GetVersionUseCase>()

    private lateinit var useCase: UpdateImagesUseCase
    private val imagesFolder = File("fake/path")

    @Before
    fun setup() {
        every { pathProvider.buildTempPathFile(any()) } returns imagesFolder

        useCase = UpdateImagesUseCase(
            pathProvider,
            directoryManager,
            saveGuideImagesUseCase,
            getVersionUseCase
        )
    }

    @Test
    fun `when new file and images exist then images are saved`() = runTest {
        every { getVersionUseCase.invoke(nameGuide) } returns Versions.VERSION2

        val image = mockk<QuestionContentDomain.Image>()
        val question = mockk<QuestionItemDomain> {
            every { content } returns listOf(image)
        }

        useCase(
            nameGuide = "guia",
            preguntasProcesadas = listOf(question) ,
            respuestasProcesadas = emptyList(),
            isNewFile = true
        )

        coVerify {
            saveGuideImagesUseCase.saveImagesInDevice(listOf(image), imagesFolder)
        }

        verify {
            directoryManager.prepareCleanDirectory(imagesFolder.path, true)
            directoryManager.deleteLeftoverImagesInDevice("guia", listOf(image))
        }
    }

    @Test
    fun `when new file and no images then images are not saved`() = runTest {
        every { getVersionUseCase.invoke(nameGuide) } returns Versions.VERSION2

        useCase(
            nameGuide = "guia",
            preguntasProcesadas = emptyList(),
            respuestasProcesadas = emptyList(),
            isNewFile = true
        )

        coVerify(exactly = 0) {
            saveGuideImagesUseCase.saveImagesInDevice(any(), any())
        }
    }

    @Test
    fun `when existing file and version is V1 then images are moved`() = runTest {
        every { getVersionUseCase.invoke(nameGuide) } returns Versions.VERSION1

        val image = mockk<QuestionContentDomain.Image>()
        val question = mockk<QuestionItemDomain> {
            every { content } returns listOf(image)
        }

        useCase(
            nameGuide = "guia",
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = false
        )

        verify {
            directoryManager.moveImagesV1(
                listImages = listOf(image),
                nameGuide = "guia"
            )
        }
    }

    @Test
    fun `when existing file and version is not V1 then images are not moved`() = runTest {
        every { getVersionUseCase.invoke(nameGuide) } returns Versions.VERSION2

        useCase(
            preguntasProcesadas = emptyList(),
            respuestasProcesadas = emptyList(),
            isNewFile = false
        )

        verify(exactly = 0) {
            directoryManager.moveImagesV1(any(), any())
        }
    }*/
}