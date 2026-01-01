package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.jonathanev.review.domain.ApplyColorRangesToQAUseCase
import com.jonathanev.review.presentation.state.UIPreviewQuestionState
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.model.TypeContent
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.domain.GetPreviewQuestionsUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.domain.model.QAItemDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragmentPreviewQuestionsViewModel @Inject constructor(
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getPreviewQuestionsUseCase: GetPreviewQuestionsUseCase,
    private val filePathsProvider: FilePathsProvider,
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val pathProvider: PathProvider,
    private val applyColorRangesToQAUseCase: ApplyColorRangesToQAUseCase
): ViewModel() {
    private var _preguntas: MutableList<QuestionItemUi> = mutableListOf()
    val preguntas: List<QuestionItemUi> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemUi> = mutableListOf()
    val respuestas: List<QuestionItemUi> get() = _respuestas

    private val _uiState = MutableStateFlow(UIPreviewQuestionState(emptyList()))
    val uiState = _uiState.asStateFlow()

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var typeContent = TypeContent.QUESTION

    fun setMainPath(){
        val currentPath = pathProvider.getCurrentPath()
        val beforePath = filePathsProvider.beforePath(File(currentPath))
        pathProvider.setCurrentPath(beforePath.path)

        getGuidesBefore()
    }

    private fun getGuidesBefore() {
        loadGuidesUseCase.invoke()
    }

    fun getObtenerDatosXML() {
        if (respuestas.isEmpty()) {
            //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
            val datos = getObtenerDatosXMLUseCase.invoke()
            val datosProcesados = applyColorRangesToQAUseCase.invoke(datos)

            val questionsDomain =
                datosProcesados.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }

            val answersDomain =
                datosProcesados.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }

            _preguntas = questionsDomain.map { it.toUi() }.toMutableList()
            _respuestas = answersDomain.map { it.toUi() }.toMutableList()
            uploadQuestion(datosProcesados)
        }
    }

    private fun uploadQuestion(
        domainItems: List<QAItemDomain>,
    ) {
        val response = getPreviewQuestionsUseCase.invoke(domainItems)
        val responseToUi = response.map { it.toUi() }

        _uiState.value = UIPreviewQuestionState(
            previewState = responseToUi
        )
    }

    private fun getCurrentPath() = pathProvider.getCurrentPath()
}