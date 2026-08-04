package com.jonathanev.review.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.Composable
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
import com.jonathanev.review.presentation.model.GuideMenuOption
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.state.ActionDialogState
import com.jonathanev.review.presentation.state.GuidesUiState
import com.jonathanev.review.presentation.viewmodel.FragmentListGuidesViewModel
import com.jonathanev.review.ui.components.DialogConfirmDelete
import com.jonathanev.review.ui.components.DialogOptionsMenu
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
    onBackNav: () -> Unit
) {
    val context = LocalContext.current
    var currentInteractionMode by remember(fileInteractionMode) {
        mutableStateOf(fileInteractionMode)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()

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
                            viewModel.onOpenMenu(result.guideUiModel)
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

            when (val state = dialogState) {
                is ActionDialogState.ConfirmDelete<GuideUiModel> -> {
                    DialogConfirmDelete(
                        description = "¿Estás seguro que deseas eliminar la guia?",
                        onDeleteItemClick = {
                            viewModel.onConfirmDelete(state.item)
                            onBackNav()
                        },
                        onCloseDialog = {
                            viewModel.onDismissDialog()
                        }
                    )
                }

                ActionDialogState.Hidden -> {
                    /* No se renderiza ningún diálogo */
                }

                is ActionDialogState.OptionsMenu<GuideUiModel> -> {
                    DialogOptionsMenu(
                        options = GuideMenuOption.entries,
                        optionTitle = { it.title },
                        onOptionSelected = { option ->
                            when (option) {
                                GuideMenuOption.OPEN -> {
                                    viewModel.setActiveGuide(state.item)
                                    onOpenGuideClick()
                                }

                                GuideMenuOption.RENAME -> {
                                    viewModel.onDismissDialog()
                                    onRenameGuideClick(state.item)
                                }

                                GuideMenuOption.MOVE -> {
                                    viewModel.setContext(state.item)
                                    onMoveGuideClick()
                                }

                                GuideMenuOption.DELETE -> {
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