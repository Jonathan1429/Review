package com.jonathanev.review.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.state.GuidesUiState
import com.jonathanev.review.presentation.viewmodel.FragmentWithoutFilesViewModel
import com.jonathanev.review.ui.components.singleClick
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.WithoutFilesScreenProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.getColorSubtitle

@DevicePreviews
@Composable
fun WithoutFilesScreenPreview(
    @PreviewParameter(WithoutFilesScreenProvider::class) data: FileInteractionMode
) {
    ReviewTheme {
        WithoutFilesScreen(
            fileInteractionMode = data,
            onAddGuideClick = {},
            onMoveCancelGuideClick = {},
            onMoveSuccessGuideClick = {},
        )
    }
}

@Composable
fun WithoutFilesRoute(
    viewModel: FragmentWithoutFilesViewModel,
    onAddGuideClick: () -> Unit,
    onNavListGuides: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interactionMode by viewModel.interactionMode.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is GuidesUiState.Success) {
            onNavListGuides()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetNavigationPath()
        }
    }

    WithoutFilesScreen(
        fileInteractionMode = interactionMode,
        onAddGuideClick = onAddGuideClick,
        onMoveCancelGuideClick = {
            viewModel.onCancelMove()
        },
        onMoveSuccessGuideClick = {
            viewModel.movingGuide()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithoutFilesScreen(
    fileInteractionMode: FileInteractionMode,
    onAddGuideClick: () -> Unit,
    onMoveCancelGuideClick: () -> Unit,
    onMoveSuccessGuideClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        topBar = {
            if (fileInteractionMode == FileInteractionMode.MovingItem) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(R.string.lblMoving))
                    },
                    navigationIcon = {
                        IconButton(onClick = singleClick { onMoveCancelGuideClick() }) {
                            Icon(
                                painterResource(R.drawable.ic_cancel),
                                contentDescription = "Cancelar"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = singleClick { onMoveSuccessGuideClick() }) {
                            Icon(
                                painterResource(R.drawable.ic_success),
                                contentDescription = "Aceptar"
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = singleClick { onAddGuideClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.plus),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.btnAddGuide),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.no_files),
                contentDescription = "Imagen sin Archivos",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(id = R.string.lblWithoutGuides),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )

            Text(
                text = stringResource(id = R.string.lblDescWithoutGuides),
                color = getColorSubtitle(),
                textAlign = TextAlign.Center
            )
        }
    }
}