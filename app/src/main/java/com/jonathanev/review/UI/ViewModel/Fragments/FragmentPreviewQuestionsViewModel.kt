package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.ViewModel
import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.presentation.state.StateUIPreviewQuestion
import com.jonathanev.review.presentation.state.ResponseDomain
import com.jonathanev.review.presentation.model.QuestionItemDomain
import com.jonathanev.review.Domain.model.TypeContent
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetPreviewQuestionsUseCase
import com.jonathanev.review.Domain.LoadGuidesUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.UI.Utils.toUi
import com.jonathanev.review.presentation.state.QAItemDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragmentPreviewQuestionsViewModel @Inject constructor(
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getPreviewQuestionsUseCase: GetPreviewQuestionsUseCase,
    //private val fileRepositoryImpl: FileRepositoryImpl,
    private val filePathsProvider: FilePathsProvider,
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val guiaRepository: GuiaRepository,
    private val fileRepository: FileRepository
    //private val guiaRepositoryImpl: GuiaRepositoryImpl
): ViewModel() {
    private var _preguntas: MutableList<QuestionItemDomain> = mutableListOf()
    val preguntas: List<QuestionItemDomain> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemDomain> = mutableListOf()
    val respuestas: List<QuestionItemDomain> get() = _respuestas

    private val _uiState = MutableStateFlow(StateUIPreviewQuestion(emptyList()))
    val uiState = _uiState.asStateFlow()

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var typeContent = TypeContent.QUESTION

    fun setMainPath(){
        val currentPath = fileRepository.getCurrentPath()
        val beforePath = filePathsProvider.beforePath(File(currentPath))
        fileRepository.setCurrentPath(beforePath.path)

        getGuidesBefore()
    }

    private fun getGuidesBefore() {
        loadGuidesUseCase.invoke()
        //guiaRepository.getGuides()
    }

    fun getObtenerDatosXML() {
        if (respuestas.isEmpty()) {
            val domainItems = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas = domainItems.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }.toMutableList()
            _respuestas = domainItems.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toMutableList()

            uploadQuestion(domainItems)
        }
    }

    private fun uploadQuestion(
        domainItems: List<QAItemDomain>,
    ) {
        val response = getPreviewQuestionsUseCase.invoke(domainItems)
        val responseToUi = response.map { it.toUi() }

        _uiState.value = StateUIPreviewQuestion(
            previewState = responseToUi
        )
    }

    private fun getCurrentPath() = fileRepository.getCurrentPath()
}