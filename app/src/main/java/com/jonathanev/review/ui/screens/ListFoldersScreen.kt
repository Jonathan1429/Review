package com.jonathanev.review.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.viewmodel.ListFoldersViewModel
import com.jonathanev.review.ui.components.GuiaItem
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.ListFoldersDataProv
import com.jonathanev.review.ui.preview.providers.ListFoldersDataProvider
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.ReviewTheme
import com.skydoves.compose.stability.runtime.TraceRecomposition

@DevicePreviews
@Composable
fun PreviewFoldersScreen(
    @PreviewParameter(ListFoldersDataProvider::class) data: ListFoldersDataProv
) {
    ReviewTheme {
        ListFoldersScreen(
            guias = data.listFolders,
            fileInteractionMode = data.fildeInteractionMode,
            onCreateFolderClick = {},
            onFolderClick = { _, _ -> },
            onMoveCancelGuideClick = {}
        )
    }
}

@Composable
fun ListFoldersRoute(
    viewModel: ListFoldersViewModel,
    guias: List<FolderUiModel>,
    fileInteractionMode: FileInteractionMode,
    onCreateFolderClick: () -> Unit,
    onFolderClick: (FileInteractionMode) -> Unit,
) {
    var currentInteractionMode by rememberSaveable(fileInteractionMode) {
        mutableStateOf(fileInteractionMode)
    }

    ListFoldersScreen(
        guias = guias,
        fileInteractionMode = currentInteractionMode,
        onCreateFolderClick = onCreateFolderClick,
        onFolderClick = { name, fileInteractionMode ->
            viewModel.navigateToDirectory(name)
            onFolderClick(fileInteractionMode)
        },
        onMoveCancelGuideClick = {
            currentInteractionMode = FileInteractionMode.Default
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition(tag = "Prueba")
@Composable
fun ListFoldersScreen(
    guias: List<FolderUiModel>,
    fileInteractionMode: FileInteractionMode,
    onCreateFolderClick: () -> Unit,
    onFolderClick: (String, FileInteractionMode) -> Unit,
    onMoveCancelGuideClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.background(Color.Red),
        contentWindowInsets = if (fileInteractionMode == FileInteractionMode.MovingItem) {
            ScaffoldDefaults.contentWindowInsets
        } else {
            WindowInsets.safeDrawing
        },
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
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateFolderClick,
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding)
        ) {
            items(guias) { guia ->
                GuiaItem(
                    guia,
                    onClick = {
                        onFolderClick(guia.folder.name, fileInteractionMode)
                    }
                )
            }
        }
    }
}