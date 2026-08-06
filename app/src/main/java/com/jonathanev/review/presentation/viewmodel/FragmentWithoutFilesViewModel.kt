package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetGuideMoveUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.MoveGuideUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.MoveGuideResponse
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.presentation.state.GuidesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragmentWithoutFilesViewModel @Inject constructor(
    private val moveGuideUseCase: MoveGuideUseCase,
    private val getGuideMoveUseCase: GetGuideMoveUseCase,
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val resetNavigationUseCase: ResetNavigationUseCase,
    private val loadGuidesUseCase: LoadGuidesUseCase
) : ViewModel() {
    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    val uiState: StateFlow<GuidesUiState> = loadGuidesUseCase.invoke()
        .map { list ->
            if (list.isEmpty()) GuidesUiState.Empty
            else GuidesUiState.Success(list.map { it.toUi() })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = GuidesUiState.Loading
        )

    private var _preguntas: MutableList<QuestionItemUi> = mutableListOf()
    val preguntas: List<QuestionItemUi> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemUi> = mutableListOf()
    val respuestas: List<QuestionItemUi> get() = _respuestas

    private fun eventMovingFile(message: String) {
        viewModelScope.launch {
            _eventsMovingFiles.emit(UIMovingEvent.ShowMessage(message))
        }
    }

    fun moveFileCancel() {
        eventMovingFile("Se ha cancelado la acción")
    }

    fun movingGuide() {
        viewModelScope.launch {
            when (val context = getGuideMoveUseCase.invoke()) {
                is GuideContext.Moving -> {
                    when (val guideData = getGuideXmlDataUseCase.invoke(context)) {
                        is GetGuideResult.Success -> {
                            val response = moveGuideUseCase.invoke(guideData, context)
                            when (response) {
                                MoveGuideResponse.ErrorMovingGuide ->
                                    eventMovingFile("Error al intentar mover la guia")

                                MoveGuideResponse.ErrorMovingImages ->
                                    eventMovingFile("Error al intentar mover imagenes")

                                MoveGuideResponse.ErrorPathGuide ->
                                    eventMovingFile("No existe la ruta para mover la guia")

                                MoveGuideResponse.ErrorPathImages ->
                                    eventMovingFile("No existe una ruta para guardar las imagenes")

                                MoveGuideResponse.Success ->
                                    eventMovingFile("Guia movida exitosamente")
                            }
                        }

                        GetGuideResult.InvalidFormat -> eventMovingFile("La guia está dañada")

                        GetGuideResult.NotFound -> eventMovingFile("No se ha encontrado la guia")

                        GetGuideResult.UnknownError -> eventMovingFile("Error desconocido")
                    }
                }

                else -> eventMovingFile("Error inesperado")
            }
        }
    }

    fun initRelativeGuide() {
        viewModelScope.launch {
            resetNavigationUseCase.invoke()
        }
    }
}