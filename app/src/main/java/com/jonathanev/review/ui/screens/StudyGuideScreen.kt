package com.jonathanev.review.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.event.CreateGuideEvent
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.state.GuideScreenUiState
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.AssetCarouselViewer
import com.jonathanev.review.ui.components.CustomAlertDialog
import com.jonathanev.review.ui.components.CustomTopBar
import com.jonathanev.review.ui.components.ErrorComponent
import com.jonathanev.review.ui.components.FilterTypeItem
import com.jonathanev.review.ui.components.QASelectType
import com.jonathanev.review.ui.components.ShowDeletePopUp
import com.jonathanev.review.ui.components.singleClick
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.model.QAType
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.StudyGuideScreenProv
import com.jonathanev.review.ui.preview.providers.StudyGuideScreenProvider
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun PreviewStudyGuideScreen(
    @PreviewParameter(StudyGuideScreenProvider::class) data: StudyGuideScreenProv
) {
    ReviewTheme {
        FillingGuideScreen(
            cardType = data.typeSelected,
            typeForSelected = data.typeForSelected,
            mediaSelected = data.mediaSelected,
            mediaForSelected = data.mediaForSelected,
            actualQuestion = data.actualQuestion,
            totalQuestions = data.totalQuestions,
            listTypeMedia = data.listTypeMedia,
            guideContext = data.guideContext,
            currentPosContent = 0,
            showDialogDeleteQuestion = data.showDialogDeleteQuestion,
            showDialogRepeatGuide = data.showDialogRepeatGuide,
            onDissmissDialogRepeatGuide = {},
            onConfirmDialogRepeatGuide = {},
            onContinueDialogDeleteQuestionClick = {},
            onBackQuestionClick = {},
            onNextQuestionClick = {},
            onDeleteQuestionClick = { },
            onCardTypeClicked = {},
            onFilterTypeClicked = {},
            onOpenAssetClick = { _, _ -> },
            onDeleteItemClick = { _, _ -> },
            onAddAssetClick = {},
            onAddQuestion = {},
            onCloseGuide = {},
            onCurrentPosContent = {},
            onDismissRequest = {}
        )
    }
}

@Composable
fun FillingGuideRoute(
    viewModel: SharedFragmentCreateFileViewModel,
    onOpenAssetClick: (QuestionContentUi, posItem: Int) -> Unit,
    onAddAssetClick: (ContentType, posItem: Int) -> Unit,
    onActionGuideNone: () -> Unit,
    onCloseGuide: () -> Unit
) {
    val context = LocalContext.current
    val typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER)
    val mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is GuideScreenUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is GuideScreenUiState.Error -> {
            ErrorComponent(
                onRetry = viewModel::retryLoad,
                onBack = onCloseGuide
            )
        }

        is GuideScreenUiState.Success -> {
            val cardType = state.qAType
            val mediaSelected = state.mediaSelected
            val currentPosContent = state.contadorContenido

            val totalQuestions = state.preguntas.size
            val actualQuestion = state.contadorPregunta + 1
            val textList by viewModel.textList.collectAsStateWithLifecycle()
            val imageList by viewModel.imageList.collectAsStateWithLifecycle()

            val listTypeMedia = if (state.mediaSelected == ContentType.TEXT) {
                textList
            } else {
                imageList
            }

            LaunchedEffect(Unit) {
                viewModel.createGuideEvent.collect { event ->
                    when (event) {
                        CreateGuideEvent.WithoutText ->
                            showToast("Debes tener al menos un texto", context)

                        CreateGuideEvent.WithoutTextQA ->
                            showToast(
                                "Debes tener al menos un texto en pregunta y respuesta",
                                context
                            )

                        is CreateGuideEvent.WithoutTextInPos -> {
                            showToast(
                                "Revisa la pregunta ${event.position} - Debes tener al menos un texto en pregunta/respuesta",
                                context
                            )
                        }

                        is CreateGuideEvent.ErrorGuideCreated -> {
                            showToast(event.text, context)
                            onCloseGuide()
                        }

                        is CreateGuideEvent.SuccessGuideCreated -> {
                            showToast(event.text, context)
                            onCloseGuide()
                        }

                        CreateGuideEvent.QADeleted -> {
                            showToast("Se ha eliminado la pregunta y respuesta", context)
                        }

                        CreateGuideEvent.CloseGuide -> {
                            onCloseGuide()
                        }
                    }
                }
            }

            FillingGuideScreen(
                cardType = cardType,
                typeForSelected = typeForSelected,
                mediaSelected = mediaSelected,
                mediaForSelected = mediaForSelected,
                actualQuestion = actualQuestion,
                totalQuestions = totalQuestions,
                listTypeMedia = listTypeMedia,
                guideContext = state.guideContext,
                currentPosContent = currentPosContent,
                showDialogDeleteQuestion = state.showDialogDeleteQuestion,
                showDialogRepeatGuide = state.showDialogRepeatGuide,
                onDissmissDialogRepeatGuide = viewModel::onDismissDialogRepeatGuide,
                onConfirmDialogRepeatGuide = viewModel::restartGuide,
                onContinueDialogDeleteQuestionClick = { isChecked ->
                    viewModel.onConfirmDeleteQuestion(dontAskAgain = isChecked)
                },
                onBackQuestionClick = {
                    viewModel.previousQuestion()
                },
                onNextQuestionClick = {
                    viewModel.onNextQuestionRequested(
                        actualQuestion = actualQuestion,
                        totalQuestions = totalQuestions
                    )
                },
                onDeleteQuestionClick = viewModel::onDeleteQuestionRequested,
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
                onCardTypeClicked = { cardTypeClicked ->
                    viewModel.onCardTypeChanged(cardTypeClicked)
                },
                onFilterTypeClicked = { filterTypeClicked ->
                    viewModel.onFilterTypeChanged(filterTypeClicked = filterTypeClicked)
                },
                onOpenAssetClick = { typeContent, posItem ->
                    onOpenAssetClick(
                        typeContent,
                        posItem
                    )
                },
                onAddAssetClick = { posItem -> onAddAssetClick(mediaSelected, posItem) },
                onAddQuestion = viewModel::addNextQuestion,
                onCloseGuide = {
                    when (state.guideContext) {
                        is GuideContext.Browsing -> viewModel.onCloseGuide()
                        is GuideContext.Creating -> viewModel.saveGuide()
                        is GuideContext.DeleteGuide -> viewModel.onCloseGuide()
                        is GuideContext.Editing -> viewModel.saveGuide()
                        is GuideContext.Moving -> viewModel.onCloseGuide()
                        is GuideContext.Rename -> viewModel.onCloseGuide()
                    }
                },
                onCurrentPosContent = { position ->
                    viewModel.updatePosContent(position)
                },
                onDismissRequest = viewModel::onDismissDialogDeleteQuestion
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillingGuideScreen(
    cardType: QAType,
    typeForSelected: List<QAType>,
    mediaSelected: ContentType,
    mediaForSelected: List<ContentType>,
    actualQuestion: Int,
    totalQuestions: Int,
    listTypeMedia: List<QuestionContentUi>,
    guideContext: GuideContext,
    showDialogDeleteQuestion: Boolean,
    currentPosContent: Int,
    showDialogRepeatGuide: Boolean,
    onDissmissDialogRepeatGuide: () -> Unit,
    onConfirmDialogRepeatGuide: () -> Unit,
    onContinueDialogDeleteQuestionClick: (Boolean) -> Unit,
    onBackQuestionClick: () -> Unit,
    onNextQuestionClick: () -> Unit,
    onDeleteQuestionClick: () -> Unit,
    onCardTypeClicked: (QAType) -> Unit,
    onFilterTypeClicked: (ContentType) -> Unit,
    onOpenAssetClick: (QuestionContentUi, posItem: Int) -> Unit,
    onDeleteItemClick: (typeContent: QuestionContentUi, positionItem: Int) -> Unit,
    onAddAssetClick: (posItem: Int) -> Unit,
    onAddQuestion: () -> Unit,
    onCloseGuide: () -> Unit,
    onCurrentPosContent: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        floatingActionButton = {
            FloatingActionButtons(
                guideContext = guideContext,
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
                guideContext = guideContext,
                onDeleteQuestionClick = onDeleteQuestionClick,
                onBackQuestionClick = onBackQuestionClick,
                onNextQuestionClick = onNextQuestionClick
            )
            QASelectType(
                typeForSelected = typeForSelected,
                cardType = cardType,
                onCardTypeClicked = { cardTypeClicked ->
                    onCardTypeClicked(cardTypeClicked)
                })
            FilterTypeItem(
                mediaForSelected = mediaForSelected,
                mediaSelected = mediaSelected,
                onFilterTypeClicked = { filterTypeClicked ->
                    onFilterTypeClicked(filterTypeClicked)
                })

            AssetCarouselViewer(
                assets = listTypeMedia,
                mediaForSelected = mediaSelected,
                guideContext = guideContext,
                currentPosContent = currentPosContent,
                onAddAssetClick = { posItem -> onAddAssetClick(posItem) },
                onDeleteItemClick = { typeContent, positionItem ->
                    onDeleteItemClick(
                        typeContent,
                        positionItem
                    )
                },
                onOpenAssetClick = { typeContent, posItem ->
                    onOpenAssetClick(
                        typeContent,
                        posItem
                    )
                },
                onCurrentPosContent = { position -> onCurrentPosContent(position) }
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
                        onDismissRequest = onDismissRequest
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
    guideContext: GuideContext,
    onAddQuestion: () -> Unit,
    onCloseGuide: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (guideContext !is GuideContext.Browsing) {
            FloatingActionButton(
                onClick = singleClick { onAddQuestion() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                NewQuestionIcon(modifier = Modifier.padding(8.dp))
            }
        }

        val painter =
            if (guideContext !is GuideContext.Browsing)
                android.R.drawable.ic_menu_save
            else
                R.drawable.ic_success
        val text =
            if (guideContext !is GuideContext.Browsing)
                stringResource(R.string.btnGuardarGuia)
            else
                stringResource(R.string.lblCloseGuide)
        ExtendedFloatingActionButton(
            onClick = singleClick { onCloseGuide() },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    modifier = Modifier.size(45.dp),
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

private fun showToast(text: String, context: Context) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}