package com.jonathanev.review.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.presentation.event.FolderActionEvent
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.FolderMenuOption
import com.jonathanev.review.presentation.model.FolderResultUi
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.state.ActionDialogState
import com.jonathanev.review.presentation.state.FoldersUiState
import com.jonathanev.review.presentation.viewmodel.ListFoldersViewModel
import com.jonathanev.review.ui.components.DialogConfirmDelete
import com.jonathanev.review.ui.components.DialogOptionsMenu
import com.jonathanev.review.ui.components.FolderItem
import com.jonathanev.review.ui.components.singleClick
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.ListFoldersDataProv
import com.jonathanev.review.ui.preview.providers.ListFoldersDataProvider
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
            onFolderClick = { _ -> },
            onMoveCancelGuideClick = {}
        )
    }
}

@Composable
fun ListFoldersRoute(
    viewModel: ListFoldersViewModel,
    onCreateFolderClick: () -> Unit,
    onFolderOpen: () -> Unit,
    onRenameFolderClick: (FolderUiModel) -> Unit,
    onNavWithoutFolders: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interactionMode by viewModel.interactionMode.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.resetNavigationPath()
    }

    LaunchedEffect(Unit) {
        viewModel.eventsMessages.collect { event ->
            when (event) {
                is FolderActionEvent.DeleteFolderSuccess -> {
                    showToast("Se ha borrado la carpeta correctamente", context)
                }

                is FolderActionEvent.ShowMessage -> {
                    showToast(event.text, context)
                }

                FolderActionEvent.OpenFolder -> {
                    onFolderOpen()
                }

                is FolderActionEvent.RenameFolder -> {
                    onRenameFolderClick(event.folder)
                }
            }
        }
    }

    when (val state = uiState) {
        FoldersUiState.Empty -> {
            onNavWithoutFolders()
        }

        FoldersUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is FoldersUiState.Success -> {
            val isDarkTheme = isSystemInDarkTheme()

            ListFoldersScreen(
                guias = state.folders,
                fileInteractionMode = interactionMode,
                onCreateFolderClick = onCreateFolderClick,
                onFolderClick = { posFolder ->
                    when (val result =
                        viewModel.getFolderSelected(state.folders, posFolder, isDarkTheme)) {
                        is FolderResultUi.Error -> {
                            Toast.makeText(
                                /* context = */ context,
                                /* text = */
                                "No se pudo encontrar la guia en la posición $posFolder",
                                /* duration = */
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is FolderResultUi.Success -> {
                            viewModel.onOpenMenu(result.folderUi)
                        }
                    }
                },
                onMoveCancelGuideClick = {
                    viewModel.onCancelMove()
                }
            )

            when (val state = dialogState) {
                is ActionDialogState.ConfirmDelete<FolderUiModel> -> {
                    DialogConfirmDelete(
                        description = "¿Estás seguro que deseas eliminar el folder y su contenido?",
                        onDeleteItemClick = {
                            viewModel.onConfirmDelete(state.item)
                        },
                        onCloseDialog = {
                            viewModel.onDismissDialog()
                        },
                    )
                }

                ActionDialogState.Hidden -> {
                    /* No se renderiza ningún diálogo */
                }

                is ActionDialogState.OptionsMenu<FolderUiModel> -> {
                    DialogOptionsMenu(
                        options = FolderMenuOption.entries,
                        optionTitle = { it.title },
                        onOptionSelected = { option ->
                            when (option) {
                                FolderMenuOption.OPEN -> {
                                    viewModel.navigateToDirectory(state.item)
                                }

                                FolderMenuOption.EDIT -> {
                                    viewModel.onEditFolder(state.item)
                                }

                                FolderMenuOption.DELETE -> {
                                    viewModel.onRequestDelete(state.item)
                                }
                            }
                        },
                        onCloseDialog = {
                            viewModel.onDismissDialog()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition(tag = "Prueba")
@Composable
fun ListFoldersScreen(
    guias: List<FolderUiModel>,
    fileInteractionMode: FileInteractionMode,
    onCreateFolderClick: () -> Unit,
    onFolderClick: (Int) -> Unit,
    onMoveCancelGuideClick: () -> Unit
) {
    Scaffold(
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
                        IconButton(onClick = singleClick { onMoveCancelGuideClick() }) {
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
            ExtendedFloatingActionButton(
                onClick = singleClick { onCreateFolderClick() },
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
                        stringResource(R.string.btnCrearCarpeta),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding)
        ) {
            itemsIndexed(guias) { index, guia ->
                FolderItem(
                    guia = guia,
                    onClick = { onFolderClick(index) }
                )
            }
        }
    }
}

private fun showToast(text: String, context: Context) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}