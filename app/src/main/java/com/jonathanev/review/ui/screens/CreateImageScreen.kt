package com.jonathanev.review.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.model.QuestionContentMode
import com.jonathanev.review.presentation.state.GuideScreenUiState
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.CustomBoxCreateImage
import com.jonathanev.review.ui.components.ErrorComponent
import com.jonathanev.review.ui.components.OptionsCreateImage
import com.jonathanev.review.ui.components.singleClick
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.CreateImageContentProv
import com.jonathanev.review.ui.preview.providers.CreateImageContentProvider
import com.jonathanev.review.ui.theme.HardColorButton
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.cardStepBackground

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

            // Sync with ViewModel when page changes
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
                        uri = selectedUri.toString(),
                        questionContentMode = questionContentMode
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = questionContentMode == QuestionContentMode.EDITING
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
                        onBackNav()
                    },
                    onBackNav = onBackNav
                )
            }

            // Reset newlyPickedUri when navigating away from a page or when list updates
            LaunchedEffect(pagerState.currentPage) {
                newlyPickedUri = null
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (guideContext !is GuideContext.Browsing) {
                FloatingActionButton(
                    onClick = singleClick { selectedImage() },
                    containerColor = HardColorButton
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
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (guideContext !is GuideContext.Browsing) {
                                    Modifier
                                } else {
                                    Modifier.padding(20.dp)
                                }
                            ),
                        uriImage = uriImage
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