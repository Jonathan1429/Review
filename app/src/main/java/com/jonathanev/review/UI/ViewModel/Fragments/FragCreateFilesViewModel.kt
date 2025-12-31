package com.jonathanev.review.UI.ViewModel.Fragments

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.data.FolderAction
import com.jonathanev.review.presentation.model.ScreenData
import com.jonathanev.review.presentation.state.ResponseDomain
import com.jonathanev.review.presentation.state.PreviewState
import com.jonathanev.review.presentation.model.QuestionItemDomain
import com.jonathanev.review.presentation.state.CreatingFileUiState
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Domain.CreateFolderUseCase
import com.jonathanev.review.Domain.CheckNameConflictUseCase
import com.jonathanev.review.Domain.GetAttributesGuideUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.ReubicarImagenesUseCase
import com.jonathanev.review.Domain.SetAttributesUseCase
import com.jonathanev.review.Domain.ValidateCreateFileUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.R
import com.jonathanev.review.UI.Utils.toUi
import com.jonathanev.review.data.mapper.GuideXmlMapper
import com.jonathanev.review.presentation.model.GuideUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragCreateFilesViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider,
    private val createFolderUseCase: CreateFolderUseCase,
    private val getAttributesGuideUseCase: GetAttributesGuideUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val reubicarImagenesUseCase: ReubicarImagenesUseCase,
    private val setAttributesXMLUseCase: SetAttributesUseCase,
    private val validateCreateFileUseCase: ValidateCreateFileUseCase,
    private val checkNameConflictUseCase: CheckNameConflictUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PreviewState())
    val uiState = _uiState.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<UIStopEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _messages = MutableSharedFlow<CreatingFileUiState>()
    val messages = _messages.asSharedFlow()

    private var _preguntas = mutableListOf<QuestionItemDomain>()
    val preguntas: List<QuestionItemDomain> get() = _preguntas

    private var _respuestas = mutableListOf<QuestionItemDomain>()
    val respuestas: List<QuestionItemDomain> get() = _respuestas

    fun loadIconsFor(action: FolderAction) {
        val icons = when (action) {
            FolderAction.CreatingFile -> listOf(R.drawable.ic_lightbulb_solid_full)
            FolderAction.CreatingFolder -> listOf(
                R.drawable.ic_anchor_solid_full,
                R.drawable.ic_angellist_brands_solid_full,
                R.drawable.ic_bacteria_solid_full
            )

            FolderAction.RenamingFile -> listOf(R.drawable.ic_lightbulb_solid_full)
            FolderAction.RenamingFolder -> listOf(
                R.drawable.ic_anchor_solid_full,
                R.drawable.ic_angellist_brands_solid_full,
                R.drawable.ic_bacteria_solid_full
            )

            FolderAction.None -> emptyList()
            is FolderAction.MovingFile -> emptyList()
        }

        _uiState.value = _uiState.value.copy(
            icons = icons,
            selectedIndex = 0,
            icon = icons.first(),
            //icon = icons.firstOrNull(),
            //name = "Nuevo archivo",
            color = Color.GRAY
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
        _uiState.value = _uiState.value.copy(color = color)
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
        val currentPath = File(fileRepository.getCurrentPath())
        val folderPath = File(currentPath, data.name)

        if (!folderPath.exists()) {
            folderPath.mkdir()
            createScreenMetadata(data, folderPath)
        }
    }

    private fun createScreenMetadata(data: ScreenData, dir: File) {
        createFolderUseCase.invoke(data, dir)
    }

    fun getCurrentPath() = fileRepository.getCurrentPath()

    fun fillFields(): GuideUiModel {
        val currentPath = File(getCurrentPath())
        val guideDomain = getAttributesGuideUseCase.invoke(currentPath)
        return guideDomain.toUi()
    }

    fun getObtenerDatosXML() {
        if (respuestas.isEmpty()) {
            //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas =
                datos.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }.toMutableList()
            _respuestas =
                datos.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toMutableList()
        }
    }

    fun renameFile(fileName: String, description: String) {
        getObtenerDatosXML()

        val currentPath = File(getCurrentPath())

        reubicarImagenesUseCase.invoke(currentPath, fileName, preguntas, respuestas)
        val isUpdated =
            setAttributesXMLUseCase.invoke(
                currentPath,
                fileName,
                description,
                preguntas,
                respuestas
            )

        if (isUpdated) {
            viewModelScope.launch {
                _eventsMessages.emit(
                    UIStopEvent.GuideRenamedSuccess("Se ha renombrado el archivo con exito")
                )

                fileRepository.setCurrentPath(filePathsProvider.fileGuides.path)
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
        return checkNameConflictUseCase.invoke(mode, name)
    }

    fun onContinueProcess(confirmed: Boolean, name: String, description: String) {
        if (!confirmed) return

        processScreenData(name, description)
    }

    fun beforePath() {
        val currentPath = File(fileRepository.getCurrentPath())
        val beforePath = filePathsProvider.beforePath(currentPath)
        Log.i("Path: ", beforePath.path)
        fileRepository.setCurrentPath(beforePath.path)
    }
}