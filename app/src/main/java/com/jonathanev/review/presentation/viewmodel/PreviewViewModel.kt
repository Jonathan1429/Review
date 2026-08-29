package com.jonathanev.review.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetActiveGuideUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.GetPreviewQuestionsUseCase
import com.jonathanev.review.domain.SetContextEditUseCase
import com.jonathanev.review.domain.SetContextPlayUseCase
import com.jonathanev.review.domain.SetCrearXmlUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.domain.model.SavingStatus
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.presentation.event.PreviewGuideEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.ActiveGuideUIState
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val getPreviewQuestionsUseCase: GetPreviewQuestionsUseCase,
    private val getActiveGuideUseCase: GetActiveGuideUseCase,
    private val setContextEditUseCase: SetContextEditUseCase,
    private val setContextPlayUseCase: SetContextPlayUseCase,
    private val setCrearXmlUseCase: SetCrearXmlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PreviewQuestionStateUi(
            activeGuide = ActiveGuideUIState.Loading,
            previewState = emptyList()
        )
    )
    val uiState: StateFlow<PreviewQuestionStateUi> = _uiState.asStateFlow()

    private val _previewGuideEvent = MutableSharedFlow<PreviewGuideEvent>()
    val previewGuideEvent = _previewGuideEvent.asSharedFlow()

    init {
        loadInitialData()
    }

    fun retryLoad() {
        loadInitialData()
    }

    // Variable a nivel de ViewModel
    private var domainListMemory: MutableList<QAItemDomain> = mutableListOf()

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(activeGuide = ActiveGuideUIState.Loading) }

            val activeGuideDomain = getActiveGuideUseCase.invoke().firstOrNull()
            if (activeGuideDomain == null) {
                _uiState.update { it.copy(activeGuide = ActiveGuideUIState.Error) }
                return@launch
            }

            val context = GuideContext.Browsing(guide = activeGuideDomain, -1)
            when (val result = getGuideXmlDataUseCase.invoke(context = context)) {
                is GetGuideResult.Success -> {
                    domainListMemory = result.list.toMutableList()

                    val previewQuestions = getPreviewQuestionsUseCase.invoke(result.list).map { it.toUi() }
                    _uiState.update {
                        it.copy(
                            activeGuide = ActiveGuideUIState.Success(activeGuideDomain.toUi()),
                            previewState = previewQuestions
                        )
                    }
                }
                else -> _uiState.update { it.copy(activeGuide = ActiveGuideUIState.Error) }
            }
        }
    }

    private fun sendEvent(previewGuideEvent: PreviewGuideEvent) {
        viewModelScope.launch {
            _previewGuideEvent.emit(previewGuideEvent)
        }
    }

    fun editingGuide(position: Int) {
        viewModelScope.launch {
            try {
                val activeGuide = getActiveGuideUseCase.invoke().firstOrNull() ?: return@launch
                setContextEditUseCase.invoke(GuideContext.Editing(activeGuide, position))
                sendEvent(PreviewGuideEvent.Editing)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                sendEvent(PreviewGuideEvent.ShowError(e.message ?: "Ocurrió un error inesperado"))
            }
        }
    }

    fun reviewGuide(position: Int) {
        viewModelScope.launch {
            try {
                val activeGuide = getActiveGuideUseCase.invoke().firstOrNull() ?: return@launch
                setContextPlayUseCase.invoke(GuideContext.Browsing(activeGuide, position))
                sendEvent(PreviewGuideEvent.Review)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                sendEvent(PreviewGuideEvent.ShowError(e.message ?: "Ocurrió un error inesperado"))
            }
        }
    }

    private var saveJob: Job? = null

    fun moveQuestion(from: Int, to: Int) {
        if (from == to) return

        _uiState.update { currentState ->
            val currentList = currentState.previewState.toMutableList()

            if (from !in currentList.indices || to !in currentList.indices ||
                from !in domainListMemory.indices || to !in domainListMemory.indices
            ) {
                return@update currentState
            }

            // 1. Reordenar listas inmediatas en memoria
            val itemUi = currentList.removeAt(from)
            currentList.add(to, itemUi)

            val itemDomain = domainListMemory.removeAt(from)
            domainListMemory.add(to, itemDomain)

            // Actualizar UI instantáneamente
            currentState.copy(
                previewState = currentList,
                savingStatus = SavingStatus.SAVING
            )
        }

        // 2. Cancelar la persistencia anterior (Debounce)
        saveJob?.cancel()

        // 3. Programar la persistencia al terminar las ráfagas de movimientos
        saveJob = viewModelScope.launch {
            delay(600.milliseconds)

            try {
                val activeGuide = getActiveGuideUseCase.invoke().firstOrNull() ?: run {
                    _uiState.update { it.copy(savingStatus = SavingStatus.ERROR) }
                    loadInitialData()
                    return@launch
                }

                // Snapshot thread-safe de la lista en memoria
                val snapshotMemory = domainListMemory.toList()

                withContext(Dispatchers.IO) {
                    setCrearXmlUseCase.invoke(
                        guideDomainModel = activeGuide,
                        preguntas = snapshotMemory.map { it.question },
                        respuestas = snapshotMemory.map { it.answer },
                        saveGuideMode = SaveGuideMode.Update
                    )
                }

                _uiState.update { it.copy(savingStatus = SavingStatus.SAVED) }
                delay(1200.milliseconds)
                _uiState.update { it.copy(savingStatus = SavingStatus.IDLE) }

            } catch (e: Exception) {
                if (e is CancellationException) throw e

                _uiState.update { it.copy(savingStatus = SavingStatus.ERROR) }
                loadInitialData()
            }
        }
    }
}
