package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.DeleteGuideUseCase
import com.jonathanev.review.domain.GetGuideMoveUseCase
import com.jonathanev.review.domain.GetGuidePosicionUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.GetNavigationUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.MoveGuideUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.domain.SetContextMoveUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.GuideResultDomain
import com.jonathanev.review.domain.result.MoveGuideResponse
import com.jonathanev.review.presentation.event.GuideActionEvent
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUI
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.model.RelativeGuidePath
import com.jonathanev.review.presentation.state.GuidesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragmentListGuidesViewModel @Inject constructor(
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val getGuidePosicionUseCase: GetGuidePosicionUseCase,
    private val deleteGuideUseCase: DeleteGuideUseCase,
    private val setContextMoveUseCase: SetContextMoveUseCase,
    private val getGuideMoveUseCase: GetGuideMoveUseCase,
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val moveGuideUseCase: MoveGuideUseCase,
    private val getNavigationUseCase: GetNavigationUseCase,
    private val resetNavigationUseCase: ResetNavigationUseCase
) : ViewModel() {
    private var cachedGuides: List<GuideDomainModel> = emptyList()
    private var selectedGuideDomain: GuideDomainModel? = null

    val uiState: StateFlow<GuidesUiState> = loadGuidesUseCase.invoke()
        .map { list ->
            if (list.isEmpty()) GuidesUiState.Empty
            else GuidesUiState.Success(list.map { it.toUi() })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GuidesUiState.Loading
        )

    val relativePath: StateFlow<RelativeGuidePath> = getNavigationUseCase.invoke()
        .map { relativeGuidePath ->
            relativeGuidePath.toUI()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RelativeGuidePath("")
        )

    private val _guides = MutableStateFlow<List<GuideUiModel>>(listOf())
    val guides = _guides.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<GuideActionEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow() // usar estos eventos en compose

    fun getGuideSelected(position: Int): GuideResultUi {
        return when (val result = getGuidePosicionUseCase.invoke(position, cachedGuides)) {
            GuideResultDomain.Error -> result.toUi()
            is GuideResultDomain.Success -> {
                selectedGuideDomain = result.guideDomainModel
                result.toUi()
            }
        }
    }

    fun deleteGuide(nameGuide: String) {
        viewModelScope.launch {
            val guideDomainModel = cachedGuides.find { it.nameGuide == nameGuide }
            if (guideDomainModel == null) {
                emitMessage(GuideActionEvent.ShowMessage("No se ha encontrado la guia"))
                return@launch
            }

            val response = deleteGuideUseCase.invoke(guideDomainModel)
            when (response) {
                DeleteGuideResult.DeleteSuccess -> {
                    emitMessage(GuideActionEvent.Success("Guia borrada exitosamente"))
                }

                DeleteGuideResult.ErrorGuide -> emitMessage(GuideActionEvent.ShowMessage("Hubo un error al borrar la guia"))
                DeleteGuideResult.ErrorImage ->
                    emitMessage(GuideActionEvent.ShowMessage("Hubo inconvenientes en el borrado completo de archivos"))

                else -> emitMessage(GuideActionEvent.ShowMessage("Ocurrió un error al eliminar la guia"))
            }
        }
    }

    private fun emitMessage(guideActionEvent: GuideActionEvent) {
        viewModelScope.launch {
            _eventsMessages.emit(guideActionEvent)
        }
    }

    fun movingGuide(relativeGuidePath: RelativeGuidePath) {
        when (val context = getGuideMoveUseCase.invoke()) {
            is GuideContext.Moving -> {
                val guideDomainModel = cachedGuides.find { it.nameGuide == context.guide.nameGuide }

                if (guideDomainModel != null) {
                    viewModelScope.launch {
                        _eventsMovingFiles.emit(UIMovingEvent.ExistFile)
                    }
                    return
                }

                onContinueProcess(true, relativeGuidePath)
            }

            else -> eventMovingFile("Error inesperado")
        }
    }

    fun onContinueProcess(confirmed: Boolean, relativeGuidePath: RelativeGuidePath) {
        viewModelScope.launch {
            if (!confirmed) return@launch

            when (val context = getGuideMoveUseCase.invoke()) {
                is GuideContext.Moving -> {
                    when (val guideData = getGuideXmlDataUseCase.invoke(context)) {
                        is GetGuideResult.Success -> {
                            val relGuidePathDomain = relativeGuidePath.toDomain()
                            val response =
                                moveGuideUseCase.invoke(guideData, context, relGuidePathDomain)
                            when (response) {
                                MoveGuideResponse.ErrorMovingGuide ->
                                    eventMovingFile("Error al intentar mover la guia")

                                MoveGuideResponse.ErrorMovingImages ->
                                    eventMovingFile("Error al intentar mover imagenes")

                                MoveGuideResponse.ErrorPathGuide ->
                                    eventMovingFile("No existe la ruta para mover la guia")

                                MoveGuideResponse.ErrorPathImages ->
                                    eventMovingFile("No existe una ruta para guardar las imagenes")

                                MoveGuideResponse.Success -> {
                                    eventMovingFile("Guia movida exitosamente")
                                }
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

    private fun eventMovingFile(message: String) {
        viewModelScope.launch {
            _eventsMovingFiles.emit(UIMovingEvent.ShowMessage(message))
        }
    }

    fun moveFileCancel() {
        eventMovingFile("Se ha cancelado la acción")
    }

    fun setContext(relativeGuidePath: RelativeGuidePath) {
        val relGuidePathDomain = relativeGuidePath.toDomain()
        val guide = selectedGuideDomain ?: return
        setContextMoveUseCase.invoke(guide, relGuidePathDomain)
        initRelativeGuide()
    }

    fun initRelativeGuide() {
        viewModelScope.launch {
            resetNavigationUseCase.invoke()
        }
    }
}
