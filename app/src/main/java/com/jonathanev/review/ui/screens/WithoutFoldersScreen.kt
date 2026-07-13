package com.jonathanev.review.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.ui.components.BasePasos
import com.jonathanev.review.ui.components.SinFolders
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.PasoPreviewData
import com.jonathanev.review.ui.preview.providers.PasosDataProvider
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun PreviewWithoutFoldersScreen() {
    ReviewTheme {
        WithoutFoldersScreen(onNavCreateFilesProperties = {})
    }
}

@DevicePreviews
@Composable
fun PreviewBasePasos(
    @PreviewParameter(PasosDataProvider::class) data: PasoPreviewData
) {
    ReviewTheme {
        BasePasos(
            image = data.image,
            title = data.title,
            description = data.description
        )
    }
}

@DevicePreviews
@Composable
fun PreviewSinFolders(){
    ReviewTheme {
        SinFolders()
    }
}

@Composable
fun WithoutFoldersScreen(
    onNavCreateFilesProperties: () -> Unit
) {
    Scaffold(
        //topBar = { TopAppBar(title = { Text("Carpetas") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavCreateFilesProperties,
                containerColor = ColorBotones
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Boton crear carpeta",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SinFolders()
            Spacer(Modifier.size(8.dp))
            BasePasos(
                R.drawable.ic_plus_circle_outline,
                R.string.lblTitleStepOne,
                R.string.lblDescStepOne
            )
            Spacer(Modifier.size(8.dp))
            BasePasos(
                R.drawable.ic_palette,
                R.string.lblTitleStepTwo,
                R.string.lblDescStepTwo
            )
            Spacer(Modifier.size(8.dp))
            BasePasos(
                R.drawable.ic_folder_move_outline,
                R.string.lblTitleStepThree,
                R.string.lblDescStepThree
            )
        }
    }
}
