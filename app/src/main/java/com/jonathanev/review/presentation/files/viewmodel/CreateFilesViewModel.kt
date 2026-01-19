package com.jonathanev.review.presentation.files.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.IsExistFileUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.RenameGuideUseCase
import com.jonathanev.review.domain.SaveMetadataUseCase
import com.jonathanev.review.domain.ValidateCreateFileUseCase
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.result.RenamedGuideResult
import com.jonathanev.review.presentation.event.RenameGuideEvent
import com.jonathanev.review.presentation.files.model.GuideResultUi
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.presentation.model.ScreenDataUi
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
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
    private val renameGuideUseCase: RenameGuideUseCase,
    private val validateCreateFileUseCase: ValidateCreateFileUseCase,
    private val saveMetadataUseCase: SaveMetadataUseCase,
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val navigationPathRepository: NavigationPathRepository,
    private val isExistFileUseCase: IsExistFileUseCase,
) : ViewModel() {
    private var cachedGuides: List<GuideDomainModel> = emptyList()

    private val _uiState = MutableStateFlow(PreviewState())
    val uiState = _uiState.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<RenameGuideEvent>()
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

    fun saveMetadata(data: ScreenDataUi) {
        val screenDataDomain = data.toDomain()

        saveMetadataUseCase.invoke(screenDataDomain)
    }

    fun fillFields(fileName: String): GuideResultUi {
        val guideDomainModel = cachedGuides.find { it.nameGuide == fileName }

        if (guideDomainModel == null) {
            return GuideResultUi.Error
        }

        return GuideResultUi.Success(guideDomainModel.toUi())
    }

    fun renameFile(oldName: String, fileName: String, description: String) {
        val guide = cachedGuides.find { it.nameGuide == oldName }
        if (guide == null) {
            emitMessage("No se ha encontrado la guia a renombrar")
            return
        }

        viewModelScope.launch {
            when (renameGuideUseCase.invoke(
                guide = guide,
                newName = fileName,
                description = description
            )) {
                RenamedGuideResult.ImageError ->
                    emitMessage("No se pasaron correctamente todas las imagenes")

                RenamedGuideResult.RenamedError ->
                    emitMessage("No se ha podido renombrar la guia")

                RenamedGuideResult.Success ->
                    emitMessage("Guia renombrada exitosamente")

                RenamedGuideResult.Error -> emitMessage("Ocurrió un error al abrir la guia")

                RenamedGuideResult.InvalidFormat -> emitMessage("La guia está dañada")

                RenamedGuideResult.NotFound -> emitMessage("No se ha encontrado la guia")

                RenamedGuideResult.UnknownError -> emitMessage("Error desconocido")
                RenamedGuideResult.GuidePathError ->
                    emitMessage("No fue posible renombrar la guia en la ruta actual")
            }
        }
    }

    private fun emitMessage(text: String) {
        viewModelScope.launch {
            _eventsMessages.emit(RenameGuideEvent.ShowMessage(text))
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
        navigationPathRepository.back()
    }

    fun uploadCachedGuides() {
        cachedGuides = loadGuidesUseCase.invoke()
    }
}