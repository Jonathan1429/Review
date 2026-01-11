package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.BackPathUseCase
import com.jonathanev.review.domain.DeleteGuideUseCase
import com.jonathanev.review.domain.GetCurrentPathFilesUseCase
import com.jonathanev.review.domain.GetGuidePosicionUseCase
import com.jonathanev.review.domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.domain.LoadGuidesUseCase
import com.jonathanev.review.domain.MoveGuideUseCase
import com.jonathanev.review.domain.SetMainPathUseCase
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideResultDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.files.model.GuideResultUi
import com.jonathanev.review.presentation.files.model.GuideUiModel
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FragmentListGuidesViewModel @Inject constructor(
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val getGuidePosicionUseCase: GetGuidePosicionUseCase,
    private val setMainPathUseCase: SetMainPathUseCase,
    private val backPathUseCase: BackPathUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val deleteGuideUseCase: DeleteGuideUseCase,
    private val getCurrentPathFilesUseCase: GetCurrentPathFilesUseCase,
    private val moveGuideUseCase: MoveGuideUseCase
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
    val preguntas: List<QuestionItemUi> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemUi> = mutableListOf()
    val respuestas: List<QuestionItemUi> get() = _respuestas

    fun getAllGuides() {
        cachedGuides = loadGuidesUseCase.invoke()
        val guidesUi = cachedGuides.map { guide -> guide.toUi() }
        _guides.postValue(guidesUi)
    }

    fun getGuideSelected(position: Int): GuideResultUi {
        val resultDomain = getGuidePosicionUseCase.invoke(position, cachedGuides)
        return resultDomain.toUi()
        //_selectedGuide.value = resultToUi
    }

    /*fun setMainPath() {
        setMainPathUseCase.invoke()
        getAllGuides(folderId)
    }*/

    fun deleteFiles(nameGuide: String) {
        val guideDomainModel = cachedGuides.find { it.nameGuide == nameGuide }
        val datos = getObtenerDatosXMLUseCase.invoke(guideDomainModel)

        val tempQuestions =
            datos.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }.toList()
        val tempAnswers =
            datos.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toList()

        val listImages = (tempQuestions + tempAnswers).flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()
        /*val currentGuide = changeGuidePathBuildFileUseCase.invoke(nameGuide)
        //getObtenerDatosXML(File(currentGuide))

        val listImages = (preguntas + respuestas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()*/

        val message = deleteGuideUseCase.invoke(guideDomainModel!!, listImages)

        viewModelScope.launch {
            _eventsMessages.emit(
                message
            )
        }
    }

    private fun getObtenerDatosXML(currentGuide: File) {
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

    fun changeFilePathToMain() {
        setMainPathUseCase.invoke()
    }

    fun getPaths(): Pair<File, File> {
        return getCurrentPathFilesUseCase.invoke()
    }

    fun movingFiles(mode: FolderAction): Boolean {
        if (mode is FolderAction.MovingFile){
            val guideDomainModel = cachedGuides.find { it.nameGuide == mode.guideDomain.nameGuide }
            if (guideDomainModel != null) {
                viewModelScope.launch {
                    _eventsMovingFiles.emit(
                        UIMovingEvent.ExistFile
                    )
                }

                return false
            }

            return onContinueProcess(confirmed = true, mode = mode)
        }
        return false
    }

    fun onContinueProcess(confirmed: Boolean, mode: FolderAction): Boolean {
        if (!confirmed) return false

        return if (mode is FolderAction.MovingFile) {
            moveGuideUseCase.invoke(mode)
        } else {
            false
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

    fun back() {
        backPathUseCase.invoke()
    }

    fun resetPaths() {
        setMainPathUseCase.invoke()
    }

    fun setMainPath() {
        setMainPathUseCase.invoke()
    }

    fun getGuideDomain(position: Int): GuideResultDomain {
        return getGuidePosicionUseCase.invoke(position, cachedGuides)
    }
}
