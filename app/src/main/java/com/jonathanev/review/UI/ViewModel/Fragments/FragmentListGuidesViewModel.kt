package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Domain.ChangeGuidePathBuildFileUseCase
import com.jonathanev.review.Domain.DeleteGuideUseCase
import com.jonathanev.review.Domain.GetGuidePosicionUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetVersionUseCase
import com.jonathanev.review.Domain.LoadGuidesUseCase
import com.jonathanev.review.Domain.MoverArchivoUseCase
import com.jonathanev.review.Domain.MoverImagenesUseCase
import com.jonathanev.review.Domain.SetMainPathUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.UI.Utils.toUi
import com.jonathanev.review.data.FolderAction
import com.jonathanev.review.data.mapper.GuideXmlMapper
import com.jonathanev.review.data.model.GuideXmlModel
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.presentation.model.QuestionItemDomain
import com.jonathanev.review.presentation.state.ResponseDomain
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
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider,
    private val loadGuidesUseCase: LoadGuidesUseCase,
    private val getGuidePosicionUseCase: GetGuidePosicionUseCase,
    private val setMainPathUseCase: SetMainPathUseCase,
    private val changeGuidePathBuildFileUseCase: ChangeGuidePathBuildFileUseCase,
    private val deleteGuideUseCase: DeleteGuideUseCase,
    private val moverArchivoUseCase: MoverArchivoUseCase,
    private val moverImagenesUseCase: MoverImagenesUseCase
) : ViewModel() {
    private var cachedGuides: List<GuideXmlModel> = emptyList()
    private val _guides = MutableLiveData<List<GuideUiModel>>()
    val guides: LiveData<List<GuideUiModel>> = _guides

    private val _selectedGuide = MutableLiveData<GuideResultUi>()
    val selectedGuide: LiveData<GuideResultUi> = _selectedGuide

    private val _eventsMessages = MutableSharedFlow<UIStopEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    private var _preguntas: MutableList<QuestionItemDomain> = mutableListOf()
    val preguntas: MutableList<QuestionItemDomain> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemDomain> = mutableListOf()
    val respuestas: MutableList<QuestionItemDomain> get() = _respuestas

    fun getAllGuides() {
        val xmlGuides = loadGuidesUseCase.invoke()
        cachedGuides = xmlGuides
        val guidesUi = xmlGuides.map { guide -> GuideXmlMapper.toUi(guide) }
        _guides.postValue(guidesUi)
    }

    fun getGuideSelected(position: Int) {
        val guidesDomain = cachedGuides.map { guide -> GuideXmlMapper.toDomain(guide) }

        val resultDomain = getGuidePosicionUseCase.invoke(position, guidesDomain)
        val resultToUi = resultDomain.toUi()

        _selectedGuide.value = resultToUi
    }

    fun setMainPath() {
        setMainPathUseCase.invoke()
        getAllGuides()
    }

    fun changeFilePath(nameGuide: String) {
        val newPath = changeGuidePathBuildFileUseCase.invoke(nameGuide)
        fileRepository.setCurrentPath(newPath)
        getAllGuides()
    }

    fun deleteFiles(nameGuide: String) {
        val currentPath = File(fileRepository.getCurrentPath())
        val currentGuide = filePathsProvider.buildFile(currentPath, nameGuide)

        getObtenerDatosXML(currentGuide)

        val listImages = (preguntas + respuestas)
            .flatMap { it.content }
            .filterIsInstance<QuestionContentDomain.Image>()

        val message = deleteGuideUseCase.invoke(currentGuide, listImages)

        viewModelScope.launch {
            if (message is UIStopEvent.DeleteGuideSuccess) {
                fileRepository.setCurrentPath(filePathsProvider.fileGuides.path)
            }

            _eventsMessages.emit(
                message
            )
        }
    }

    private fun getObtenerDatosXML(currentGuide: File) {
        if (respuestas.isEmpty()) {
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = currentGuide.path)

            _preguntas =
                datos.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }.toMutableList()
            _respuestas =
                datos.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toMutableList()
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

            if (isSuccessXML.first) {
                val version = getVersionUseCase.invoke(isSuccessXML.second)
                moverImagenesUseCase.invoke(
                    version,
                    mode.pathFile,
                    isSuccessXML.second,
                    preguntas,
                    respuestas
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
