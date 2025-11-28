package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.StateUIPreviewQuestion
import com.jonathanev.review.Data.Model.prueba.QAItem
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetPreviewQuestionsUseCase
import com.jonathanev.review.Domain.GetQuestionContentsUseCase
import com.jonathanev.review.Domain.SetPintarTextosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragmentPreviewQuestionsViewModel @Inject constructor(
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getPreviewQuestionsUseCase: GetPreviewQuestionsUseCase,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val filePathsProvider: FilePathsProvider,
    private val guiaRepository: GuiaRepository
): ViewModel() {
    private var _preguntas: MutableList<QuestionItem> = mutableListOf()
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private var _respuestas: MutableList<QuestionItem> = mutableListOf()
    val respuestas: MutableList<QuestionItem> get() = _respuestas

    private val _uiState = MutableStateFlow(StateUIPreviewQuestion(emptyList()))
    val uiState = _uiState.asStateFlow()

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var typeContent = TypeContent.QUESTION

    fun setMainPath(){
        val currentPath = fileRepositoryImpl.getCurrentPath()
        val beforePath = filePathsProvider.beforePath(File(currentPath))
        fileRepositoryImpl.setCurrentPath(beforePath.path)

        getGuidesBefore()
    }

    private fun getGuidesBefore() {
        guiaRepository.getGuides()
    }

    fun getObtenerDatosXML() {
        if (respuestas.isEmpty()) {
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas = datos.map { it.question }.toMutableList()
            _respuestas = datos.map { it.answer }.toMutableList()

            uploadQuestion(datos)
        }
    }

    private fun getCurrentPath() = fileRepositoryImpl.getCurrentPath()

    private fun uploadQuestion(qaItems: List<QAItem>) {
        val response = getPreviewQuestionsUseCase.invoke(qaItems)

        _uiState.value = StateUIPreviewQuestion(
            previewState = response
        )
    }

    /*fun getReinicioGuia() {
        onResetContadorPreg()

        cargarPregunta(typeContent)
    }

    private fun onResetContadorPreg() {
        _contadorPregunta = 0
    }*/
}