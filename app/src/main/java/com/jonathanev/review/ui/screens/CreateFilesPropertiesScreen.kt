package com.jonathanev.review.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.state.CreatingUIState
import com.jonathanev.review.presentation.state.PropertiesFilesState
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.presentation.viewmodel.NavigationViewModel
import com.jonathanev.review.ui.components.BoxItemFolder
import com.jonathanev.review.ui.components.CustomTextField
import com.jonathanev.review.ui.components.IconsForSelect
import com.jonathanev.review.ui.components.SelectedPickerColor
import com.jonathanev.review.ui.model.PropertiesGuide
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.CreateFilesScreenDataProvider
import com.jonathanev.review.ui.preview.providers.PropertiesCreateFilesScreen
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun PreviewCreatingFile(
    @PreviewParameter(CreateFilesScreenDataProvider::class) data: PropertiesCreateFilesScreen
) {
    ReviewTheme {
        CreateFilesPropertiesScreen(
            state = data.state,
            fileFormMode = data.fileFormMode,
            onClickApply = {},
            onNameChange = {},
            onDescriptionChange = {},
            onConfirmDialog = {},
            onChangeIcon = { _, _ -> },
            onChangeColor = {},
            onShowToast = {},
            onDismissDialogs = {}
        )
    }
}

@Composable
fun CreateFilesPropertiesRoute(
    viewModel: CreateFilesViewModel,
    viewModelNavigation: NavigationViewModel,
    fileFormMode: FileFormMode,
    onNavBack: () -> Unit,
    onNavFillingGuide: (PropertiesGuide) -> Unit
) {
    val state by viewModel.uiStateComposable.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDarkTheme = isSystemInDarkTheme()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(fileFormMode) {
        when (fileFormMode) {
            FileFormMode.CreatingFile -> viewModel.initWithMode(fileFormMode)
            is FileFormMode.RenameFile -> {
                viewModel.initWithMode(fileFormMode)

                val oldName = fileFormMode.guideUiModel.nameGuide
                val responseFillFields = viewModel.fillFields(oldName)
                if (!responseFillFields) {
                    Toast.makeText(
                        context,
                        "Guia dañada, imposible renombrar",
                        Toast.LENGTH_SHORT
                    ).show()
                    onNavBack()
                }
            }

            FileFormMode.CreatingFolder -> viewModel.initWithMode(fileFormMode)
        }
    }

    LaunchedEffect(viewModel.eventUI, lifecycle) {
        viewModel.eventUI
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    is CreatingUIState.CreateFile -> {
                        onNavFillingGuide(PropertiesGuide(state.name, state.description))
                    }

                    is CreatingUIState.RenameFile -> {
                        val relativeGuidePath = viewModelNavigation.relativeGuidePath.value

                        viewModel.uploadCachedGuides(relativeGuidePath)

                        viewModel.renameFile(
                            oldName = state.oldName,
                            fileName = state.name,
                            description = state.description,
                            relativeGuidePath = relativeGuidePath
                        )
                        onNavBack()
                    }

                    is CreatingUIState.CreateFolder -> {
                        viewModel.saveMetadata(isDarkTheme)
                        onNavBack()
                    }

                    is CreatingUIState.Message -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    CreateFilesPropertiesScreen(
        state = state,
        fileFormMode = fileFormMode,
        onClickApply = {
            focusManager.clearFocus()
            viewModel.dismissOverwriteDialog()
            viewModel.processSaveRequest()
        },
        onNameChange = { viewModel.onNameChange(it) },
        onDescriptionChange = { viewModel.onDescriptionChange(it) },
        onConfirmDialog = { response ->
            if (response) {
                viewModel.validateData()
            }
        },
        onChangeIcon = { position, icon -> viewModel.changeIconSelected(position, icon) },
        onChangeColor = { color -> viewModel.changeColorSelected(color) },
        onShowToast = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
        onDismissDialogs = { viewModel.dismissOverwriteDialog() }
    )
}

@Composable
fun CreateFilesPropertiesScreen(
    state: PropertiesFilesState,
    fileFormMode: FileFormMode,
    onClickApply: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirmDialog: (Boolean) -> Unit,
    onChangeIcon: (Int, IconType) -> Unit,
    onChangeColor: (Int) -> Unit,
    onShowToast: (String) -> Unit,
    onDismissDialogs: () -> Unit
) {
    if (state.showOverwriteDialogFile) {
        AlertDialog(
            onDismissRequest = { onConfirmDialog(false) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirmDialog(true)
                    onDismissDialogs()
                }) { Text("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onConfirmDialog(false)
                    onDismissDialogs()
                }) { Text("Cancelar") }
            },
            title = { Text("Archivo existente") },
            text = { Text("Ya existe un archivo con ese nombre. ¿Deseas continuar?") }
        )
    }

    if (state.showOverwriteDialogFolder) {
        onShowToast("Ya existe un folder con ese nombre")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Button(
                onClick = onClickApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    //.navigationBarsPadding(), // Respeta la barra de navegación del sistema
                    .imePadding(), // Sube con el teclado de forma fija
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBotones)
            ) {
                Text("Aplicar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
                .padding(8.dp)
        ) {
            when (fileFormMode) {
                FileFormMode.CreatingFile, is FileFormMode.RenameFile -> {
                    CustomTextField(state.name, "Nombra tu archivo") { onNameChange(it) }
                    Spacer(Modifier.size(12.dp))
                    CustomTextField(
                        state.description,
                        "Descripción (Opcional)"
                    ) { onDescriptionChange(it) }
                    Spacer(Modifier.size(12.dp))
                    IconsForSelect(state.icons, state.selectedIndex) { position, icon ->
                        onChangeIcon(position, icon)
                    }
                }

                FileFormMode.CreatingFolder -> {
                    CustomTextField(state.name, "Nombra tu carpeta") { onNameChange(it) }
                    Spacer(Modifier.size(12.dp))
                    IconsForSelect(state.icons, state.selectedIndex) { position, icon ->
                        onChangeIcon(position, icon)
                    }
                    Spacer(Modifier.size(12.dp))
                    Text("Selecciona el color de la carpeta")
                    SelectedPickerColor { onChangeColor(it) }
                    Spacer(Modifier.size(12.dp))
                    Text("Previsualizacion:")
                    BoxItemFolder(
                        iconRes = state.icon,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}