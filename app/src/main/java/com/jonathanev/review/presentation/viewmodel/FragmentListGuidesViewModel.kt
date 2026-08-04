package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.DeleteGuideUseCase
import com.jonathanev.review.domain.GetGuideMoveUseCase
import com.jonathanev.review.domain.GetGuidePosicionUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.MoveGuideUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.domain.SetActiveGuideUseCase
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
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.state.ActionDialogState
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
    private val resetNavigationUseCase: ResetNavigationUseCase,
    private val setActiveGuideUseCase: SetActiveGuideUseCase
) : ViewModel() {
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

    private val _dialogState =
        MutableStateFlow<ActionDialogState<GuideUiModel>>(ActionDialogState.Hidden)
    val dialogState: StateFlow<ActionDialogState<GuideUiModel>> = _dialogState.asStateFlow()

    private val _guides = MutableStateFlow<List<GuideUiModel>>(listOf())
    val guides = _guides.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<GuideActionEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow() // usar estos eventos en compose

    fun getGuideSelected(guides: List<GuideUiModel>, position: Int): GuideResultUi {
        return when (val result =
            getGuidePosicionUseCase.invoke(position, guides.map { it.toDomain() })) {
            GuideResultDomain.Error -> result.toUi()
            is GuideResultDomain.Success -> {
                selectedGuideDomain = result.guideDomainModel
                result.toUi()
            }
        }
    }

    fun deleteGuide(guides: List<GuideUiModel>, nameGuide: String) {
        viewModelScope.launch {
            val guideUIModel = guides.find { it.nameGuide == nameGuide }
            if (guideUIModel == null) {
                emitMessage(GuideActionEvent.ShowMessage("No se ha encontrado la guia"))
                return@launch
            }

            val guideDomainModel = guideUIModel.toDomain()
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

    fun movingGuide(guides: List<GuideUiModel>) {
        when (val context = getGuideMoveUseCase.invoke()) {
            is GuideContext.Moving -> {
                val isExistGuide = guides.any { it.nameGuide == context.guide.nameGuide }

                if (isExistGuide) {
                    viewModelScope.launch {
                        _eventsMovingFiles.emit(UIMovingEvent.ExistFile)
                    }
                    return
                }

                onContinueProcess(true)
            }

            else -> eventMovingFile("Error inesperado")
        }
    }

    fun onContinueProcess(confirmed: Boolean) {
        viewModelScope.launch {
            if (!confirmed) return@launch

            when (val context = getGuideMoveUseCase.invoke()) {
                is GuideContext.Moving -> {
                    when (val guideData = getGuideXmlDataUseCase.invoke(context)) {
                        is GetGuideResult.Success -> {
                            val response =
                                moveGuideUseCase.invoke(guideData, context)
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

    fun setContext() {
        viewModelScope.launch {
            onDismissDialog()
            val guide = selectedGuideDomain ?: return@launch
            setContextMoveUseCase.invoke(guide)
            resetNavigationUseCase.invoke()
        }
    }

    fun setActiveGuide(guideUIModel: GuideUiModel) {
        viewModelScope.launch {
            onDismissDialog()
            setActiveGuideUseCase.invoke(guideUIModel.toDomain())
        }
    }

    fun onOpenMenu(guide: GuideUiModel) {
        _dialogState.value = ActionDialogState.OptionsMenu(guide)
    }

    fun onRequestDelete(guide: GuideUiModel) {
        _dialogState.value = ActionDialogState.ConfirmDelete(guide)
    }

    fun onDismissDialog() {
        _dialogState.value = ActionDialogState.Hidden
    }

    fun onConfirmDelete(guide: GuideUiModel) {
        viewModelScope.launch {
            onDismissDialog()
            val guideDomainModel = guide.toDomain()
            deleteGuideUseCase.invoke(guideDomainModel)
        }
    }
}
