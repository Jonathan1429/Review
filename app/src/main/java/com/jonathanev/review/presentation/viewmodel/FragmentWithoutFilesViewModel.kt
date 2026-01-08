package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GenerateTextColorRangesUseCase
import com.jonathanev.review.domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.domain.GetVersionUseCase
import com.jonathanev.review.domain.MoverArchivoUseCase
import com.jonathanev.review.domain.MoverImagenesUseCase
import com.jonathanev.review.domain.SetMainPathUseCase
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.presentation.event.UIMovingEvent
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
class FragmentWithoutFilesViewModel @Inject constructor(
    private val pathProvider: PathProvider,
    private val filePathsProvider: FilePathsProvider,
    private val moverArchivoUseCase: MoverArchivoUseCase,
    private val getVersionUseCase: GetVersionUseCase,
    private val moverImagenesUseCase: MoverImagenesUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val setMainPathUseCase: SetMainPathUseCase,
    private val generateTextColorRangesUseCase: GenerateTextColorRangesUseCase,
) : ViewModel() {
    private val _eventsMovingFiles = MutableSharedFlow<UIMovingEvent>()
    val eventsMovingFiles = _eventsMovingFiles.asSharedFlow()

    private var _preguntas: MutableList<QuestionItemUi> = mutableListOf()
    val preguntas: List<QuestionItemUi> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemUi> = mutableListOf()
    val respuestas: List<QuestionItemUi> get() = _respuestas

    fun setMainPath() {
        val initialPath = filePathsProvider.fileGuides
        //pathProvider.setCurrentPath(initialPath.path)

        //getFolders()
    }

    /*private fun getFolders() {
        getAllFoldersUseCase.invoke()
    }*/

    fun movingFiles(mode: FolderAction) {
        /*if (mode is FolderAction.MovingFile) {
            val isSuccessXML = moverArchivoUseCase.invoke(mode.pathFile)

            getObtenerDatosXML(isSuccessXML.second)
            val questionsDomain = preguntas.map { it.toDomain() }
            val answersDomain = respuestas.map { it.toDomain() }

            if (isSuccessXML.first) {
                val version = getVersionUseCase.invoke(nameGuide)
                moverImagenesUseCase.invoke(
                    version,
                    mode.pathFile,
                    isSuccessXML.second,
                    questionsDomain,
                    answersDomain
                )
            }

            setMainPathUseCase.invoke()
        }*/
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