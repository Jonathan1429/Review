package com.jonathanev.review.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.data.mapper.toColorType
import com.jonathanev.review.domain.DeleteGuideUseCase
import com.jonathanev.review.domain.ExistXMLGuideV1UseCase
import com.jonathanev.review.domain.IsExistFileUseCase
import com.jonathanev.review.domain.IsExistFolderUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.RenameGuideUseCase
import com.jonathanev.review.domain.SaveMetadataUseCase
import com.jonathanev.review.domain.ValidateCreateFileUseCase
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.ExistGuideV1Result
import com.jonathanev.review.domain.result.RenamedGuideResult
import com.jonathanev.review.domain.result.ValidateCreateFileResult
import com.jonathanev.review.presentation.event.CreateFilesEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.presentation.model.ScreenDataUi
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
    private val isExistFileUseCase: IsExistFileUseCase,
    private val isExistFolderUseCase: IsExistFolderUseCase,
    private val deleteGuideUseCase: DeleteGuideUseCase,
    private val existXMLGuideV1UseCase: ExistXMLGuideV1UseCase
) : ViewModel() {
    private var cachedGuides: List<GuideDomainModel> = emptyList()

    var uiStateComposable by mutableStateOf(PreviewState())
        private set

    private val _uiState = MutableStateFlow(PreviewState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateFilesEvent>()
    val events = _events.asSharedFlow()

    /*private val _eventsMessages = MutableSharedFlow<RenameGuideEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()*/

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

        uiStateComposable = uiStateComposable.copy(
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

    fun changeIconSelected(position: Int, icon: IconType) {
        uiStateComposable = uiStateComposable.copy(
            selectedIndex = position,
            icon = icon
        )
    }

    fun changeColorSelected(color: Int) {
        uiStateComposable = uiStateComposable.copy(
            color = color.toColorType()
        )
    }

    fun setColor(color: Int) {
        val randomColor = ColorType.RandomColor(color)

        _uiState.value =
            _uiState.value.copy(
                color = randomColor
            )
    }

    fun validateData() {
        val state = uiStateComposable

        // Validación del nombre del archivo
        viewModelScope.launch {

            when (val response = validateCreateFileUseCase.invoke(state.name, state.description)) {
                is ValidateCreateFileResult.Error -> _events.emit(
                    CreateFilesEvent.ShowMessage(
                        response.message
                    )
                ) //CreatingFileUiState.Message(response.message)
                is ValidateCreateFileResult.Success -> prepareScreenData()
            }
        }
    }

    fun saveMetadata(data: ScreenDataUi) {
        // Esto estaba descomentado
        //val screenDataDomain = data.toDomain()

        //saveMetadataUseCase.invoke(screenDataDomain)
    }

    fun fillFields(fileName: String): GuideResultUi {
        val guideDomainModel = cachedGuides.find { it.nameGuide == fileName }

        if (guideDomainModel == null) {
            return GuideResultUi.Error
        }

        return GuideResultUi.Success(guideDomainModel.toUi())
    }

    fun renameFile(
        oldName: String,
        fileName: String,
        description: String,
        relativeGuidePath: RelativeGuidePath
    ) {
        val guide = cachedGuides.find { it.nameGuide == oldName }
        if (guide == null) {
            emitMessage("No se ha encontrado la guia a renombrar")
            return
        }

        viewModelScope.launch {
            when (renameGuideUseCase.invoke(
                guide = guide,
                relativeGuidePath = relativeGuidePath,
                newName = fileName,
                description = description
            )) {
                RenamedGuideResult.ImageError ->
                    emitMessage("No se pasaron correctamente todas las imagenes")

                RenamedGuideResult.RenamedError ->
                    emitMessage("No se ha podido renombrar la guia")

                RenamedGuideResult.Success -> {
                    emitMessage("Guia renombrada exitosamente")

                    val xmlGuideV1 = GuideDomainModel(GuideVersion.V1, fileName, description)

                    when (existXMLGuideV1UseCase.invoke(xmlGuideV1, relativeGuidePath)) {
                        ExistGuideV1Result.Error ->
                            Log.d("RenameGuide", "Error al validar la guia V1")

                        ExistGuideV1Result.ExistGuide -> {
                            when (deleteGuideUseCase.invoke(
                                guideDomainModel = xmlGuideV1,
                                relativeGuidePath = relativeGuidePath
                            )) {
                                DeleteGuideResult.DeleteSuccess ->
                                    Log.d("RenameGuide", "Guia V1 eliminada correctamente")

                                DeleteGuideResult.Error ->
                                    Log.d("RenameGuide", "Error general al eliminar la Guia V1")

                                DeleteGuideResult.ErrorGuide ->
                                    Log.d("RenameGuide", "Error eliminando archivo de la Guia V1")

                                DeleteGuideResult.ErrorImage ->
                                    Log.d("RenameGuide", "Error eliminando imagenes de la Guia V1")

                                DeleteGuideResult.InvalidFormat ->
                                    Log.d("RenameGuide", "Formato invalido de la Guia V1")

                                DeleteGuideResult.NotFound ->
                                    Log.d("RenameGuide", "Guia V1 no encontrada")

                                DeleteGuideResult.UnknownError ->
                                    Log.d("RenameGuide", "Error desconocido de la Guia V1")
                            }
                        }

                        ExistGuideV1Result.NoExistGuide ->
                            Log.d("RenameGuide", "No existe Guia V1")
                    }
                }

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
            _events.emit(CreateFilesEvent.ShowMessage(text))
        }
    }

    private fun emitMessages(text: String) {
        viewModelScope.launch {
            _messages.emit(CreatingFileUiState.Message(text))
        }
    }

    fun fileExist(mode: FolderAction, name: String): Boolean {
        return when (mode) {
            FolderAction.CreatingFile -> isExistFileUseCase.invoke(
                cachedGuides = cachedGuides,
                name = name,
                oldName = ""
            )

            is FolderAction.RenamingFile -> {
                isExistFileUseCase.invoke(
                    cachedGuides = cachedGuides,
                    name = name,
                    oldName = mode.fileName
                )
            }

            FolderAction.CreatingFolder -> isExistFolderUseCase.invoke(name)
            else -> {
                emitMessage("Error inesperado")
                true
            }
        }
    }

    // Esto no se estaba usando pero hay verificar nuevamente esto
    fun onContinueProcess(confirmed: Boolean, name: String, description: String) {
        if (!confirmed) return
        // Esto estaba descomentado
        //validateData(name, description)
    }

    fun uploadCachedGuides(relativeGuidePath: RelativeGuidePath) {
        cachedGuides = loadGuidesUseCase.invoke(relativeGuidePath)
    }

    fun prepareScreenData() {
        viewModelScope.launch {
            val state = uiStateComposable

            val isExistFile = fileExist(state.mode, state.name)

            when (state.mode) {
                FolderAction.CreatingFolder -> {
                    if (isExistFile) {
                        //_events.emit(CreateFilesEvent.ShowMessage("Ya tienes una carpeta con el mismo nombre"))
                        emitMessage("Ya tienes una carpeta con el mismo nombre")
                    } else {
                        _events.emit(CreateFilesEvent.CreatingFolder)
                    }
                }

                FolderAction.RenamingFolder -> emitMessage("Renombrar archivos es una opción no habilitada")

                FolderAction.CreatingFile -> {
                    if (isExistFile) {
                        uiStateComposable = state.copy(
                            showDialog = true
                        )
                    } else {
                        _events.emit(CreateFilesEvent.CreateFile)
                    }
                }

                is FolderAction.RenamingFile -> {
                    if (isExistFile) {
                        uiStateComposable = state.copy(
                            showDialog = true
                        )
                    } else {
                        _events.emit(CreateFilesEvent.RenamingFile)
                    }
                }

                FolderAction.None -> emitMessage("No se puede procesar esta solicitud")
                is FolderAction.MovingFile -> Log.i("Moviendo: ", "Moviendo archivos")
            }
        }
    }

    // Función para cuando el usuario pulsa "Confirmar" o "Cancelar"
    fun onConfirmAlertDialog(confirmed: Boolean) {
        val state = uiStateComposable
        // Cerramos el diálogo primero
        uiStateComposable = uiStateComposable.copy(showDialog = false)

        if (confirmed) {
            // Esto estaba descomentado
            //validateData(state.name, state.description)
        }
    }

    fun initWithMode(mode: FolderAction) {
        // Solo inicializamos si el modo es diferente al actual
        // para evitar reinicios innecesarios
        if (uiStateComposable.mode != mode) {
            uiStateComposable = uiStateComposable.copy(mode = mode)
            loadIconsFor(mode)
        }
    }

    fun onNameChange(newName: String) {
        uiStateComposable = uiStateComposable.copy(name = newName)
    }

    fun onDescriptionChange(newDesc: String) {
        uiStateComposable = uiStateComposable.copy(description = newDesc)
    }
}