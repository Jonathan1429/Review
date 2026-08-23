package com.jonathanev.review.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.model.QuestionContentMode
import com.jonathanev.review.presentation.state.GuideScreenUiState
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.CustomAlertDialog
import com.jonathanev.review.ui.components.CustomBoxCreateImage
import com.jonathanev.review.ui.components.ErrorComponent
import com.jonathanev.review.ui.components.OptionsCreateImage
import com.jonathanev.review.ui.components.singleClick
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.CreateImageContentProv
import com.jonathanev.review.ui.preview.providers.CreateImageContentProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.cardStepBackground
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

@DevicePreviews
@Composable
fun PreviewCreateImageScreen(
    @PreviewParameter(CreateImageContentProvider::class) data: CreateImageContentProv
) {
    ReviewTheme {
        CreateImageScreen(
            guideContext = data.guideContext,
            uriImage = data.uriImage,
            selectedImage = { },
            imageUploaded = { },
            onBackNav = {}
        )
    }
}

@Composable
fun CreateImageRoute(
    posItem: Int,
    viewModel: SharedFragmentCreateFileViewModel,
    questionContentMode: QuestionContentMode,
    onBackNav: () -> Unit
) {
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
                onBack = onBackNav
            )
        }

        is GuideScreenUiState.Success -> {
            val imageList by viewModel.imageList.collectAsStateWithLifecycle()
            var newlyPickedUri by rememberSaveable { mutableStateOf<String?>(null) }

            val pagerState = rememberPagerState(initialPage = posItem) {
                if (questionContentMode == QuestionContentMode.CREATING) 1 else imageList.size
            }

            // Acción centralizada para interceptar el regreso/salida
            val onBackAction = {
                // 1. Sincronizamos la posición actual antes de cualquier validación
                if (questionContentMode == QuestionContentMode.EDITING) {
                    viewModel.updatePosContent(pagerState.currentPage)
                }

                // 2. Si seleccionó una imagen nueva y no la ha guardado, mostramos el diálogo
                if (newlyPickedUri != null) {
                    viewModel.onBackFromEditor()
                } else {
                    onBackNav()
                }
            }

            // Interceptamos la tecla o gesto físico de ir atrás
            BackHandler(onBack = onBackAction)

            // Sincronizar posición con el ViewModel al cambiar de página en el Pager
            LaunchedEffect(pagerState.currentPage) {
                if (questionContentMode == QuestionContentMode.EDITING) {
                    viewModel.updatePosContent(pagerState.currentPage)
                }
            }

            val resultLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                uri?.let { selectedUri ->
                    newlyPickedUri = selectedUri.toString()

                    viewModel.addImageContent(
                        uri = selectedUri.toString()
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = questionContentMode == QuestionContentMode.EDITING,
                beyondViewportPageCount = 1,
                key = { page ->
                    when (questionContentMode) {
                        QuestionContentMode.CREATING -> newlyPickedUri ?: "creating"
                        QuestionContentMode.EDITING -> {
                            if (page == pagerState.currentPage && newlyPickedUri != null) {
                                newlyPickedUri!!
                            } else {
                                imageList.getOrNull(page)?.uri ?: page
                            }
                        }
                    }
                }
            ) { page ->
                val uriImage = when (questionContentMode) {
                    QuestionContentMode.CREATING -> {
                        newlyPickedUri ?: ""
                    }

                    QuestionContentMode.EDITING -> {
                        if (page == pagerState.currentPage && newlyPickedUri != null) {
                            newlyPickedUri!!
                        } else {
                            imageList.getOrNull(page)?.uri ?: ""
                        }
                    }
                }

                CreateImageScreen(
                    guideContext = state.guideContext,
                    uriImage = uriImage,
                    selectedImage = {
                        resultLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    imageUploaded = {
                        viewModel.confirmSaveImage(uriImage, questionContentMode)
                        onBackNav()
                    },
                    onBackNav = onBackAction // Pasamos onBackAction para controlar también el botón superior de la topbar
                )
            }

            // Limpiamos la imagen temporal si cambia la página del pager
            LaunchedEffect(pagerState.currentPage) {
                newlyPickedUri = null
            }

            // Diálogo de confirmación para descartar cambios
            if (state.showDialogDiscardDraft) {
                Dialog(onDismissRequest = {
                    viewModel.onDismissDiscardDraft()
                }) {
                    CustomAlertDialog(
                        title = stringResource(R.string.lblDiscardChangesTitle),
                        message = stringResource(R.string.lblDiscardChangesMessage),
                        onDismissRequest = {
                            viewModel.onDismissDiscardDraft()
                        },
                        onConfirm = {
                            viewModel.onConfirmDiscardDraft()
                            newlyPickedUri = null // Limpiamos la selección no guardada
                            onBackNav()           // Salimos de la pantalla
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateImageScreen(
    guideContext: GuideContext,
    uriImage: String,
    selectedImage: () -> Unit,
    imageUploaded: () -> Unit,
    onBackNav: () -> Unit
) {
    val zoomableState = rememberZoomableImageState(
        rememberZoomableState()
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (guideContext !is GuideContext.Browsing) {
                FloatingActionButton(
                    onClick = singleClick { selectedImage() },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ) {
                    Icon(
                        modifier = Modifier.size(35.dp),
                        painter = painterResource(R.drawable.ic_file_image),
                        contentDescription = "Seleccionar imagen",
                        tint = Color.White
                    )
                }
            }
        }
    ) { padding ->
        // Contenido de la pantalla
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(42.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = cardStepBackground
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CustomBoxCreateImage(
                        modifier = Modifier.fillMaxSize(),
                        uriImage = uriImage,
                        contentScale = ContentScale.Fit,
                        state = zoomableState
                    )

                    OptionsCreateImage(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(bottom = 16.dp),
                        guideContext = guideContext,
                        uriImage = uriImage,
                        imageUploaded = imageUploaded,
                        onBackNav = onBackNav
                    )
                }
            }
        }
    }
}