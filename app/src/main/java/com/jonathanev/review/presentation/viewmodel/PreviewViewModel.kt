package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetActiveGuideUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.GetPreviewQuestionsUseCase
import com.jonathanev.review.domain.SetContextEditUseCase
import com.jonathanev.review.domain.SetContextPlayUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.presentation.event.PreviewGuideEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.ActiveGuideUIState
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val getPreviewQuestionsUseCase: GetPreviewQuestionsUseCase,
    private val getActiveGuideUseCase: GetActiveGuideUseCase,
    private val setContextEditUseCase: SetContextEditUseCase,
    private val setContextPlayUseCase: SetContextPlayUseCase
) : ViewModel() {
    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PreviewQuestionStateUi> = retryTrigger
        .flatMapLatest {
            getActiveGuideUseCase.invoke()
        }
        .flatMapLatest { activeGuideDomain ->
            if (activeGuideDomain == null) {
                flowOf(
                    PreviewQuestionStateUi(
                        activeGuide = ActiveGuideUIState.Loading,
                        previewState = emptyList()
                    )
                )
            } else {
                flow {
                    emit(
                        PreviewQuestionStateUi(
                            activeGuide = ActiveGuideUIState.Loading,
                            previewState = emptyList()
                        )
                    )

                    val context = GuideContext.Browsing(guide = activeGuideDomain, -1)
                    when (val result = getGuideXmlDataUseCase.invoke(context = context)) {
                        is GetGuideResult.Success -> {
                            val response = getPreviewQuestionsUseCase(result.list)
                            emit(
                                PreviewQuestionStateUi(
                                    activeGuide = ActiveGuideUIState.Success(
                                        activeGuideDomain.toUi()
                                    ),
                                    previewState = response.map { it.toUi() }
                                )
                            )
                        }

                        else -> emit(
                            PreviewQuestionStateUi(
                                activeGuide = ActiveGuideUIState.Error,
                                previewState = emptyList()
                            )
                        )
                    }
                }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PreviewQuestionStateUi(
                activeGuide = ActiveGuideUIState.Loading,
                previewState = emptyList()
            )
        )

    private val _previewGuideEvent = MutableSharedFlow<PreviewGuideEvent>()
    val previewGuideEvent = _previewGuideEvent.asSharedFlow()

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

    fun retryLoad() {
        retryTrigger.tryEmit(Unit)
    }
}