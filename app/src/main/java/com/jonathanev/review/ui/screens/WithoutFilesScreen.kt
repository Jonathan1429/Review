package com.jonathanev.review.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.viewmodel.FragmentWithoutFilesViewModel
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun WithoutFilesScreenPreview() {
    ReviewTheme {
        WithoutFilesScreen(
            fileInteractionMode = FileInteractionMode.MovingItem,
            onAddGuideClick = {},
            onMoveCancelGuideClick = {},
            onMoveSuccessGuideClick = {},
        )
    }
}

@Composable
fun WithoutFilesRoute(
    fileInteractionMode: FileInteractionMode,
    onAddGuideClick: () -> Unit,
    viewModel: FragmentWithoutFilesViewModel
) {
    WithoutFilesScreen(
        fileInteractionMode = fileInteractionMode,
        onAddGuideClick = onAddGuideClick,
        onMoveCancelGuideClick = {
            viewModel.initRelativeGuide()
        },
        onMoveSuccessGuideClick = {
            viewModel.initRelativeGuide()
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
                        IconButton(onClick = onMoveCancelGuideClick) {
                            Icon(
                                painterResource(R.drawable.ic_cancel),
                                contentDescription = "Cancelar"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onMoveSuccessGuideClick) {
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
                onClick = onAddGuideClick,
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
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(padding)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.no_files),
                    contentDescription = "Imagen sin Archivos",
                    contentScale = ContentScale.Crop,
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
                    color = colorResource(id = R.color.text_gray),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}