package com.jonathanev.review.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.data.mapper.toColorType
import com.jonathanev.review.domain.CreateFolderUseCase
import com.jonathanev.review.domain.DeleteGuideUseCase
import com.jonathanev.review.domain.ExistXMLGuideV1UseCase
import com.jonathanev.review.domain.GetVersionGuideUseCase
import com.jonathanev.review.domain.IsExistFileUseCase
import com.jonathanev.review.domain.IsExistFolderUseCase
import com.jonathanev.review.domain.NextNavigationUseCase
import com.jonathanev.review.domain.RenameFolderUseCase
import com.jonathanev.review.domain.RenameGuideUseCase
import com.jonathanev.review.domain.ResetNavigationUseCase
import com.jonathanev.review.domain.SaveMetadataUseCase
import com.jonathanev.review.domain.SetActiveGuideUseCase
import com.jonathanev.review.domain.SetContextCreateUseCase
import com.jonathanev.review.domain.ValidateCreateFileUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.ExistGuideV1Result
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.ReadGuideError
import com.jonathanev.review.domain.result.RenamedGuideResult
import com.jonathanev.review.domain.result.ValidateCreateFileResult
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.model.ScreenDataUi
import com.jonathanev.review.presentation.state.CreatingUIState
import com.jonathanev.review.presentation.state.CreatingUIState.CreateFile
import com.jonathanev.review.presentation.state.CreatingUIState.CreateFolder
import com.jonathanev.review.presentation.state.CreatingUIState.Message
import com.jonathanev.review.presentation.state.CreatingUIState.RenameFile
import com.jonathanev.review.presentation.state.CreatingUIState.RenameFolder
import com.jonathanev.review.presentation.state.PropertiesFilesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class CreateFilesViewModel @Inject constructor(
    private val renameGuideUseCase: RenameGuideUseCase,
    private val validateCreateFileUseCase: ValidateCreateFileUseCase,
    private val saveMetadataUseCase: SaveMetadataUseCase,
    private val isExistFileUseCase: IsExistFileUseCase,
    private val isExistFolderUseCase: IsExistFolderUseCase,
    private val deleteGuideUseCase: DeleteGuideUseCase,
    private val existXMLGuideV1UseCase: ExistXMLGuideV1UseCase,
    private val getVersionGuideUseCase: GetVersionGuideUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val renameFolderUseCase: RenameFolderUseCase,
    private val resetNavigationUseCase: ResetNavigationUseCase,
    private val nextNavigationUseCase: NextNavigationUseCase,
    private val setActiveGuideUseCase: SetActiveGuideUseCase,
    private val setContextCreateUseCase: SetContextCreateUseCase,
    private val navigationPathRepository: NavigationPathRepository
) : ViewModel() {
    //private var cachedGuides: List<GuideDomainModel> = emptyList()

    private val _uiStateComposable = MutableStateFlow(PropertiesFilesState())
    val uiStateComposable = _uiStateComposable.asStateFlow()

    private val _eventUI = MutableSharedFlow<CreatingUIState>()
    val eventUI = _eventUI.asSharedFlow()

    private var currentMode: FileFormMode? = null
    /*private val _eventsMessages = MutableSharedFlow<RenameGuideEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()*/

    private val _messages = MutableSharedFlow<CreatingUIState>()
    val messages = _messages.asSharedFlow()

    fun loadIconsFor(mode: FileFormMode) {
        val icons = when (mode) {
            FileFormMode.CreatingFile, is FileFormMode.RenameFile -> {
                listOf(IconType.LIGHTBULB)
            }

            FileFormMode.CreatingFolder, is FileFormMode.RenameFolder -> listOf(
                IconType.ANCHOR_SOLID_FULL,
                IconType.ANGELLIST_BRANDS_SOLID_FULL,
                IconType.BACTERIA_SOLID_FULL
            )
        }

        _uiStateComposable.update { currentState ->
            currentState.copy(
                icons = icons,
                selectedIndex = 0,
                icon = icons.first()
            )
        }
    }

    fun initForm(mode: FileFormMode) {
        this.currentMode = mode
    }

    fun changeIconSelected(position: Int, icon: IconType) {
        _uiStateComposable.update { currentState ->
            currentState.copy(
                selectedIndex = position,
                icon = icon
            )
        }
    }

    fun changeColorSelected(color: Int) {
        _uiStateComposable.update { currentState ->
            currentState.copy(
                color = color.toColorType()
            )
        }
    }

    suspend fun saveMetadata(isDarkTheme: Boolean) {
        val state = uiStateComposable.value

        val icon = state.icons[state.selectedIndex]
        val data = ScreenDataUi(
            name = state.name,
            description = state.description,
            imgFolder = icon,
            color = state.color
        )

        val screenDataDomain = data.toDomain(isDarkTheme)

        saveMetadataUseCase.invoke(screenDataDomain)
    }

    fun fillFields(
        fileName: String,
        description: String,
        icon: IconType? = null,
        color: ColorType? = null
    ) {
        _uiStateComposable.update { currentState ->
            currentState.copy(
                name = fileName,
                description = description,
                oldName = fileName,
                oldDescription = description,
                icon = icon ?: currentState.icon,
                color = color ?: currentState.color,
                selectedIndex = icon?.let { currentState.icons.indexOf(it) }
                    ?: currentState.selectedIndex
            )
        }
    }

    fun renameFile(
        oldName: String,
        newFileName: String,
        newDescription: String
    ) {
        viewModelScope.launch {
            val guideResource = getVersionGuideUseCase(oldName)

            if (guideResource is GuideResource.Error) {
                val errorMessage = when (guideResource.exception) {
                    ReadGuideError.FileNotFound -> "La guía no existe"
                    ReadGuideError.InvalidXmlFormat -> "El archivo XML está dañado"
                    ReadGuideError.EmptyOrCorruptFile -> "Archivo vacío o corrupto"
                    is ReadGuideError.UnknownErrorRead -> "Error al leer la guía"
                }
                emitEvent(Message(errorMessage))
                return@launch
            }

            val guideDomain = (guideResource as GuideResource.Success).data

            when (renameGuideUseCase.invoke(
                oldGuide = guideDomain,
                newGuide = GuideDomainModel(GuideVersion.V2, newFileName, newDescription)
            )) {
                RenamedGuideResult.ImageError ->
                    emitEvent(Message("No se pasaron correctamente todas las imagenes"))

                RenamedGuideResult.RenamedError ->
                    emitEvent(Message("No se ha podido renombrar la guia"))

                RenamedGuideResult.Success -> {
                    emitEvent(Message("Guia renombrada exitosamente"))

                    val xmlGuideV1 = GuideDomainModel(GuideVersion.V1, newFileName, newDescription)

                    when (existXMLGuideV1UseCase.invoke(xmlGuideV1)) {
                        ExistGuideV1Result.Error ->
                            Log.d("RenameGuide", "Error al validar la guia V1")

                        ExistGuideV1Result.ExistGuide -> {
                            when (deleteGuideUseCase.invoke(guideDomainModel = xmlGuideV1)) {
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

                RenamedGuideResult.Error -> emitEvent(Message("Ocurrió un error al abrir la guia"))

                RenamedGuideResult.InvalidFormat -> emitEvent(Message("La guia está dañada"))

                RenamedGuideResult.NotFound -> emitEvent(Message("No se ha encontrado la guia"))

                RenamedGuideResult.UnknownError -> emitEvent(Message("Error desconocido"))
                RenamedGuideResult.GuidePathError ->
                    emitEvent(Message("No fue posible renombrar la guia en la ruta actual"))
            }
        }
    }

    private fun emitEvent(state: CreatingUIState) {
        viewModelScope.launch {
            when (state) {
                CreateFile, RenameFile, CreateFolder, RenameFolder ->
                    _eventUI.emit(state)

                // RenameFolder and Message
                is Message ->
                    _eventUI.emit(state)
            }
        }
    }

    suspend fun fileExist(mode: FileFormMode, name: String): Boolean {
        return when (mode) {
            FileFormMode.CreatingFile,
            is FileFormMode.RenameFile -> isExistFileUseCase.invoke(name = name)

            FileFormMode.CreatingFolder -> isExistFolderUseCase.invoke(name = name)
            is FileFormMode.RenameFolder -> isExistFolderUseCase.invoke(name = name)
        }
    }

    // Esto no se estaba usando pero hay verificar nuevamente esto
    /*fun onContinueProcess(confirmed: Boolean, name: String, description: String) {
        if (!confirmed) return
        // Esto estaba descomentado
        //validateData(name, description)
    }*/

    /*fun uploadCachedGuides() {
        cachedGuides = loadGuidesUseCase.invoke())
    }*/

    suspend fun dataUniqueScreen(): Boolean {
        val state = uiStateComposable.value
        val mode = currentMode ?: run {
            emitEvent(Message("No fue posible procesar los datos"))
            return false
        }

        // Si el nombre no ha cambiado y estamos en modo renombrar, permitimos continuar directamente
        val nameHasChanged = state.name != state.oldName
        if (!nameHasChanged && (mode is FileFormMode.RenameFile || mode is FileFormMode.RenameFolder)) {
            return true
        }

        val existFile = fileExist(mode, state.name)
        if (!existFile) {
            return true
        }

        when (mode) {
            FileFormMode.CreatingFile,
            is FileFormMode.RenameFile -> {
                _uiStateComposable.update { currentState ->
                    currentState.copy(showOverwriteDialogFile = true)
                }
            }

            FileFormMode.CreatingFolder,
            is FileFormMode.RenameFolder -> {
                _uiStateComposable.update { currentState ->
                    currentState.copy(showOverwriteDialogFolder = true)
                }
            }
        }

        return false
    }

    // Función para cuando el usuario pulsa "Confirmar" o "Cancelar"
    /*fun onConfirmAlertDialog(confirmed: Boolean) {
        val state = uiStateComposable
        // Cerramos el diálogo primero
        uiStateComposable = uiStateComposable.copy(showOverwriteDialogFile = false)

        if (confirmed) {
            onContinueProcess()
            //validateData()
        }
    }*/

    fun initWithMode(mode: FileFormMode) {
        if (mode == FileFormMode.CreatingFile || mode == FileFormMode.CreatingFolder) {
            _uiStateComposable.value = PropertiesFilesState()
        }
        loadIconsFor(mode)
        initForm(mode)
    }

    fun onNameChange(newName: String) {
        _uiStateComposable.update { currentState ->
            currentState.copy(
                name = newName
            )
        }
    }

    fun onDescriptionChange(newDesc: String) {

        _uiStateComposable.update { currentState ->
            currentState.copy(
                description = newDesc
            )
        }
    }

    fun processSaveRequest(isDarkTheme: Boolean) {
        viewModelScope.launch {
            _uiStateComposable.update { currentState ->
                currentState.copy(
                    name = currentState.name.trim(),
                    description = currentState.description.trim(),
                    oldName = currentState.oldName.trim(),
                    oldDescription = currentState.oldDescription.trim()
                )
            }

            val dataUniqueScreen = dataUniqueScreen()

            if (dataUniqueScreen) {
                proceedWithSave(isDarkTheme)
            }
        }
    }

    private suspend fun proceedWithSave(isDarkTheme: Boolean) {
        if (validateData()) {
            saveData(isDarkTheme)
        }
    }

    suspend fun saveData(isDarkTheme: Boolean) {
        val state = uiStateComposable.value

        val icon = state.icons[state.selectedIndex]
        val data = ScreenDataUi(
            name = state.name,
            description = state.description,
            imgFolder = icon,
            color = state.color
        )

        when (currentMode) {
            FileFormMode.CreatingFile -> {
                try {
                    val guideDomainModel =
                        GuideDomainModel(GuideVersion.V2, state.name, state.description)
                    setActiveGuideUseCase.invoke(guideDomainModel)
                    setContextCreateUseCase.invoke(GuideContext.Creating(guideDomainModel))
                    emitEvent(CreateFile)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                    emitEvent(Message(e.message ?: "Ocurrió un error inesperado"))
                }
            }

            FileFormMode.CreatingFolder -> {
                try {
                    nextNavigationUseCase.invoke(data.name)
                    val isFolderCreate = createFolder(isDarkTheme, data)
                    if (!isFolderCreate) {
                        emitEvent(Message("No se pudo crear la carpeta"))
                        return
                    }
                    saveMetadata(isDarkTheme)
                    navigationPathRepository.setLastModifiedFolder(data.name)
                    emitEvent(CreateFolder)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                    emitEvent(Message(e.message ?: "Ocurrió un error inesperado"))
                } finally {
                    resetNavigationUseCase.invoke()
                }
            }

            is FileFormMode.RenameFile -> {
                emitEvent(RenameFile)
            }

            is FileFormMode.RenameFolder -> {
                try {
                    val isRenamed = renameFolderUseCase.invoke(
                        oldName = state.oldName,
                        newName = state.name,
                        data = data.toDomain(isDarkTheme)
                    )
                    if (isRenamed) {
                        navigationPathRepository.setLastModifiedFolder(state.name)
                        emitEvent(RenameFolder)
                    } else {
                        emitEvent(Message("No se pudo renombrar la carpeta"))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emitEvent(Message(e.message ?: "Ocurrió un error inesperado"))
                }
            }

            null -> emitEvent(Message("No se pudo crear el archivo"))
        }
    }

    private suspend fun createFolder(isDarkTheme: Boolean, data: ScreenDataUi): Boolean {
        return createFolderUseCase.invoke(data.toDomain(isDarkTheme))
    }

    fun validateData(): Boolean {
        val state = uiStateComposable.value
        val mode = currentMode

        if (mode == null) {
            emitEvent(Message("No se pudo crear el archivo"))
            return false
        }

        return when (val response =
            validateCreateFileUseCase.invoke(state.name, state.description, mode)) {
            is ValidateCreateFileResult.Error -> {
                emitEvent(Message(response.message))
                false
            }

            is ValidateCreateFileResult.Success -> true
        }
    }

    fun dismissOverwriteDialog() {
        _uiStateComposable.update { currentState ->
            currentState.copy(
                showOverwriteDialogFile = false,
                showOverwriteDialogFolder = false
            )
        }
    }

    fun onConfirmCreateFile(isDarkTheme: Boolean) {
        viewModelScope.launch {
            dismissOverwriteDialog()
            proceedWithSave(isDarkTheme)
        }
    }
}