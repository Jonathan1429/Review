package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.ChangeGuidePathBuildFileUseCase
import com.jonathanev.review.domain.DeleteGuideUseCase
import com.jonathanev.review.domain.GenerateTextColorRangesUseCase
import com.jonathanev.review.domain.GetGuidePosicionUseCase
import com.jonathanev.review.domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.domain.GetVersionUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.MoverArchivoUseCase
import com.jonathanev.review.domain.MoverImagenesUseCase
import com.jonathanev.review.domain.SetMainPathUseCase
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.files.model.GuideResultUi
import com.jonathanev.review.presentation.files.model.GuideUiModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.presentation.mapper.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragmentListGuidesViewModel @Inject constructor(
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getVersionUseCase: GetVersionUseCase,
    private val pathProvider: PathProvider,
    private val filePathsProvider: FilePathsProvider,
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val getGuidePosicionUseCase: GetGuidePosicionUseCase,
    private val setMainPathUseCase: SetMainPathUseCase,
    private val changeGuidePathBuildFileUseCase: ChangeGuidePathBuildFileUseCase,
    private val deleteGuideUseCase: DeleteGuideUseCase,
    private val moverArchivoUseCase: MoverArchivoUseCase,
    private val moverImagenesUseCase: MoverImagenesUseCase,
    private val generateTextColorRangesUseCase: GenerateTextColorRangesUseCase
) : ViewModel() {
    private var cachedGuides: List<GuideDomainModel> = emptyList()
    private val _guides = MutableLiveData<List<GuideUiModel>>()
    val guides: LiveData<List<GuideUiModel>> = _guides

    private val _selectedGuide = MutableLiveData<GuideResultUi>()
    val selectedGuide: LiveData<GuideResultUi> = _selectedGuide

    private val _eventsMessages = MutableSharedFlow<UIStopEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    private var _preguntas: MutableList<QuestionItemUi> = mutableListOf()
    val preguntas: MutableList<QuestionItemUi> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemUi> = mutableListOf()
    val respuestas: MutableList<QuestionItemUi> get() = _respuestas

    fun getAllGuides() {
        val xmlGuides = loadGuidesUseCase.invoke()
        cachedGuides = xmlGuides
        val guidesUi = xmlGuides.map { guide -> guide.toUi() }
        _guides.postValue(guidesUi)
    }

    fun getGuideSelected(position: Int) {
        val resultDomain = getGuidePosicionUseCase.invoke(position, cachedGuides)
        val resultToUi = resultDomain.toUi()

        _selectedGuide.value = resultToUi
    }

    fun setMainPath() {
        setMainPathUseCase.invoke()
        getAllGuides()
    }

    fun changeFilePath(nameGuide: String) {
        val newPath = changeGuidePathBuildFileUseCase.invoke(nameGuide)
        pathProvider.setCurrentPath(newPath)
        getAllGuides()
    }

    fun deleteFiles(nameGuide: String) {
        val currentPath = File(pathProvider.getCurrentPath())
        val currentGuide = filePathsProvider.buildFile(currentPath, nameGuide)

        getObtenerDatosXML(currentGuide)

        val listImages = (preguntas + respuestas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()

        val message = deleteGuideUseCase.invoke(currentGuide, listImages)

        viewModelScope.launch {
            if (message is UIStopEvent.DeleteGuideSuccess) {
                pathProvider.setCurrentPath(filePathsProvider.fileGuides.path)
            }

            _eventsMessages.emit(
                message
            )
        }
    }

    private fun getObtenerDatosXML(currentGuide: File) {
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

    fun changeFilePathToMain() {
        setMainPathUseCase.invoke()
    }

    fun getFilePath(nameGuide: String): File {
        return File(changeGuidePathBuildFileUseCase.invoke(nameGuide))
    }

    fun movingFiles(mode: FolderAction) {
        if (mode is FolderAction.MovingFile) {
            val isSuccessXML = moverArchivoUseCase.invoke(mode.pathFile)

            getObtenerDatosXML(isSuccessXML.second)

            val questionsItemDomain = preguntas.map { it.toDomain() }
            val answersItemDomain = respuestas.map { it.toDomain() }

            if (isSuccessXML.first) {
                val version = getVersionUseCase.invoke(isSuccessXML.second)
                moverImagenesUseCase.invoke(
                    version,
                    mode.pathFile,
                    isSuccessXML.second,
                    questionsItemDomain,
                    answersItemDomain
                )
            }

            setMainPathUseCase.invoke()
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

    fun moveFileSuccess() {
        eventMovingFile("Se ha movido la guia correctamente")
    }
}
