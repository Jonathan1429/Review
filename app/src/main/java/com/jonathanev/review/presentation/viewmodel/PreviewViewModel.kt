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
        val currentPreviewList = _uiState.value.previewState.toMutableList()

        Log.d("ReorderDebug", "🔄 moveQuestion SOLICITADO: from=$from -> to=$to")

        // Validar rangos en ambas listas
        if (from !in currentPreviewList.indices || to !in currentPreviewList.indices ||
            from !in domainListMemory.indices || to !in domainListMemory.indices
        ) {
            Log.e(
                "ReorderDebug",
                "❌ Índices fuera de rango: from=$from, to=$to (previewSize=${currentPreviewList.size}, memorySize=${domainListMemory.size})"
            )
            return
        }

        // 🟢 1. Reordenar UI aplicando copy() para romper la referencia y forzar actualización en Compose
        val item = currentPreviewList.removeAt(from)
        currentPreviewList.add(to, item.copy())

        // 🟢 2. Reordenar Memoria de Dominio
        val domainItem = domainListMemory.removeAt(from)
        domainListMemory.add(to, domainItem)

        Log.d("ReorderDebug", "✅ Reordenamiento local exitoso en UI y Memoria")

        _uiState.update {
            it.copy(
                previewState = currentPreviewList,
                savingStatus = SavingStatus.SAVING
            )
        }

        // 3. Cancelar debounce anterior
        saveJob?.cancel()

        // 4. Copiar snapshot inmutable para el Hilo IO
        val snapshotMemory = domainListMemory.toList()

        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(600.milliseconds)

            try {
                Log.d("ReorderDebug", "💾 Guardando nuevo orden en XML...")
                val activeGuide = getActiveGuideUseCase.invoke().firstOrNull() ?: run {
                    Log.e("ReorderDebug", "❌ getActiveGuideUseCase devolvió null al reordenar")
                    _uiState.update { it.copy(savingStatus = SavingStatus.ERROR) }
                    loadInitialData()
                    return@launch
                }

                setCrearXmlUseCase.invoke(
                    guideDomainModel = activeGuide,
                    preguntas = snapshotMemory.map { it.question },
                    respuestas = snapshotMemory.map { it.answer },
                    saveGuideMode = SaveGuideMode.Update
                )

                Log.d("ReorderDebug", "🎉 XML guardado correctamente en disco")

                _uiState.update { it.copy(savingStatus = SavingStatus.SAVED) }
                delay(1200.milliseconds)
                _uiState.update { it.copy(savingStatus = SavingStatus.IDLE) }

            } catch (e: Exception) {
                if (e is CancellationException) throw e

                Log.e("ReorderDebug", "💥 Excepción guardando orden (from: $from, to: $to)", e)
                loadInitialData()
                _uiState.update { it.copy(savingStatus = SavingStatus.ERROR) }
            }
        }
    }
}
