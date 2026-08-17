package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.ClearContextUseCase
import com.jonathanev.review.domain.GetGuideContextUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.MoveGuideUseCase
import com.jonathanev.review.domain.ObservePathUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.MoveGuideResponse
import com.jonathanev.review.presentation.event.StateGuideActionEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.state.GuidesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class FragmentWithoutFilesViewModel @Inject constructor(
    private val moveGuideUseCase: MoveGuideUseCase,
    private val getGuideContextUseCase: GetGuideContextUseCase,
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    loadGuidesUseCase: LoadGuidesUseCase,
    private val clearContextUseCase: ClearContextUseCase,
    private val resetNavigationUseCase: ResetNavigationUseCase,
    observePathUseCase: ObservePathUseCase
) : ViewModel() {
    private val _stateGuideActionEvent = MutableSharedFlow<StateGuideActionEvent>()
    val stateGuideActionEvent = _stateGuideActionEvent.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GuidesUiState> = observePathUseCase.invoke()
        .flatMapLatest { _ ->
            loadGuidesUseCase.invoke()
        }
        .map { list ->
            if (list.isEmpty()) GuidesUiState.Empty
            else GuidesUiState.Success(list.map { it.toUi() })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = GuidesUiState.Loading
        )

    val interactionMode: StateFlow<FileInteractionMode> = getGuideContextUseCase()
        .map { activeMoving ->
            if (activeMoving is GuideContext.Moving) FileInteractionMode.MovingItem else FileInteractionMode.Default
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FileInteractionMode.Default
        )

    fun onCancelMove() {
        viewModelScope.launch {
            try {
                clearContextUseCase.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                eventMovingFile(e.message ?: "Ocurrió un error inesperado")
            }
        }
    }

    private fun eventMovingFile(message: String) {
        viewModelScope.launch {
            _stateGuideActionEvent.emit(StateGuideActionEvent.ShowMessage(message))
        }
    }

    fun moveFileCancel() {
        onCancelMove()
        eventMovingFile("Se ha cancelado la acción")
    }

    fun movingGuide() {
        viewModelScope.launch {
            try {
                when (val context = getGuideContextUseCase.invoke().firstOrNull()) {
                    is GuideContext.Moving -> {
                        when (val guideData = getGuideXmlDataUseCase.invoke(context)) {
                            is GetGuideResult.Success -> {
                                when (moveGuideUseCase.invoke(guideData, context)) {
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
            } finally {
                clearContextUseCase.invoke()
            }
        }
    }

    fun resetNavigationPath() {
        viewModelScope.launch {
            try {
                resetNavigationUseCase.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                eventMovingFile(e.message ?: "Ocurrió un error inesperado")
            }
        }
    }
}