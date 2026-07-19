package com.jonathanev.review.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.CustomBoxCreateImage
import com.jonathanev.review.ui.components.OptionsCreateImage
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.cardStepBackground

@DevicePreviews
@Composable
fun PreviewCreateImageEdit() {
    ReviewTheme {
        CreateImageScreen(
            guideMode = GuideMode.Edit("", "", 0),
            uriImage = "",
            selectedImage = { },
            imageUploaded = { }
        )
    }
}

@DevicePreviews
@Composable
fun PreviewCreateImageCreate() {
    ReviewTheme {
        CreateImageScreen(
            guideMode = GuideMode.Create("", ""),
            uriImage = "",
            selectedImage = { },
            imageUploaded = { }
        )
    }
}

@DevicePreviews
@Composable
fun PreviewCreateImageReview() {
    ReviewTheme {
        CreateImageScreen(
            guideMode = GuideMode.Review("", 0),
            uriImage = "",
            selectedImage = { },
            imageUploaded = { }
        )
    }
}

@Composable

fun CreateImageRoute(
    guideMode: GuideMode,
    contentType: QuestionContentUi.Image,
    viewModel: SharedFragmentCreateFileViewModel,
    imageUploaded: () -> Unit
) {
    var uriImage by rememberSaveable { mutableStateOf(contentType.uri) }
    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setActualUri(uri.toString())
        }
    }

    CreateImageScreen(
        guideMode = guideMode,
        uriImage = uriImage,
        selectedImage = { resultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        imageUploaded = {
            viewModel.addImageContent()
            imageUploaded()
        }
    )
}

@Composable
fun CreateImageScreen(
    guideMode: GuideMode,
    uriImage: String,
    selectedImage: () -> Unit,
    imageUploaded: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                ) {
                    if (guideMode !is GuideMode.Review) {
                        OptionsCreateImage(
                            uriImage = uriImage,
                            selectImage = selectedImage,
                            imageUploaded = imageUploaded
                        )
                    }
                    CustomBoxCreateImage(
                        modifier = Modifier.then(
                            if (guideMode !is GuideMode.Review) {
                                Modifier
                            } else {
                                Modifier.padding(20.dp)
                            }
                        ),
                        uriImage
                    )
                }
            }
        }
    }
}