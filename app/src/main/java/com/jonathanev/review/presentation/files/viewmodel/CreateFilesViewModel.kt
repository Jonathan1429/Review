package com.jonathanev.review.presentation.files.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetCurrentPathGuidesUseCase
import com.jonathanev.review.domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.domain.IsExistFileUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.RenameGuideUseCase
import com.jonathanev.review.domain.SaveMetadataUseCase
import com.jonathanev.review.domain.ValidateCreateFileUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.RenamedGuideResult
import com.jonathanev.review.presentation.event.PrepareGuideEvent
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
    private val getCurrentPathGuidesUseCase: GetCurrentPathGuidesUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase
) : ViewModel() {
    private var cachedGuides: List<GuideDomainModel> = emptyList()

    private val _uiState = MutableStateFlow(PreviewState())
    val uiState = _uiState.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<PrepareGuideEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsRenameMessages = MutableSharedFlow<RenameGuideEvent>()
    val eventsRenameMessages = _eventsRenameMessages.asSharedFlow()

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
            return GuideResultUi.Error("No se ha encontrado la guia a cargar")
        }

        return GuideResultUi.Success(guideDomainModel.toUi())
    }

    fun renameFile(oldName: String, fileName: String, description: String) {
        val guideDomainModel = cachedGuides.find { it.nameGuide == oldName }
        if (guideDomainModel == null) {
            emitMessagePrepare("No se ha encontrado la guia a renombrar")
            return
        }

        val currentPath = getCurrentPathGuidesUseCase.invoke()
        //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
        when (val result = getObtenerDatosXMLUseCase.invoke(
            GuideContext.Actual(
                guideDomainModel,
                GuidePath(currentPath)
            )
        )) {
            is GetGuideResult.Success -> {
                viewModelScope.launch {
                    val response =
                        renameGuideUseCase.invoke(
                            fileName,
                            description,
                            result
                        )

                    when(response){
                        RenamedGuideResult.ImageError -> emitMessageRename(RenameGuideEvent.ImageError)
                        RenamedGuideResult.RenamedError -> emitMessageRename(RenameGuideEvent.RenamedError)
                        RenamedGuideResult.Success -> emitMessageRename(RenameGuideEvent.Success)
                    }
                }
            }

            GetGuideResult.Error -> emitMessagePrepare("Ocurrió un error al abrir la guia")

            GetGuideResult.InvalidFormat -> emitMessagePrepare("La guia está dañada")

            GetGuideResult.NotFound -> emitMessagePrepare("No se ha encontrado la guia")

            GetGuideResult.UnknownError -> emitMessagePrepare("Error desconocido")
        }
    }

    private fun emitMessagePrepare(text: String) {
        viewModelScope.launch {
            _eventsMessages.emit(PrepareGuideEvent.ShowMessage(text))
        }
    }

    private fun emitMessageRename(imageError: RenameGuideEvent) {
        viewModelScope.launch {
            _eventsRenameMessages.emit(imageError)
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