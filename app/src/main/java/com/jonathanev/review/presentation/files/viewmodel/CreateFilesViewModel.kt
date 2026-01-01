package com.jonathanev.review.presentation.files.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.CheckNameConflictUseCase
import com.jonathanev.review.domain.CreateFolderUseCase
import com.jonathanev.review.domain.GenerateTextColorRangesUseCase
import com.jonathanev.review.domain.GetAttributesGuideUseCase
import com.jonathanev.review.domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.domain.ReubicarImagenesUseCase
import com.jonathanev.review.domain.SetAttributesUseCase
import com.jonathanev.review.domain.ValidateCreateFileUseCase
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.presentation.files.model.GuideUiModel
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
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreateFilesViewModel @Inject constructor(
    private val pathProvider: PathProvider,
    private val filePathsProvider: FilePathsProvider,
    private val createFolderUseCase: CreateFolderUseCase,
    private val getAttributesGuideUseCase: GetAttributesGuideUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val reubicarImagenesUseCase: ReubicarImagenesUseCase,
    private val setAttributesXMLUseCase: SetAttributesUseCase,
    private val validateCreateFileUseCase: ValidateCreateFileUseCase,
    private val checkNameConflictUseCase: CheckNameConflictUseCase,
    private val generateTextColorRangesUseCase: GenerateTextColorRangesUseCase
) : ViewModel() {
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

            FolderAction.RenamingFile -> listOf(IconType.LIGHTBULB)

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
        val currentPath = File(pathProvider.getCurrentPath())
        val folderPath = File(currentPath, data.name)

        if (!folderPath.exists()) {
            folderPath.mkdir()
            createScreenMetadata(data, folderPath)
        }
    }

    private fun createScreenMetadata(data: ScreenData, dir: File) {
        createFolderUseCase.invoke(data, dir)
    }

    fun getCurrentPath() = pathProvider.getCurrentPath()

    fun fillFields(): GuideUiModel {
        val currentPath = File(getCurrentPath())
        val guideDomain = getAttributesGuideUseCase.invoke(currentPath)
        return guideDomain.toUi()
    }

    fun getObtenerDatosXML() {
        if (respuestas.isEmpty()) {
            //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
            val datos = getObtenerDatosXMLUseCase.invoke()

            val tempQuestions =
                datos.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }.toList()
            val tempAnswers =
                datos.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toList()

            val questionsDomain = generateTextColorRangesUseCase.invoke(tempQuestions)
            val answersDomain = generateTextColorRangesUseCase.invoke(tempAnswers)
            _preguntas = questionsDomain.map { it.toUi() }.toMutableList()
            _respuestas = answersDomain.map { it.toUi() }.toMutableList()
        }
    }

    fun renameFile(fileName: String, description: String) {
        getObtenerDatosXML()

        val currentPath = File(getCurrentPath())

        val questionsDomain = preguntas.map { it.toDomain() }
        val answersDomain = preguntas.map { it.toDomain() }

        reubicarImagenesUseCase.invoke(currentPath, fileName, questionsDomain, answersDomain)
        val isUpdated =
            setAttributesXMLUseCase.invoke(
                currentPath,
                fileName,
                description,
                questionsDomain,
                answersDomain
            )

        if (isUpdated) {
            viewModelScope.launch {
                _eventsMessages.emit(
                    UIStopEvent.GuideRenamedSuccess("Se ha renombrado el archivo con exito")
                )

                pathProvider.setCurrentPath(filePathsProvider.fileGuides.path)
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
        val currentPath = File(pathProvider.getCurrentPath())
        val beforePath = filePathsProvider.beforePath(currentPath)
        Log.i("Path: ", beforePath.path)
        pathProvider.setCurrentPath(beforePath.path)
    }
}