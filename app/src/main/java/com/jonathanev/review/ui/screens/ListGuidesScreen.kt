package com.jonathanev.review.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.model.RelativeGuidePath
import com.jonathanev.review.presentation.state.DialogState
import com.jonathanev.review.presentation.viewmodel.FragmentListGuidesViewModel
import com.jonathanev.review.presentation.viewmodel.NavigationViewModel
import com.jonathanev.review.ui.components.ItemGuide
import com.jonathanev.review.ui.model.PropertiesGuide
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.StudyGuidesProvider
import com.jonathanev.review.ui.theme.BorderPasos
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.cardStepBackground

@DevicePreviews
@Composable
fun PreviewListGuidesScreen(
    @PreviewParameter(StudyGuidesProvider::class) data: List<GuideUiModel>
) {
    ReviewTheme {
        ListGuidesScreen(
            guides = data,
            onAddGuideClick = { },
            onItemClick = { },
            onMoveCancelGuideClick = { },
            onMoveSuccessGuideClick = { },
            fileInteractionMode = FileInteractionMode.MovingItem
        )
    }
}

@Composable
fun ListGuidesRoute(
    viewModel: FragmentListGuidesViewModel,
    navigationViewModel: NavigationViewModel,
    guides: List<GuideUiModel>,
    fileInteractionMode: FileInteractionMode,
    onAddGuideClick: () -> Unit,
    onOpenGuideClick: (String) -> Unit,
    onDeleteGuideClick: () -> Unit,
    onRenameGuideClick: (PropertiesGuide) -> Unit,
    onMoveGuideClick: () -> Unit,
    onMoveCancelGuideClick: () -> Unit,
    onMoveSuccessGuideClick: () -> Unit,
    onBackNav: () -> Unit
) {
    val context = LocalContext.current
    var currentDialog by rememberSaveable { mutableStateOf<DialogState?>(null) }
    val relativeGuidePath =
        RelativeGuidePath(navigationViewModel.relativeGuidePath.collectAsStateWithLifecycle().value)

    LaunchedEffect(relativeGuidePath) {
        viewModel.getAllGuides(relativeGuidePath)
    }

    ListGuidesScreen(
        guides = guides,
        onAddGuideClick = onAddGuideClick,
        onItemClick = { posGuide ->
            when (val result = viewModel.getGuideSelected(posGuide)) {
                GuideResultUi.Error -> {
                    onBackNav()
                    Toast.makeText(
                        /* context = */ context,
                        /* text = */ "No se pudo encontrar la guia en la posición $posGuide",
                        /* duration = */ Toast.LENGTH_SHORT
                    ).show()
                }

                is GuideResultUi.Success -> {
                    currentDialog = DialogState.OptionsMenu(result)
                }
            }
        },
        onMoveCancelGuideClick = onMoveCancelGuideClick,
        onMoveSuccessGuideClick = {
            viewModel.movingGuide(relativeGuidePath)
            onMoveSuccessGuideClick()
        },
        fileInteractionMode = fileInteractionMode
    )

    currentDialog?.let { stateDialog ->
        currentDialog = when (stateDialog) {
            is DialogState.ConfirmDelete -> {
                dialogConfirmDelete(
                    currentDialog,
                    viewModel,
                    stateDialog,
                    relativeGuidePath,
                    onDeleteGuideClick
                )
            }

            is DialogState.OptionsMenu -> {
                dialogOptionsMenu(
                    currDialog = currentDialog,
                    stateDialog = stateDialog,
                    onOpenGuideClick = onOpenGuideClick,
                    onRenameGuideClick = { guideUiModel ->
                        onRenameGuideClick(
                            PropertiesGuide(
                                name = guideUiModel.nameGuide,
                                description = guideUiModel.description
                            )
                        )
                    },
                    onMoveGuideClick = {
                        viewModel.setContext(relativeGuidePath)
                        navigationViewModel.setMainPath()
                        onMoveGuideClick()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListGuidesScreen(
    guides: List<GuideUiModel>,
    onAddGuideClick: () -> Unit,
    onItemClick: (Int) -> Unit,
    onMoveCancelGuideClick: () -> Unit,
    onMoveSuccessGuideClick: () -> Unit,
    fileInteractionMode: FileInteractionMode
) {
    Scaffold(
        topBar = {
            if (fileInteractionMode == FileInteractionMode.MovingItem) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(R.string.lblMoving))
                    },
                    navigationIcon = {
                        IconButton(onClick = onMoveCancelGuideClick ) {
                            Icon(painterResource(R.drawable.ic_cancel), contentDescription = "Cancelar")
                        }
                    },
                    actions = {
                        IconButton(onClick = onMoveSuccessGuideClick ) {
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
                containerColor = ColorBotones,
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
                .background(cardStepBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.lblStudyGuide),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(guides) { index, guide ->
                        ItemGuide(
                            guide = guide,
                            onClick = { onItemClick(index) }
                        )

                        if (index < guides.lastIndex) {
                            HorizontalDivider(
                                color = BorderPasos,
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
private fun dialogConfirmDelete(
    currentDialog: DialogState?,
    viewModel: FragmentListGuidesViewModel,
    stateDialog: DialogState.ConfirmDelete,
    relativeGuidePath: RelativeGuidePath,
    onDeleteGuideClick: () -> Unit
): DialogState? {
    var currentDialog1 = currentDialog
    AlertDialog(
        onDismissRequest = {
            currentDialog1 = null
        },
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
                viewModel.deleteGuide(
                    nameGuide = stateDialog.guide.guideUiModel.nameGuide,
                    relativeGuidePath = relativeGuidePath
                )
                onDeleteGuideClick()
                currentDialog1 = null
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                currentDialog1 = null
            }) {
                Text("Cancelar")
            }
        }
    )
    return currentDialog1
}

@Composable
private fun dialogOptionsMenu(
    currDialog: DialogState?,
    stateDialog: DialogState.OptionsMenu,
    onOpenGuideClick: (String) -> Unit,
    onRenameGuideClick: (GuideUiModel) -> Unit,
    onMoveGuideClick: () -> Unit
): DialogState? {
    var currentDialog = currDialog
    AlertDialog(
        onDismissRequest = {
            currentDialog = null
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
                                        currentDialog = null
                                        onOpenGuideClick(stateDialog.guide.guideUiModel.nameGuide)
                                    }

                                    "Eliminar" -> {
                                        currentDialog =
                                            DialogState.ConfirmDelete(stateDialog.guide)
                                    }

                                    "Cambiar nombre" -> {
                                        onRenameGuideClick(stateDialog.guide.guideUiModel)
                                    }

                                    "Mover" -> {
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
                currentDialog = null
            }) {
                Text("Cancelar")
            }
        }
    )
    return currentDialog
}