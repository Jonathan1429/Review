package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.data.FolderAction
import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.Model.prueba.AnswerState
import com.jonathanev.review.data.Model.prueba.QuestionItem
import com.jonathanev.review.data.Model.prueba.UIMovingEvent
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetVersionUseCase
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
class FragmentWithoutFilesViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val guiaRepository: GuiaRepository,
    private val filePathsProvider: FilePathsProvider,
    private val moverArchivoUseCase: MoverArchivoUseCase,
    private val getVersionUseCase: GetVersionUseCase,
    private val moverImagenesUseCase: MoverImagenesUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val setMainPathUseCase: SetMainPathUseCase
): ViewModel(){
    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    private var _preguntas: MutableList<QuestionItem> = mutableListOf()
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private var _respuestas: MutableList<QuestionItem> = mutableListOf()
    val respuestas: MutableList<QuestionItem> get() = _respuestas

    fun setMainPath(){
        val initialPath = filePathsProvider.fileGuides
        fileRepository.setCurrentPath(initialPath.path)

        setGuidesMainPath()
    }

    private fun setGuidesMainPath() {
        val initialPath = filePathsProvider.fileGuides
        guiaRepository.getFolders(initialPath)
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

    private fun getObtenerDatosXML(currentGuide: File) {
        if (respuestas.isEmpty()) {
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = currentGuide.path)

            _preguntas = datos.map { it.question }.toMutableList()
            _respuestas = datos.mapNotNull { (it.answer as? AnswerState.Filled )?.item }.toMutableList()
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