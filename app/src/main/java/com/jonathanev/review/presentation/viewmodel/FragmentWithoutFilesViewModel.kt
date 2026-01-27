package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetGuideMoveUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.MoveGuideUseCase
import com.jonathanev.review.domain.SetMainPathUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.MoveGuideResponse
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.domain.repository.NavigationPathRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragmentWithoutFilesViewModel @Inject constructor(
    private val moveGuideUseCase: MoveGuideUseCase,
    private val navigationPathRepository: NavigationPathRepository,
    private val setMainPathUseCase: SetMainPathUseCase,
    private val getGuideMoveUseCase: GetGuideMoveUseCase,
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase
) : ViewModel() {
    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    private var _preguntas: MutableList<QuestionItemUi> = mutableListOf()
    val preguntas: List<QuestionItemUi> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemUi> = mutableListOf()
    val respuestas: List<QuestionItemUi> get() = _respuestas

    fun back() {
        navigationPathRepository.back()
    }

    fun setMainPath() {
        setMainPathUseCase.invoke()
    }

    private fun eventMovingFile(message: String) {
        viewModelScope.launch {
            _eventsMovingFiles.emit(UIMovingEvent.ShowMessage(message))
        }
    }

    fun moveFileCancel() {
        eventMovingFile("Se ha cancelado la acción")
    }

    fun movingGuide() {
        when (val context = getGuideMoveUseCase.invoke()) {
            is GuideContext.Moving -> {
                when (val guideData = getGuideXmlDataUseCase.invoke(context.guide)) {
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

                            MoveGuideResponse.WarningDeleteFolder ->
                                eventMovingFile("Hubo inconveniente en el paso de todos los archivos")
                        }
                    }

                    GetGuideResult.Error -> eventMovingFile("Ocurrió un error al abrir la guia")

                    GetGuideResult.InvalidFormat -> eventMovingFile("La guia está dañada")

                    GetGuideResult.NotFound -> eventMovingFile("No se ha encontrado la guia")

                    GetGuideResult.UnknownError -> eventMovingFile("Error desconocido")
                }
            }

            else -> eventMovingFile("Error inesperado")
        }

        return
    }
}