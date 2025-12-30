package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.data.FolderAction
import com.jonathanev.review.data.Model.GuideModel
import com.jonathanev.review.data.Model.GuideResult
import com.jonathanev.review.data.Model.prueba.AnswerState
import com.jonathanev.review.data.Model.prueba.QuestionContent
import com.jonathanev.review.data.Model.prueba.QuestionItem
import com.jonathanev.review.data.Model.prueba.UIMovingEvent
import com.jonathanev.review.data.Model.prueba.UIStopEvent
import com.jonathanev.review.data.provider.FilePathsProvider
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
    private var cachedGuides: List<GuideModel> = emptyList()
    private val _guides = MutableLiveData<List<GuideModel>>()
    val guides: LiveData<List<GuideModel>> = _guides

    private val _eventsMessages = MutableSharedFlow<UIStopEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    private var _preguntas: MutableList<QuestionItem> = mutableListOf()
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private var _respuestas: MutableList<QuestionItem> = mutableListOf()
    val respuestas: MutableList<QuestionItem> get() = _respuestas

    fun getAllGuides() {
        cachedGuides = loadGuidesUseCase.invoke()
        _guides.postValue(cachedGuides)
    }

    fun getGuideSelected(position: Int): GuideResult {
        return getGuidePosicionUseCase(position, cachedGuides)
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
            .filterIsInstance<QuestionContent.Image>()

        val message = deleteGuideUseCase.invoke(currentGuide, listImages)

        viewModelScope.launch {
            if (message is UIStopEvent.DeleteGuideSuccess){
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

            _preguntas = datos.map { it.question }.toMutableList()
            _respuestas = datos.mapNotNull { (it.answer as? AnswerState.Filled )?.item }.toMutableList()
        }
    }

    fun changeFilePathToMain() {
        setMainPathUseCase.invoke()
    }

    fun getFilePath(nameGuide: String): File {
        return File(changeGuidePathBuildFileUseCase.invoke(nameGuide))
    }

    fun movingFiles(mode: FolderAction) {
        if (mode is FolderAction.MovingFile){
            val isSuccessXML = moverArchivoUseCase.invoke(mode.pathFile)

            getObtenerDatosXML(isSuccessXML.second)

            if (isSuccessXML.first){
                val version = getVersionUseCase.invoke(isSuccessXML.second)
                moverImagenesUseCase.invoke(version, mode.pathFile, isSuccessXML.second, preguntas, respuestas)
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

    fun moveFileSuccess(){
        eventMovingFile("Se ha movido la guia correctamente")
    }
}
