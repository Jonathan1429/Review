package com.jonathanev.review.UI.ViewModel.Fragments

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.Model.ScreenData
import com.jonathanev.review.Data.Model.prueba.AnswerState
import com.jonathanev.review.Data.Model.prueba.PreviewState
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.UICreatingFile
import com.jonathanev.review.Data.Model.prueba.UIStopEvent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Domain.CreateFolderUseCase
import com.jonathanev.review.Domain.CheckNameConflictUseCase
import com.jonathanev.review.Domain.GetAttributesGuideUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.ReubicarImagenesUseCase
import com.jonathanev.review.Domain.SetAttributesUseCase
import com.jonathanev.review.Domain.ValidateCreateFileUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.R
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

    private val _messages = MutableSharedFlow<UICreatingFile>()
    val messages = _messages.asSharedFlow()

    private var _preguntas = mutableListOf<QuestionItem>()
    val preguntas: List<QuestionItem> get() = _preguntas

    private var _respuestas = mutableListOf<QuestionItem>()
    val respuestas: List<QuestionItem> get() = _respuestas

    fun loadIconsFor(action: FolderAction) {
        val icons = when (action) {
            FolderAction.CREATING_FILE -> listOf(R.drawable.ic_lightbulb_solid_full)
            FolderAction.CREATING_FOLDER -> listOf(
                R.drawable.ic_anchor_solid_full,
                R.drawable.ic_angellist_brands_solid_full,
                R.drawable.ic_bacteria_solid_full
            )

            FolderAction.RENAMING_FILE -> listOf(R.drawable.ic_lightbulb_solid_full)
            FolderAction.RENAMING_FOLDER -> listOf(
                R.drawable.ic_anchor_solid_full,
                R.drawable.ic_angellist_brands_solid_full,
                R.drawable.ic_bacteria_solid_full
            )

            FolderAction.NONE -> emptyList()
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

    fun fillFields(): GuideModel {
        val currentPath = File(getCurrentPath())
        return getAttributesGuideUseCase.invoke(currentPath)
    }

    fun getObtenerDatosXML() {
        if (respuestas.isEmpty()) {
            //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas = datos.map { it.question }.toMutableList()
            _respuestas =
                datos.mapNotNull { (it.answer as? AnswerState.Filled)?.item }.toMutableList()
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
}