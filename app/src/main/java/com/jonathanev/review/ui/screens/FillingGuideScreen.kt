package com.jonathanev.review.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.presentation.model.ActionGuide
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.AssetCarouselViewer
import com.jonathanev.review.ui.components.FilterChipItem
import com.jonathanev.review.ui.components.NavigationPagerBar
import com.jonathanev.review.ui.components.QATypeItem
import com.jonathanev.review.ui.mapper.toDrawable
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.model.QAType
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun PreviewFillingGuide() {
    ReviewTheme {
        FillingGuideScreen(
            typeSelected = QAType.QUESTION,
            typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER),
            mediaSelected = ContentType.TEXT,
            mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE),
            actualQuestion = 1,
            totalQuestions = 2,
            listTypeMedia = listOf(QuestionContentUi.Text("Hola", listOf())),
            onTypeClicked = {},
            onFilterClicked = {},
            onModifyAssetClick = {  },
            onAddQuestion = {},
            onSaveQuestion = {}
        )
    }
}

@Composable
fun FillingGuideRoute(
    viewModel: SharedFragmentCreateFileViewModel,
    action: ActionGuide,
    relativeGuidePath: RelativeGuidePath,
    onModifyAssetClick: (QuestionContentUi) -> Unit
) {
    var typeSelected by rememberSaveable { mutableStateOf(QAType.QUESTION) }
    val typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER)
    var mediaSelected by rememberSaveable { mutableStateOf(ContentType.TEXT) }
    val mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE)

    val totalQuestions = viewModel.uiState.collectAsStateWithLifecycle().value.preguntas.size
    val actualQuestion = viewModel.uiState.collectAsStateWithLifecycle().value.contadorPregunta
    val listTypeMedia = if (typeSelected == QAType.QUESTION) {
        viewModel.textList.collectAsStateWithLifecycle().value
    } else {
        viewModel.imageList.collectAsStateWithLifecycle().value
    }

    LaunchedEffect(Unit) {
        when (action) {
            is ActionGuide.CREATE -> TODO()
            is ActionGuide.EDIT -> {
                viewModel.getObtenerDatosXML(
                    noQuestion = action.noQuestion,
                    nameGuide = action.nameGuide,
                    relativeGuidePath = relativeGuidePath
                )
            }

            ActionGuide.NONE -> TODO()
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
        onTypeClicked = { typeClicked ->
            typeSelected = typeClicked
        },
        onFilterClicked = { filterClicked ->
            mediaSelected = filterClicked
        },
        onModifyAssetClick = { typeContent -> onModifyAssetClick(typeContent) },
        onAddQuestion = { },
        onSaveQuestion = { }
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
    onTypeClicked: (QAType) -> Unit,
    onFilterClicked: (ContentType) -> Unit,
    onModifyAssetClick: (QuestionContentUi) -> Unit,
    onAddQuestion: () -> Unit,
    onSaveQuestion: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButtons()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            CustomTopBar(actualQuestion, totalQuestions)
            QAType(typeForSelected, typeSelected, onTypeClicked = { typeClicked ->
                onTypeClicked(typeClicked)
            })
            FilterChip(mediaForSelected, mediaSelected, onFilterClicked = { filterClicked ->
                onFilterClicked(filterClicked)
            })

            AssetCarouselViewer(
                assets = listTypeMedia,
                mediaForSelected = mediaSelected,
                onAddAssetClick = { },
                onDeleteAssetClick = { },
                onModifyAssetClick = { typeContent -> onModifyAssetClick(typeContent) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    mediaForSelected: List<ContentType>,
    mediaSelected: ContentType,
    onFilterClicked: (ContentType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        mediaForSelected.forEach { item ->
            FilterChipItem(
                itemContentType = item,
                iconRes = item.toDrawable(),
                contentTypeSelected = mediaSelected,
                onFilterClicked = { filterClicked ->
                    onFilterClicked(filterClicked)
                }
            )
        }
    }
}

@Composable
private fun QAType(
    typeForSelected: List<QAType>,
    typeSelected: QAType,
    onTypeClicked: (QAType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        typeForSelected.forEach { item ->
            QATypeItem(
                item, typeSelected,
                onTypeClicked = { typeClicked ->
                    onTypeClicked(typeClicked)
                }
            )
        }
    }
}

@Composable
private fun CustomTopBar(
    actualQuestion: Int,
    totalQuestions: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        NavigationPagerBar(
            actualQuestion = actualQuestion,
            totalQuestions = totalQuestions
        )
        Spacer(modifier = Modifier.width(5.dp))
        Box(
            modifier = Modifier
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.drawable.ic_trash),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FloatingActionButtons() {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio de separación entre ambos botones
    ) {
        FloatingActionButton(
            onClick = { /* TODO: Lógica para añadir una pregunta más */ },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            NewQuestionIcon(modifier = Modifier.padding(8.dp))
        }

        ExtendedFloatingActionButton(
            onClick = { /* TODO: Lógica para guardar la guía completa */ },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_save),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            text = {
                Text(
                    "Guardar Guía",
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