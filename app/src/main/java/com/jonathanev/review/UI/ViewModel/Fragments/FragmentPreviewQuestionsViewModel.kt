package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.GuiaRepositoryImpl
import com.jonathanev.review.Data.Model.StateUIPreviewQuestion
import com.jonathanev.review.Data.Model.prueba.AnswerState
import com.jonathanev.review.Data.Model.prueba.QAItem
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetPreviewQuestionsUseCase
import com.jonathanev.review.Domain.LoadGuidesUseCase
import com.jonathanev.review.Domain.repository.FileRepository
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
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas = datos.map { it.question }.toMutableList()
            _respuestas = datos.mapNotNull { (it.answer as? AnswerState.Filled )?.item }.toMutableList()

            uploadQuestion(datos)
        }
    }

    private fun getCurrentPath() = fileRepository.getCurrentPath()

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