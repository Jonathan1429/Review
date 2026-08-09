package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.ClearActiveGuideUseCase
import com.jonathanev.review.domain.ClearGuideMoveUseCase
import com.jonathanev.review.domain.DeleteGuideUseCase
import com.jonathanev.review.domain.GetGuideContextUseCase
import com.jonathanev.review.domain.GetGuidePosicionUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.MoveGuideUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.domain.SetActiveGuideUseCase
import com.jonathanev.review.domain.SetContextMoveUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.GuideResultDomain
import com.jonathanev.review.domain.result.MoveGuideResponse
import com.jonathanev.review.presentation.event.GuideActionEvent
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.FileInteractionMode
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
import kotlinx.coroutines.flow.firstOrNull
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
    private val getGuideContextUseCase: GetGuideContextUseCase,
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val moveGuideUseCase: MoveGuideUseCase,
    private val resetNavigationUseCase: ResetNavigationUseCase,
    private val setActiveGuideUseCase: SetActiveGuideUseCase,
    private val clearActiveGuideUseCase: ClearActiveGuideUseCase,
    private val clearGuideMoveUseCase: ClearGuideMoveUseCase
) : ViewModel() {
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

    private val _dialogState =
        MutableStateFlow<ActionDialogState<GuideUiModel>>(ActionDialogState.Hidden)
    val dialogState: StateFlow<ActionDialogState<GuideUiModel>> = _dialogState.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<GuideActionEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow() // usar estos eventos en compose

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
            clearGuideMoveUseCase.invoke()
        }
    }

    fun getGuideSelected(guides: List<GuideUiModel>, position: Int): GuideResultUi {
        return when (val result =
            getGuidePosicionUseCase.invoke(position, guides.map { it.toDomain() })) {
            GuideResultDomain.Error -> result.toUi()
            is GuideResultDomain.Success -> result.toUi()
        }
    }

    private fun emitMessage(guideActionEvent: GuideActionEvent) {
        viewModelScope.launch {
            _eventsMessages.emit(guideActionEvent)
        }
    }

    fun movingGuide(guides: List<GuideUiModel>) {
        viewModelScope.launch {
            when (val context = getGuideContextUseCase.invoke().firstOrNull()) {
                is GuideContext.Moving -> {
                    val isExistGuide = guides.any { it.nameGuide == context.guide.nameGuide }

                    if (isExistGuide) {
                        _eventsMovingFiles.emit(UIMovingEvent.ExistFile)
                        return@launch
                    }

                    onContinueProcess(true)
                }

                else -> eventMovingFile("Error inesperado")
            }
        }
    }

    fun onContinueProcess(confirmed: Boolean) {
        viewModelScope.launch {
            if (!confirmed) return@launch

            when (val context = getGuideContextUseCase.invoke().firstOrNull()) {
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

    fun setContextMoving(guide: GuideUiModel) {
        viewModelScope.launch {
            val guideDomainModel = guide.toDomain()
            val guideContext = GuideContext.Moving(
                guide = guideDomainModel,
                oldRelativeGuidePath = RelativeGuidePath("")
            )
            setContextMoveUseCase.invoke(guideContext)
            resetNavigationUseCase.invoke()
        }
    }

    fun setActiveGuide(guideUIModel: GuideUiModel) {
        viewModelScope.launch {
            setActiveGuideUseCase.invoke(guideUIModel.toDomain())
        }
    }

    fun clearActiveGuide() {
        viewModelScope.launch {
            clearActiveGuideUseCase.invoke()
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
            val response = deleteGuideUseCase.invoke(guideDomainModel)
            when (response) {
                DeleteGuideResult.DeleteSuccess -> {
                    emitMessage(GuideActionEvent.GuideDeleteSuccess)
                }

                DeleteGuideResult.ErrorGuide -> emitMessage(GuideActionEvent.ShowMessage("Hubo un error al borrar la guia"))
                DeleteGuideResult.ErrorImage ->
                    emitMessage(GuideActionEvent.ShowMessage("Hubo inconvenientes en el borrado completo de archivos"))

                else -> emitMessage(GuideActionEvent.ShowMessage("Ocurrió un error al eliminar la guia"))
            }
        }
    }

    fun onOpenGuide(guideUIModel: GuideUiModel) {
        onDismissDialog()
        setActiveGuide(guideUIModel)
        emitMessage(GuideActionEvent.OpenGuide)
    }

    fun onRenameGuide(guideUIModel: GuideUiModel) {
        onDismissDialog()
        emitMessage(GuideActionEvent.RenameGuide(guideUIModel))
    }

    fun onMoveGuide(guideUIModel: GuideUiModel) {
        onDismissDialog()
        setContextMoving(guideUIModel)
        emitMessage(GuideActionEvent.MoveGuide)
    }
}
