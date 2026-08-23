package com.jonathanev.review.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import kotlin.math.abs

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

@OptIn(ExperimentalFoundationApi::class)
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

            // Controladores para la notificación de aviso al arrastrar
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            val pagerState = rememberPagerState(initialPage = posItem) {
                if (questionContentMode == QuestionContentMode.CREATING) 1 else imageList.size
            }

            // Acción centralizada para interceptar el regreso/salida
            val onBackAction = {
                if (questionContentMode == QuestionContentMode.EDITING) {
                    viewModel.updatePosContent(pagerState.currentPage)
                }

                if (newlyPickedUri != null) {
                    viewModel.onBackFromEditor()
                } else {
                    onBackNav()
                }
            }

            BackHandler(onBack = onBackAction)

            // Sincronizar posición con el ViewModel en modo EDICIÓN
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
                    viewModel.addImageContent(uri = selectedUri.toString())
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            // Si está en modo CREATING, interceptamos el arrastre para avisar al usuario
                            if (questionContentMode == QuestionContentMode.CREATING) {
                                Modifier.pointerInput(Unit) {
                                    detectHorizontalDragGestures { _, dragAmount ->
                                        if (abs(dragAmount) > 10f) {
                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                snackbarHostState.showSnackbar(
                                                    message = "El desplazamiento solo está disponible al editar elementos guardados",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                }
                            } else Modifier
                        ),
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
                            val targetPage = if (questionContentMode == QuestionContentMode.CREATING) {
                                posItem
                            } else {
                                pagerState.currentPage
                            }

                            viewModel.confirmSaveImage(
                                uri = uriImage,
                                currentPage = targetPage,
                                questionContentMode = questionContentMode
                            )
                            newlyPickedUri = null
                            onBackNav()
                        },
                        onBackNav = onBackAction
                    )
                }

                // Componente flotante que muestra el mensaje de aviso en la parte inferior
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                )
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
                            newlyPickedUri = null
                            onBackNav()
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