package com.jonathanev.review.UI.ViewModel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.InternalRules
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.GetGuiaUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetQuestionContentsUseCase
import com.jonathanev.review.Domain.SetPintarTextosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActivityRepasarGuiaViewModel @Inject constructor(
    private val setPintarTextosUseCase: SetPintarTextosUseCase,
    private val getGuiaUseCase: GetGuiaUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    //private val getClickRegresarUseCase: GetClickRegresarUseCase,
    private val getQuestionContentsUseCase: GetQuestionContentsUseCase,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val guiaProvider: GuiaProvider
) : ViewModel() {
    private lateinit var _preguntas: MutableList<QuestionItem>
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private lateinit var _respuestas: MutableList<QuestionItem>
    val respuestas: MutableList<QuestionItem> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var _guiaModel = MutableLiveData<GuiaModel>()
    val guiaModel: MutableLiveData<GuiaModel> get() = _guiaModel

    private var typeContent = TypeContent.QUESTION

    /*private val _uiStateBtnBack = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnBack: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnBack
    private val _uiStateBtnNext = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnNext: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnNext
    private val _uiStateBtnRoll = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnRoll: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnRoll*/

    //private var _currentPath = MutableStateFlow(getCurrentPathUseCase())
    // El StateFlow del VM ahora es una simple copia del Flow del Repositorio.
    // Usamos 'StateFlow' del repositorio para el estado de la UI del VM.
    val currentPath: StateFlow<String> = fileRepositoryImpl.currentPathFlow
        .stateIn(
            scope = viewModelScope,
            // WhileSubscribed asegura que solo se recolecte cuando la UI esté activa.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = fileRepositoryImpl.getCurrentPath() // Valor inicial de la ruta
        )

    private val _uiState = MutableStateFlow(EstadoUI())
    val uiState = _uiState.asStateFlow()

    fun getGuia() {
        _guiaModel.postValue(getGuiaUseCase(ruta = fileRepositoryImpl.getCurrentPath()))
    }

    fun getObtenerDatosXML() {
        if (respuestas.isEmpty()) {
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas = datos.preguntas
            _respuestas = datos.respuestas
        }

        cargarPregunta(typeContent)
    }

    fun onClickRoll() {
        typeContent =
            if (typeContent == TypeContent.QUESTION) TypeContent.ANSWER else TypeContent.QUESTION

        cargarPregunta(typeContent)
    }

    fun getReinicioGuia() {
        onResetContadorPreg()
        typeContent = TypeContent.QUESTION

        cargarPregunta(typeContent)
    }

    fun onClickNext() {
        val posPregFin = preguntas.size - 1
        val contador = contadorPregunta + 1

        if (contador <= posPregFin) {
            _contadorPregunta++
            typeContent = TypeContent.QUESTION

            cargarPregunta(typeContent = typeContent, shouldFlip = true)
        } else {
            _uiState.value = EstadoUI(
                message = "Se acabaron las preguntas, ¿Quieres repetir la guia?",
                internalRules = InternalRules(
                    isThereMoreAsks = false
                )
            )
        }
    }

    fun onClickBefore() {
        val contador = contadorPregunta - 1

        if (contador >= 0) {
            _contadorPregunta--
            typeContent = TypeContent.QUESTION

            cargarPregunta(typeContent)
        } else {
            _uiState.value = EstadoUI(
                message = "Ya no tienes preguntas anteriores",
                internalRules = InternalRules(
                    isThereMoreAsks = false
                )
            )
        }
    }

    fun onResetContadorPreg() {
        _contadorPregunta = 0
    }

    fun getCurrentPath() = fileRepositoryImpl.getCurrentPath()

    private fun cargarPregunta(typeContent: TypeContent, shouldFlip: Boolean = false) {
        val contentList = getQuestionContentsUseCase(
            if (typeContent == TypeContent.QUESTION) preguntas else respuestas,
            contadorPregunta
        )

        contentList.forEach { item ->
            when (val result = setPintarTextosUseCase(item, getCurrentPath())) {

                is QuestionContent.Image -> {
                    _uiState.value = EstadoUI(
                        shouldFlip = shouldFlip,
                        internalRules = InternalRules(isShowCancelar = true),
                        content = result
                    )
                }

                is QuestionContent.Text -> {
                    _uiState.value = EstadoUI(
                        shouldFlip = shouldFlip,
                        internalRules = InternalRules(
                            isShowQuitColor = true,
                            isShowSelColor = true
                        ),
                        content = result
                    )
                }

                QuestionContent.None -> _uiState.value = EstadoUI()
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun setRespuestas(value: MutableList<QuestionItem>) {
        _respuestas = value
    }

    fun prueba(): List<GuiaModel> {
        return guiaProvider.guias
    }
}