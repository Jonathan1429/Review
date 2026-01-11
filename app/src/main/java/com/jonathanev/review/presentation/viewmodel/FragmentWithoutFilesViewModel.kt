package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.BackPathUseCase
import com.jonathanev.review.domain.MoveGuideUseCase
import com.jonathanev.review.domain.SetMainPathUseCase
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.presentation.model.QuestionItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragmentWithoutFilesViewModel @Inject constructor(
    private val moveGuideUseCase: MoveGuideUseCase,
    private val backPathUseCase: BackPathUseCase,
    private val setMainPathUseCase: SetMainPathUseCase
) : ViewModel() {
    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    private var _preguntas: MutableList<QuestionItemUi> = mutableListOf()
    val preguntas: List<QuestionItemUi> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemUi> = mutableListOf()
    val respuestas: List<QuestionItemUi> get() = _respuestas

    fun back(){
        backPathUseCase.invoke()
    }

    fun setMainPath() {
        setMainPathUseCase.invoke()
    }

    private fun eventMovingFile(message: String) {
        viewModelScope.launch {
            _eventsMovingFiles.emit(UIMovingEvent.ShowMessage(message))
        }
    }

    fun moveFileCancel() {
        eventMovingFile("Se ha cancelado la acción")
    }

    fun moveFileSuccess() {
        eventMovingFile("Se ha movido la guia correctamente")
    }

    fun onContinueProcess(confirmed: Boolean, mode: FolderAction): Boolean {
        if (!confirmed) return false

        return if (mode is FolderAction.MovingFile) {
            moveGuideUseCase.invoke(mode)
        } else {
            false
        }
    }
}