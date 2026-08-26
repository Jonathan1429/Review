package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetActiveGuideUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.GetPreviewQuestionsUseCase
import com.jonathanev.review.domain.SetContextEditUseCase
import com.jonathanev.review.domain.SetContextPlayUseCase
import com.jonathanev.review.domain.SetCrearXmlUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.presentation.event.PreviewGuideEvent
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.ActiveGuideUIState
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    fun moveQuestion(from: Int, to: Int) {
        val currentPreviewList = _uiState.value.previewState.toMutableList()
        if (from !in currentPreviewList.indices || to !in currentPreviewList.indices) return

        // 1. Actualización inmediata de la UI (Optimistic UI update)
        val item = currentPreviewList.removeAt(from)
        currentPreviewList.add(to, item)
        _uiState.update { it.copy(previewState = currentPreviewList) }

        // 2. Persistencia en segundo plano
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val activeGuide = getActiveGuideUseCase.invoke().firstOrNull() ?: return@launch
                val context = GuideContext.Browsing(guide = activeGuide, -1)
                val result = getGuideXmlDataUseCase.invoke(context = context)

                if (result is GetGuideResult.Success) {
                    val domainList = result.list.toMutableList()
                    if (from in domainList.indices && to in domainList.indices) {
                        val domainItem = domainList.removeAt(from)
                        domainList.add(to, domainItem)

                        setCrearXmlUseCase.invoke(
                            guideDomainModel = activeGuide,
                            preguntas = domainList.map { it.question },
                            respuestas = domainList.map { it.answer },
                            saveGuideMode = SaveGuideMode.Update
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Opcional: revertir UI si falla el guardado
                loadInitialData()
            }
        }
    }
}
