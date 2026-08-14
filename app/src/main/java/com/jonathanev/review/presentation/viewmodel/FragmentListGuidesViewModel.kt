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
import com.jonathanev.review.domain.ObservePathUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.domain.SetActiveGuideUseCase
import com.jonathanev.review.domain.SetContextMoveUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.GuideResultDomain
import com.jonathanev.review.domain.result.MoveGuideResponse
import com.jonathanev.review.presentation.event.NavGuideActionEvent
import com.jonathanev.review.presentation.event.StateGuideActionEvent
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.state.ActionDialogState
import com.jonathanev.review.presentation.state.GuidesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

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
    private val clearGuideMoveUseCase: ClearGuideMoveUseCase,
    observePathUseCase: ObservePathUseCase
) : ViewModel() {
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

    private val _dialogState =
        MutableStateFlow<ActionDialogState<GuideUiModel>>(ActionDialogState.Hidden)
    val dialogState: StateFlow<ActionDialogState<GuideUiModel>> = _dialogState.asStateFlow()

    private val _navGuideActionEvent = MutableSharedFlow<NavGuideActionEvent>()
    val navGuideActionEvent = _navGuideActionEvent.asSharedFlow()

    private val _stateGuideActionEvent = MutableSharedFlow<StateGuideActionEvent>()
    val stateGuideActionEvent = _stateGuideActionEvent.asSharedFlow()

    val interactionMode: StateFlow<FileInteractionMode> = getGuideContextUseCase()
        .map { activeMoving ->
            if (activeMoving is GuideContext.Moving) FileInteractionMode.MovingItem else FileInteractionMode.Default
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FileInteractionMode.Default
        )

    private val _highlightedGuide = MutableStateFlow<GuideUiModel?>(null)
    val highlightedGuide: StateFlow<GuideUiModel?> = _highlightedGuide.asStateFlow()

    private var highlightJob: Job? = null

    init {
        viewModelScope.launch {
            getGuideContextUseCase().collect { context ->
                val guide = when (context) {
                    is GuideContext.Moving -> context.guide.toUi()
                    is GuideContext.Editing -> context.guide.toUi()
                    is GuideContext.Creating -> context.guide.toUi()
                    else -> null
                }

                if (guide != null && guide != _highlightedGuide.value) {
                    _highlightedGuide.value = guide
                    highlightJob?.cancel()
                    highlightJob = viewModelScope.launch {
                        delay(7000.milliseconds)
                        _highlightedGuide.value = null
                    }
                } else if (guide == null) {
                    _highlightedGuide.value = null
                    highlightJob?.cancel()
                }
            }
        }
    }

    fun onCancelMove() {
        viewModelScope.launch {
            try {
                clearGuideMoveUseCase.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                emitMessage(
                    StateGuideActionEvent.ShowMessage(
                        e.message ?: "Ocurrió un error inesperado"
                    )
                )
            }
        }
    }

    fun getGuideSelected(guides: List<GuideUiModel>, position: Int): GuideResultUi {
        return when (val result =
            getGuidePosicionUseCase.invoke(position, guides.map { it.toDomain() })) {
            GuideResultDomain.Error -> result.toUi()
            is GuideResultDomain.Success -> result.toUi()
        }
    }

    private fun emitNavigation(navGuideActionEvent: NavGuideActionEvent) {
        viewModelScope.launch {
            _navGuideActionEvent.emit(navGuideActionEvent)
        }
    }

    private fun emitMessage(stateGuideActionEvent: StateGuideActionEvent) {
        viewModelScope.launch {
            _stateGuideActionEvent.emit(stateGuideActionEvent)
        }
    }

    fun movingGuide(guides: List<GuideUiModel>) {
        viewModelScope.launch {
            when (val context = getGuideContextUseCase.invoke().firstOrNull()) {
                is GuideContext.Moving -> {
                    val isExistGuide = guides.any { it.nameGuide == context.guide.nameGuide }

                    if (isExistGuide) {
                        emitMessage(StateGuideActionEvent.ExistFile)
                        return@launch
                    }

                    onContinueProcess(true)
                }

                else -> emitMessage(StateGuideActionEvent.ShowMessage("Error inesperado"))
            }
        }
    }

    fun onContinueProcess(confirmed: Boolean) {
        viewModelScope.launch {
            try {
                if (!confirmed) return@launch

                when (val context = getGuideContextUseCase.invoke().firstOrNull()) {
                    is GuideContext.Moving -> {
                        when (val guideData = getGuideXmlDataUseCase.invoke(context)) {
                            is GetGuideResult.Success -> {
                                val response =
                                    moveGuideUseCase.invoke(guideData, context)
                                when (response) {
                                    MoveGuideResponse.ErrorMovingGuide ->
                                        emitMessage(StateGuideActionEvent.ShowMessage("Error al intentar mover la guia"))

                                    MoveGuideResponse.ErrorMovingImages ->
                                        emitMessage(StateGuideActionEvent.ShowMessage("Error al intentar mover imagenes"))

                                    MoveGuideResponse.ErrorPathGuide ->
                                        emitMessage(StateGuideActionEvent.ShowMessage("No existe la ruta para mover la guia"))

                                    MoveGuideResponse.ErrorPathImages ->
                                        emitMessage(StateGuideActionEvent.ShowMessage("No existe una ruta para guardar las imagenes"))

                                    MoveGuideResponse.Success -> {
                                        emitMessage(StateGuideActionEvent.ShowMessage("Guia movida exitosamente"))
                                    }
                                }
                            }

                            GetGuideResult.InvalidFormat ->
                                emitMessage(StateGuideActionEvent.ShowMessage("La guia está dañada"))

                            GetGuideResult.NotFound ->
                                emitMessage(StateGuideActionEvent.ShowMessage("No se ha encontrado la guia"))

                            GetGuideResult.UnknownError ->
                                emitMessage(StateGuideActionEvent.ShowMessage("Guia movida exitosamente"))
                        }
                    }

                    else ->
                        emitMessage(StateGuideActionEvent.ShowMessage("Error inesperado"))
                }
            } finally {
                clearGuideMoveUseCase.invoke()
            }
        }
    }

    fun setContextMoving(guide: GuideUiModel) {
        viewModelScope.launch {
            try {
                val guideDomainModel = guide.toDomain()
                val guideContext = GuideContext.Moving(
                    guide = guideDomainModel,
                    oldRelativeGuidePath = RelativeGuidePath("")
                )
                setContextMoveUseCase.invoke(guideContext)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                emitMessage(
                    StateGuideActionEvent.ShowMessage(
                        e.message ?: "Ocurrió un error inesperado"
                    )
                )
            } finally {
                resetNavigationUseCase.invoke()
            }
        }
    }

    fun setActiveGuide(guideUIModel: GuideUiModel) {
        viewModelScope.launch {
            try {
                setActiveGuideUseCase.invoke(guideUIModel.toDomain())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                emitMessage(
                    StateGuideActionEvent.ShowMessage(
                        e.message ?: "Ocurrió un error inesperado"
                    )
                )
            }
        }
    }

    fun clearActiveGuide() {
        viewModelScope.launch {
            try {
                clearActiveGuideUseCase.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                emitMessage(
                    StateGuideActionEvent.ShowMessage(
                        e.message ?: "Ocurrió un error inesperado"
                    )
                )
            }
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
                    emitMessage(StateGuideActionEvent.GuideDeleteSuccess)
                }

                DeleteGuideResult.ErrorGuide -> emitMessage(StateGuideActionEvent.ShowMessage("Hubo un error al borrar la guia"))
                DeleteGuideResult.ErrorImage ->
                    emitMessage(StateGuideActionEvent.ShowMessage("Hubo inconvenientes en el borrado completo de archivos"))

                else -> emitMessage(StateGuideActionEvent.ShowMessage("Ocurrió un error al eliminar la guia"))
            }
        }
    }

    fun onOpenGuide(guideUIModel: GuideUiModel) {
        onDismissDialog()
        setActiveGuide(guideUIModel)
        emitNavigation(NavGuideActionEvent.OpenNavGuide)
    }

    fun onRenameGuide(guideUIModel: GuideUiModel) {
        onDismissDialog()
        emitNavigation(NavGuideActionEvent.RenameNavGuide(guideUIModel))
    }

    fun onMoveGuide(guideUIModel: GuideUiModel) {
        onDismissDialog()
        setContextMoving(guideUIModel)
        emitNavigation(NavGuideActionEvent.MoveNavGuide)
    }

    /*fun resetNavigationPath() {
        viewModelScope.launch {
            try {
                resetNavigationUseCase.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                emitMessage(GuideActionEvent.ShowMessage(e.message ?: "Ocurrió un error inesperado"))
            }
        }
    }*/
}
