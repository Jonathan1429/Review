package com.jonathanev.review.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.state.DialogState
import com.jonathanev.review.presentation.state.GuidesUiState
import com.jonathanev.review.presentation.viewmodel.FragmentListGuidesViewModel
import com.jonathanev.review.ui.components.ItemGuide
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.StudyGuidesProv
import com.jonathanev.review.ui.preview.providers.StudyGuidesProvider
import com.jonathanev.review.ui.theme.HardColorButton
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun PreviewMovingGuide(
    @PreviewParameter(StudyGuidesProvider::class) data: StudyGuidesProv
) {
    ReviewTheme {
        ListGuidesScreen(
            guides = data.listStudyGuides,
            fileInteractionMode = data.fileInteractionMode,
            onAddGuideClick = { },
            onItemClick = { },
            onMoveCancelGuideClick = { },
            onMoveSuccessGuideClick = { },
            onErrorProcess = {}
        )
    }
}

@Composable
fun ListGuidesRoute(
    viewModel: FragmentListGuidesViewModel,
    fileInteractionMode: FileInteractionMode,
    onAddGuideClick: () -> Unit,
    onOpenGuideClick: () -> Unit,
    onRenameGuideClick: (GuideUiModel) -> Unit,
    onMoveGuideClick: () -> Unit,
    onBackNav: () -> Unit,
    onNavigateWithoutFilesScreen: () -> Unit,
) {
    val context = LocalContext.current
    var currentDialog by remember { mutableStateOf<DialogState?>(null) }

    var currentInteractionMode by remember(fileInteractionMode) {
        mutableStateOf(fileInteractionMode)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is GuidesUiState.Empty) {
            onNavigateWithoutFilesScreen()
        }
    }


    when (val state = uiState) {
        is GuidesUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is GuidesUiState.Empty -> {
            Box(modifier = Modifier.fillMaxSize())
        }

        is GuidesUiState.Success -> {
            ListGuidesScreen(
                guides = state.guides,
                onAddGuideClick = onAddGuideClick,
                fileInteractionMode = currentInteractionMode,
                onItemClick = { posGuide ->
                    when (val result = viewModel.getGuideSelected(state.guides, posGuide)) {
                        GuideResultUi.Error -> {
                            onBackNav()
                            Toast.makeText(
                                /* context = */ context,
                                /* text = */
                                "No se pudo encontrar la guia en la posición $posGuide",
                                /* duration = */
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is GuideResultUi.Success -> {
                            currentDialog = DialogState.OptionsMenu(result)
                        }
                    }
                },
                onMoveCancelGuideClick = {
                    currentInteractionMode = FileInteractionMode.Default
                },
                onMoveSuccessGuideClick = {
                    viewModel.movingGuide(state.guides)
                },
                onErrorProcess = {
                    Toast.makeText(
                        /* context = */ context,
                        /* text = */ "Debes mover la guia antes de hacer otra accion",
                        /* duration = */ Toast.LENGTH_SHORT
                    ).show()
                }
            )

            currentDialog?.let { stateDialog ->
                when (stateDialog) {
                    is DialogState.ConfirmDelete -> {
                        DialogConfirmDelete(
                            onDeleteGuideClick = {
                                viewModel.deleteGuide(
                                    state.guides,
                                    nameGuide = stateDialog.item.guideUiModel.nameGuide
                                )
                                currentDialog = null
                            },
                            onCloseDialog = {
                                currentDialog = null
                            }
                        )
                    }

                    is DialogState.OptionsMenu -> {
                        DialogOptionsMenu(
                            stateDialog = stateDialog,
                            onOpenGuideClick = { guideUIModel ->
                                viewModel.setActiveGuide(guideUIModel)
                                onOpenGuideClick()
                            },
                            onRenameGuideClick = { guideUiModel ->
                                onRenameGuideClick(guideUiModel)
                            },
                            onMoveGuideClick = {
                                viewModel.setContext()
                                onMoveGuideClick()
                            },
                            onCloseDialog = {
                                currentDialog = null
                            },
                            onDialogConfirmDelete = { state ->
                                currentDialog = state
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListGuidesScreen(
    guides: List<GuideUiModel>,
    fileInteractionMode: FileInteractionMode,
    onAddGuideClick: () -> Unit,
    onItemClick: (Int) -> Unit,
    onMoveCancelGuideClick: () -> Unit,
    onMoveSuccessGuideClick: () -> Unit,
    onErrorProcess: () -> Unit
) {
    Scaffold(
        contentWindowInsets = if (fileInteractionMode == FileInteractionMode.MovingItem) {
            androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
        } else {
            androidx.compose.foundation.layout.WindowInsets.safeDrawing
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
            FloatingActionButton(
                onClick = onAddGuideClick,
                containerColor = HardColorButton,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    //.align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 32.dp)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Guía de Estudio",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(guides) { index, guide ->
                        ItemGuide(
                            guide = guide,
                            onClick = {
                                if (fileInteractionMode == FileInteractionMode.MovingItem) {
                                    onErrorProcess()
                                } else {
                                    onItemClick(index)
                                }
                            }
                        )

                        if (index < guides.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogConfirmDelete(
    onDeleteGuideClick: () -> Unit,
    onCloseDialog: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCloseDialog,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_advertencia),
                contentDescription = "Advertencia"
            )
        },
        title = {
            Text(text = "¡Atención!")
        },
        text = { Text(text = "¿Estás seguro que deseas eliminar la guia?") },
        confirmButton = {
            TextButton(onClick = {
                onDeleteGuideClick()
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCloseDialog) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DialogOptionsMenu(
    stateDialog: DialogState.OptionsMenu,
    onOpenGuideClick: (GuideUiModel) -> Unit,
    onRenameGuideClick: (GuideUiModel) -> Unit,
    onMoveGuideClick: () -> Unit,
    onCloseDialog: () -> Unit,
    onDialogConfirmDelete: (DialogState) -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            onCloseDialog()
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_advertencia),
                contentDescription = "Advertencia"
            )
        },
        title = {
            Text(text = "¿Qué acción deseas realizar?")
        },
        text = {
            val opciones = listOf("Abrir", "Eliminar", "Cambiar nombre", "Mover")

            LazyColumn {
                items(opciones) { opcion ->
                    Text(
                        text = opcion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (opcion) {
                                    "Abrir" -> {
                                        onCloseDialog()
                                        onOpenGuideClick(stateDialog.item.guideUiModel)
                                    }

                                    "Eliminar" -> {
                                        onDialogConfirmDelete(DialogState.ConfirmDelete(stateDialog.item))
                                    }

                                    "Cambiar nombre" -> {
                                        onCloseDialog()
                                        onRenameGuideClick(stateDialog.item.guideUiModel)
                                    }

                                    "Mover" -> {
                                        onCloseDialog()
                                        onMoveGuideClick()
                                    }
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onCloseDialog()
            }) {
                Text("Cancelar")
            }
        }
    )
}