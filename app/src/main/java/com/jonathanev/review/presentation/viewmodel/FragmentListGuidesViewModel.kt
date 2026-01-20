package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.DeleteGuideUseCase
import com.jonathanev.review.domain.GetGuideMoveUseCase
import com.jonathanev.review.domain.GetGuidePosicionUseCase
import com.jonathanev.review.domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.MoveGuideUseCase
import com.jonathanev.review.domain.SetContextMoveUseCase
import com.jonathanev.review.domain.SetMainPathUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideResultDomain
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.MoveGuideResponse
import com.jonathanev.review.presentation.event.GuideActionEvent
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.files.model.GuideResultUi
import com.jonathanev.review.presentation.files.model.GuideUiModel
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragmentListGuidesViewModel @Inject constructor(
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val getGuidePosicionUseCase: GetGuidePosicionUseCase,
    private val setMainPathUseCase: SetMainPathUseCase,
    private val navigationPathRepository: NavigationPathRepository,
    private val deleteGuideUseCase: DeleteGuideUseCase,
    private val setContextMoveUseCase: SetContextMoveUseCase,
    private val getGuideMoveUseCase: GetGuideMoveUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val moveGuideUseCase: MoveGuideUseCase
) : ViewModel() {
    private var cachedGuides: List<GuideDomainModel> = emptyList()
    private var selectedGuideDomain: GuideDomainModel? = null

    private val _guides = MutableLiveData<List<GuideUiModel>>()
    val guides: LiveData<List<GuideUiModel>> = _guides

    private val _eventsMessages = MutableSharedFlow<GuideActionEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    private var _preguntas: MutableList<QuestionItemUi> = mutableListOf()
    val preguntas: List<QuestionItemUi> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemUi> = mutableListOf()
    val respuestas: List<QuestionItemUi> get() = _respuestas

    fun getAllGuides() {
        cachedGuides = loadGuidesUseCase.invoke()
        val guidesUi = cachedGuides.map { guide -> guide.toUi() }
        _guides.postValue(guidesUi)
    }

    fun getGuideSelected(position: Int): GuideResultUi {
        return when(val result = getGuidePosicionUseCase.invoke(position, cachedGuides)){
            GuideResultDomain.Error -> result.toUi()
            is GuideResultDomain.Success -> {
                selectedGuideDomain = result.guideDomainModel
                result.toUi()
            }
        }
    }

    fun deleteGuide(nameGuide: String) {
        val guideDomainModel = cachedGuides.find { it.nameGuide == nameGuide }
        if (guideDomainModel == null) {
            emitMessage(GuideActionEvent.ShowMessage("No se ha encontrado la guia"))
            return
        }

        val response = deleteGuideUseCase.invoke(guideDomainModel)
        when (response) {
            DeleteGuideResult.DeleteSuccess -> emitMessage(GuideActionEvent.Success("Guia borrada exitosamente"))
            DeleteGuideResult.Error -> emitMessage(GuideActionEvent.ShowMessage("Ocurrió un error al abrir la guia"))
            DeleteGuideResult.ErrorGuide -> emitMessage(GuideActionEvent.ShowMessage("Hubo un error al borrar la guia"))
            DeleteGuideResult.ErrorImage ->
                emitMessage(GuideActionEvent.ShowMessage("Hubo inconvenientes en el borrado completo de archivos"))

            DeleteGuideResult.InvalidFormat -> emitMessage(GuideActionEvent.ShowMessage("La guia está dañada"))
            DeleteGuideResult.NotFound -> emitMessage(GuideActionEvent.ShowMessage("No se ha encontrado la guia"))
            DeleteGuideResult.UnknownError -> emitMessage(GuideActionEvent.ShowMessage("Error desconocido"))
        }
    }

    private fun emitMessage(guideActionEvent: GuideActionEvent) {
        viewModelScope.launch {
            _eventsMessages.emit(guideActionEvent)
        }
    }

    fun movingGuide() {
        when (val context = getGuideMoveUseCase.invoke()) {
            is GuideContext.Moving -> {
                val guideDomainModel = cachedGuides.find { it.nameGuide == context.guide.nameGuide }

                if (guideDomainModel != null) {
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
        if (!confirmed) return

        when (val context = getGuideMoveUseCase.invoke()) {
            is GuideContext.Moving -> {
                when (val guideData = getObtenerDatosXMLUseCase.invoke(context)) {
                    is GetGuideResult.Success -> {

                        val response = moveGuideUseCase.invoke(guideData, context)
                        when (response) {
                            MoveGuideResponse.ErrorMovingGuide ->
                                eventMovingFile("Error al intentar mover la guia")

                            MoveGuideResponse.ErrorMovingImages ->
                                eventMovingFile("Error al intentar mover imagenes")

                            MoveGuideResponse.ErrorPathGuide ->
                                eventMovingFile("No existe la ruta para mover la guia")

                            MoveGuideResponse.ErrorPathImages ->
                                eventMovingFile("No existe una ruta para guardar las imagenes")

                            MoveGuideResponse.Success ->
                                eventMovingFile("Guia movida exitosamente")

                            MoveGuideResponse.WarningDeleteFolder ->
                                eventMovingFile("Hubo inconveniente en el paso de todos los archivos")
                        }
                    }

                    GetGuideResult.Error -> eventMovingFile("Ocurrió un error al abrir la guia")

                    GetGuideResult.InvalidFormat -> eventMovingFile("La guia está dañada")

                    GetGuideResult.NotFound -> eventMovingFile("No se ha encontrado la guia")

                    GetGuideResult.UnknownError -> eventMovingFile("Error desconocido")
                }
            }

            else -> eventMovingFile("Error inesperado")
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

    fun back() {
        navigationPathRepository.back()
    }

    fun setMainPath() {
        setMainPathUseCase.invoke()
    }

    fun setContext() {
        val guide = selectedGuideDomain ?: return
        setContextMoveUseCase.invoke(guide)
    }
}
