package com.jonathanev.review.presentation.files.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.BackPathUseCase
import com.jonathanev.review.domain.IsExistFileUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.RenameGuideUseCase
import com.jonathanev.review.domain.ReubicarImagenesUseCase
import com.jonathanev.review.domain.SaveMetadataUseCase
import com.jonathanev.review.domain.ValidateCreateFileUseCase
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.files.model.GuideUiModel
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.presentation.model.ScreenData
import com.jonathanev.review.presentation.state.CreatingFileUiState
import com.jonathanev.review.presentation.state.PreviewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateFilesViewModel @Inject constructor(
    private val reubicarImagenesUseCase: ReubicarImagenesUseCase,
    private val renameGuideUseCase: RenameGuideUseCase,
    private val validateCreateFileUseCase: ValidateCreateFileUseCase,
    private val saveMetadataUseCase: SaveMetadataUseCase,
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val backPathUseCase: BackPathUseCase,
    private val isExistFileUseCase: IsExistFileUseCase
) : ViewModel() {
    private var cachedGuides: List<GuideDomainModel> = emptyList()
    private var attributesGuide: GuideDomainModel? = null

    private val _uiState = MutableStateFlow(PreviewState())
    val uiState = _uiState.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<UIStopEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _messages = MutableSharedFlow<CreatingFileUiState>()
    val messages = _messages.asSharedFlow()

    private var _preguntas = mutableListOf<QuestionItemUi>()
    val preguntas: List<QuestionItemUi> get() = _preguntas

    private var _respuestas = mutableListOf<QuestionItemUi>()
    val respuestas: List<QuestionItemUi> get() = _respuestas

    fun loadIconsFor(action: FolderAction) {
        val icons = when (action) {
            FolderAction.CreatingFile -> listOf(IconType.LIGHTBULB)
            FolderAction.CreatingFolder -> listOf(
                IconType.ANCHOR_SOLID_FULL,
                IconType.ANGELLIST_BRANDS_SOLID_FULL,
                IconType.BACTERIA_SOLID_FULL
            )

            is FolderAction.RenamingFile -> listOf(IconType.LIGHTBULB)

            FolderAction.RenamingFolder -> listOf(
                IconType.ANCHOR_SOLID_FULL,
                IconType.ANGELLIST_BRANDS_SOLID_FULL,
                IconType.BACTERIA_SOLID_FULL
            )

            FolderAction.None -> emptyList()
            is FolderAction.MovingFile -> emptyList()
        }

        _uiState.value = PreviewState(
            icons = icons,
            selectedIndex = 0,
            icon = icons.first(),
            color = ColorType.Gray
        )
    }

    fun onIconSelected(position: Int) {
        val current = _uiState.value
        _uiState.value = current.copy(
            selectedIndex = position,
            icon = current.icons[position]
        )
    }

    fun setColor(color: Int) {
        val randomColor = ColorType.RandomColor(color)

        _uiState.value =
            _uiState.value.copy(
                color = randomColor
            )
    }

    fun processScreenData(name: String, description: String) {
        // Validación del nombre del archivo
        val response = validateCreateFileUseCase.invoke(name, description)

        viewModelScope.launch {
            _messages.emit(
                response
            )
        }
    }

    fun saveMetadata(data: ScreenData) {
        saveMetadataUseCase.invoke(data)
    }

    //fun getCurrentPath() = pathProvider.getCurrentPath()

    fun fillFields(fileName: String): GuideUiModel {
        val guideDomainModel = cachedGuides.find { it.nameGuide == fileName }
        attributesGuide = guideDomainModel
        return guideDomainModel!!.toUi()
    }

    fun getObtenerDatosXML() {
        /*if (respuestas.isEmpty()) {
            //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
            val datos = getObtenerDatosXMLUseCase.invoke(guideDomainModel)

            val tempQuestions =
                datos.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }.toList()
            val tempAnswers =
                datos.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toList()

            val questionsDomain = generateTextColorRangesUseCase.invoke(tempQuestions)
            val answersDomain = generateTextColorRangesUseCase.invoke(tempAnswers)
            _preguntas = questionsDomain.map { it.toUi() }.toMutableList()
            _respuestas = answersDomain.map { it.toUi() }.toMutableList()
        }*/
    }

    fun renameFile(fileName: String, description: String) {
        val questionsDomain = preguntas.map { it.toDomain() }
        val answersDomain = respuestas.map { it.toDomain() }

        reubicarImagenesUseCase.invoke(fileName, questionsDomain, answersDomain, attributesGuide!!)

        val isUpdated =
            renameGuideUseCase.invoke(
                fileName,
                description,
                questionsDomain,
                answersDomain,
                attributesGuide!!
            )

        if (isUpdated) {
            viewModelScope.launch {
                _eventsMessages.emit(
                    UIStopEvent.GuideRenamedSuccess("Se ha renombrado el archivo con exito")
                )
            }
        } else {
            viewModelScope.launch {
                _eventsMessages.emit(
                    UIStopEvent.ShowMessage("No se pudo renombrar el archivo")
                )
            }
            return
        }
    }

    fun fileExist(mode: FolderAction, name: String): Boolean {
        return isExistFileUseCase.invoke(mode, cachedGuides, name)
    }

    fun onContinueProcess(confirmed: Boolean, name: String, description: String) {
        if (!confirmed) return

        processScreenData(name, description)
    }

    fun beforePath() {
        backPathUseCase.invoke()
    }

    fun uploadCachedGuides() {
        cachedGuides = loadGuidesUseCase.invoke()
    }
}