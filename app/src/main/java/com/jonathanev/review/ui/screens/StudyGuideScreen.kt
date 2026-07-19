package com.jonathanev.review.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.AssetCarouselViewer
import com.jonathanev.review.ui.components.CustomAlertDialog
import com.jonathanev.review.ui.components.CustomTopBar
import com.jonathanev.review.ui.components.FilterTypeItem
import com.jonathanev.review.ui.components.QASelectType
import com.jonathanev.review.ui.components.ShowDeletePopUp
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.model.QAType
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.StudyGuideScreenProv
import com.jonathanev.review.ui.preview.providers.StudyGuideScreenProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import kotlinx.coroutines.launch

@DevicePreviews
@Composable
fun PreviewStudyGuideScreen(
    @PreviewParameter(StudyGuideScreenProvider::class) data: StudyGuideScreenProv
) {
    ReviewTheme {
        FillingGuideScreen(
            typeSelected = data.typeSelected,
            typeForSelected = data.typeForSelected,
            mediaSelected = data.mediaSelected,
            mediaForSelected = data.mediaForSelected,
            actualQuestion = data.actualQuestion,
            totalQuestions = data.totalQuestions,
            listTypeMedia = data.listTypeMedia,
            guideMode = data.guideMode,
            showDialogDeleteQuestion = data.showDialogDeleteQuestion,
            showDialogRepeatGuide = data.showDialogRepeatGuide,
            onDissmissDialogRepeatGuide = {},
            onConfirmDialogRepeatGuide = {},
            onContinueDialogDeleteQuestionClick = {},
            onBackQuestionClick = {},
            onNextQuestionClick = {},
            onDeleteQuestionClick = { },
            onTypeClicked = {},
            onFilterClicked = {},
            onAssetClick = {},
            onAddAssetClick = {},
            onDeleteItemClick = { _, _ -> },
            onAddQuestion = {},
        ) {}
    }
}

@Composable
fun FillingGuideRoute(
    viewModel: SharedFragmentCreateFileViewModel,
    guideMode: GuideMode,
    relativeGuidePath: RelativeGuidePath,
    onAssetClick: (QuestionContentUi) -> Unit,
    onAddAssetClick: (ContentType) -> Unit,
    onActionGuideNone: () -> Unit,
    onCloseGuide: () -> Unit
) {
    var typeSelected by rememberSaveable { mutableStateOf(QAType.QUESTION) }
    val typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER)
    var mediaSelected by rememberSaveable { mutableStateOf(ContentType.TEXT) }
    val mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE)
    var showDialogDeleteQuestion by remember { mutableStateOf(false) }
    var showDialogRepeatGuide by remember { mutableStateOf(false) }

    val totalQuestions = viewModel.uiState.collectAsStateWithLifecycle().value.preguntas.size
    val actualQuestion = viewModel.uiState.collectAsStateWithLifecycle().value.contadorPregunta + 1
    val listTypeMedia = if (typeSelected == QAType.QUESTION) {
        viewModel.textList.collectAsStateWithLifecycle().value
    } else {
        viewModel.imageList.collectAsStateWithLifecycle().value
    }
    val coroutineScope = rememberCoroutineScope()
    var restartGuide by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(restartGuide) {
        when (guideMode) {
            is GuideMode.Create -> {
                viewModel.initUIState()
            }

            is GuideMode.Edit -> {
                viewModel.getObtenerDatosXML(
                    posQuestion = guideMode.posQuestion,
                    nameGuide = guideMode.nameGuide,
                    relativeGuidePath = relativeGuidePath
                )
            }

            is GuideMode.Review -> {
                viewModel.getObtenerDatosXML(
                    posQuestion = guideMode.posQuestion,
                    nameGuide = guideMode.nameGuide,
                    relativeGuidePath = relativeGuidePath
                )
            }
        }
    }

    FillingGuideScreen(
        typeSelected = typeSelected,
        typeForSelected = typeForSelected,
        mediaSelected = mediaSelected,
        mediaForSelected = mediaForSelected,
        actualQuestion = actualQuestion,
        totalQuestions = totalQuestions,
        listTypeMedia = listTypeMedia,
        guideMode = guideMode,
        showDialogDeleteQuestion = showDialogDeleteQuestion,
        showDialogRepeatGuide = showDialogRepeatGuide,
        onDissmissDialogRepeatGuide = { showDialogRepeatGuide = false },
        onConfirmDialogRepeatGuide = {
            showDialogRepeatGuide = false
            viewModel.initUIState()
            restartGuide++
        },
        onContinueDialogDeleteQuestionClick = { isChecked ->
            showDialogDeleteQuestion = false
            if (isChecked) {
                viewModel.saveDontAskDelete()
            }
        },
        onBackQuestionClick = {
            viewModel.previousQuestion()
        },
        onNextQuestionClick = {
            if (actualQuestion == totalQuestions) {
                showDialogRepeatGuide = true
            } else {
                viewModel.nextQuestion()
            }
        },
        onDeleteQuestionClick = {
            coroutineScope.launch {
                val dontAskQuestion = viewModel.getDontAskDeleteOnce()
                if (dontAskQuestion) {
                    viewModel.deleteQuesAns()
                } else {
                    showDialogDeleteQuestion = true
                }
            }
        },
        onDeleteItemClick = { typeContent, positionItem ->
            when (typeContent) {
                is QuestionContentUi.Image -> {
                    viewModel.deleteImage(positionItem)
                }

                QuestionContentUi.None -> onActionGuideNone()
                is QuestionContentUi.Text -> {
                    viewModel.deleteText(positionItem)
                }
            }
        },
        onTypeClicked = { typeClicked ->
            typeSelected = typeClicked
        },
        onFilterClicked = { filterClicked ->
            mediaSelected = filterClicked
        },
        onAssetClick = { typeContent -> onAssetClick(typeContent) },
        onAddAssetClick = { onAddAssetClick(mediaSelected) },
        onAddQuestion = {
            viewModel.addNewQuestion()
        },
        onCloseGuide = {
            when (guideMode) {
                is GuideMode.Create -> {
                    viewModel.saveGuide(
                        nameGuide = guideMode.nameGuide,
                        description = guideMode.description,
                        relativeGuidePath = relativeGuidePath,
                        mode = SaveGuideMode.Create
                    )
                    onCloseGuide()
                }

                is GuideMode.Edit -> {
                    viewModel.saveGuide(
                        nameGuide = guideMode.nameGuide,
                        description = guideMode.description,
                        relativeGuidePath = relativeGuidePath,
                        mode = SaveGuideMode.Update
                    )
                    onCloseGuide()
                }

                is GuideMode.Review -> {
                    onCloseGuide()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillingGuideScreen(
    typeSelected: QAType,
    typeForSelected: List<QAType>,
    mediaSelected: ContentType,
    mediaForSelected: List<ContentType>,
    actualQuestion: Int,
    totalQuestions: Int,
    listTypeMedia: List<QuestionContentUi>,
    guideMode: GuideMode,
    showDialogDeleteQuestion: Boolean,
    showDialogRepeatGuide: Boolean,
    onDissmissDialogRepeatGuide: () -> Unit,
    onConfirmDialogRepeatGuide: () -> Unit,
    onContinueDialogDeleteQuestionClick: (Boolean) -> Unit,
    onBackQuestionClick: () -> Unit,
    onNextQuestionClick: () -> Unit,
    onDeleteQuestionClick: () -> Unit,
    onTypeClicked: (QAType) -> Unit,
    onFilterClicked: (ContentType) -> Unit,
    onAssetClick: (QuestionContentUi) -> Unit,
    onDeleteItemClick: (typeContent: QuestionContentUi, positionItem: Int) -> Unit,
    onAddAssetClick: () -> Unit,
    onAddQuestion: () -> Unit,
    onCloseGuide: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        floatingActionButton = {
            FloatingActionButtons(
                guideMode = guideMode,
                onAddQuestion = onAddQuestion,
                onCloseGuide = onCloseGuide
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            CustomTopBar(
                actualQuestion = actualQuestion,
                totalQuestions = totalQuestions,
                guideMode = guideMode,
                onDeleteQuestionClick = onDeleteQuestionClick,
                onBackQuestionClick = onBackQuestionClick,
                onNextQuestionClick = onNextQuestionClick
            )
            QASelectType(typeForSelected, typeSelected, onTypeClicked = { typeClicked ->
                onTypeClicked(typeClicked)
            })
            FilterTypeItem(mediaForSelected, mediaSelected, onFilterClicked = { filterClicked ->
                onFilterClicked(filterClicked)
            })

            AssetCarouselViewer(
                assets = listTypeMedia,
                mediaForSelected = mediaSelected,
                guideMode = guideMode,
                onAddAssetClick = onAddAssetClick,
                onDeleteItemClick = { typeContent, positionItem ->
                    onDeleteItemClick(
                        typeContent,
                        positionItem
                    )
                },
                onAssetClick = { typeContent -> onAssetClick(typeContent) }
            )
        }

        if (showDialogDeleteQuestion) {
            Dialog(onDismissRequest = {}) {
                Box(
                    Modifier.fillMaxSize()
                ) {
                    ShowDeletePopUp(
                        modifier = Modifier.align(Alignment.Center),
                        onContinueClick = { isChecked ->
                            onContinueDialogDeleteQuestionClick(isChecked)
                        },
                    )
                }
            }
        }

        if (showDialogRepeatGuide) {
            Dialog(onDismissRequest = {}) {
                Box(
                    Modifier.fillMaxSize()
                ) {
                    CustomAlertDialog(
                        modifier = Modifier.align(Alignment.Center),
                        onDismissRequest = onDissmissDialogRepeatGuide,
                        onConfirm = onConfirmDialogRepeatGuide,
                        title = stringResource(R.string.lblTitleRepeatGuide),
                        message = stringResource(R.string.lblDescriptionRepeatGuide)
                    )
                }
            }

        }
    }
}

@Composable
private fun FloatingActionButtons(
    guideMode: GuideMode,
    onAddQuestion: () -> Unit,
    onCloseGuide: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (guideMode !is GuideMode.Review) {
            FloatingActionButton(
                onClick = onAddQuestion,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                NewQuestionIcon(modifier = Modifier.padding(8.dp))
            }
        }

        val painter =
            if (guideMode !is GuideMode.Review)
                android.R.drawable.ic_menu_save
            else
                R.drawable.ic_success
        val text =
            if (guideMode !is GuideMode.Review)
                stringResource(R.string.btnGuardarGuia)
            else
                stringResource(R.string.lblCloseGuide)
        ExtendedFloatingActionButton(
            onClick = onCloseGuide,
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    painter = painterResource(painter),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            text = {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        )
    }
}

@Composable
private fun NewQuestionIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.question_solid_full),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.TopEnd)
                .background(
                    Color.White,
                    CircleShape
                )
                .padding(1.dp)
        )
    }
}