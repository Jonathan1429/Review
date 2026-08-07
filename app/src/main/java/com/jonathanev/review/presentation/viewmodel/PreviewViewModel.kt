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

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val getPreviewQuestionsUseCase: GetPreviewQuestionsUseCase,
    private val getActiveGuideUseCase: GetActiveGuideUseCase,
    private val setContextEditUseCase: SetContextEditUseCase,
    private val setContextPlayUseCase: SetContextPlayUseCase
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PreviewQuestionStateUi> = getActiveGuideUseCase.invoke()
        .flatMapLatest { activeGuideDomain ->
            if (activeGuideDomain == null) {
                flowOf(
                    PreviewQuestionStateUi(
                        activeGuide = ActiveGuideUIState.Error,
                        previewState = emptyList()
                    )
                )
            } else {
                flow {
                    val context = GuideContext.Browsing(guide = activeGuideDomain, -1)
                    when (val result = getGuideXmlDataUseCase(context = context)) {
                        is GetGuideResult.Success -> {
                            val response = getPreviewQuestionsUseCase.invoke(result.list)
                            val responseToUi = response.map { it.toUi() }

                            emit(
                                PreviewQuestionStateUi(
                                    activeGuide = ActiveGuideUIState.Success(activeGuideDomain.toUi()),
                                    previewState = responseToUi
                                )
                            )
                        }

                        else -> emit(
                            PreviewQuestionStateUi(
                                activeGuide = ActiveGuideUIState.Success(activeGuideDomain.toUi()),
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
            initialValue = PreviewQuestionStateUi(previewState = emptyList())
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
            val activeGuide = getActiveGuideUseCase.invoke().firstOrNull() ?: return@launch
            setContextEditUseCase.invoke(GuideContext.Editing(activeGuide, position))
            sendEvent(PreviewGuideEvent.Editing)
        }
    }

    fun reviewGuide(position: Int) {
        viewModelScope.launch {
            val activeGuide = getActiveGuideUseCase.invoke().firstOrNull() ?: return@launch
            setContextPlayUseCase.invoke(GuideContext.Browsing(activeGuide, position))
            sendEvent(PreviewGuideEvent.Review)
        }
    }
}