package com.jonathanev.review.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.state.PreviewState
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.presentation.viewmodel.NavigationViewModel
import com.jonathanev.review.ui.components.BoxItemFolder
import com.jonathanev.review.ui.components.CustomTextField
import com.jonathanev.review.ui.components.IconsForSelect
import com.jonathanev.review.ui.components.LayeredSelectedIcon
import com.jonathanev.review.ui.components.SelectedPickerColor
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.BoxItemFolderDataProvider
import com.jonathanev.review.ui.preview.providers.CreateFilesScreenDataProvider
import com.jonathanev.review.ui.preview.providers.CustomTextFieldDataProvider
import com.jonathanev.review.ui.preview.providers.IconSelected
import com.jonathanev.review.ui.preview.providers.IconsFolderDataProvider
import com.jonathanev.review.ui.preview.providers.IconsGuideDataProvider
import com.jonathanev.review.ui.preview.providers.LayeredSelectedIconDataProvider
import com.jonathanev.review.ui.preview.providers.PropertiesCreateFilesScreen
import com.jonathanev.review.ui.preview.providers.PropertiesFolderSelected
import com.jonathanev.review.ui.preview.providers.PropertiesItemFolder
import com.jonathanev.review.ui.preview.providers.PropertiesTF
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun PreviewCreateFilesPropertiesScreen(
    @PreviewParameter(CreateFilesScreenDataProvider::class) data: PropertiesCreateFilesScreen
) {
    ReviewTheme {
        CreateFilesPropertiesScreen(
            PreviewState(icons = data.listIcons, selectedIndex = data.state.selectedIndex),
            data.state.mode,
            {},
            {},
            {},
            {},
            { _, _ -> },
            {}
        )
    }
}

@DevicePreviews
@Composable
fun PreviewIconsFile(
    @PreviewParameter(IconsGuideDataProvider::class) data: List<IconType>
) {
    ReviewTheme {
        IconsForSelect(data, 0) { _, _ -> }
    }
}

@DevicePreviews
@Composable
fun PreviewIconsFolder(
    @PreviewParameter(IconsFolderDataProvider::class) data: PropertiesFolderSelected
) {
    ReviewTheme {
        IconsForSelect(data.listIcons, data.posSelected) { _, _ -> }
    }
}

@DevicePreviews
@Composable
fun PreviewSelectedPickerColor() {
    ReviewTheme {
        SelectedPickerColor { }
    }
}

@DevicePreviews
@Composable
fun PreviewBoxItemFolder(
    @PreviewParameter(BoxItemFolderDataProvider::class) data: PropertiesItemFolder
) {
    ReviewTheme {
        BoxItemFolder(data.folderColor, data.iconRes)
    }
}

@DevicePreviews
@Composable
fun PreviewCustomTextField(
    @PreviewParameter(CustomTextFieldDataProvider::class) data: PropertiesTF
) {
    ReviewTheme {
        CustomTextField(data.name, data.label) { }
    }
}

@DevicePreviews
@Composable
fun PreviewLayeredSelectedIcon(
    @PreviewParameter(LayeredSelectedIconDataProvider::class) data: IconSelected
) {
    ReviewTheme {
        LayeredSelectedIcon(data.icon, data.isSelected) { }
    }
}

@Composable
fun CreateFilesPropertiesRoute(
    viewModel: CreateFilesViewModel,
    viewModelNavigation: NavigationViewModel,
    mode: FolderAction
) {
    //val state by viewModel.uiState.collectAsStateWithLifecycle()
    val state = viewModel.uiStateComposable
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    /*LaunchedEffect(mode) {
        viewModel.initWithMode(mode)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                CreateFilesEvent.CreatingFolder -> {
                    val data = ScreenDataUi(state.name, state.description, state.icon, state.color)
                    viewModel.saveMetadata(data)
                }

                is CreateFilesEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()

                CreateFilesEvent.CreateFile -> TODO()
                CreateFilesEvent.RenamingFile -> TODO()
            }
        }
        viewModel.messages.collectLatest { event ->
            when (event) {
                is CreatingFileUiState.ContinuedProcess ->{
                    val data = ScreenDataUi(state.name, state.description, state.icon, state.color)
                    viewModel.saveMetadata(data)
                }

                is CreatingFileUiState.Message ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    CreateFilesPropertiesScreen(
        state = state,
        mode = mode,
        onClickApply = {
            focusManager.clearFocus()
            //viewModel.prepareScreenData()
            viewModel.validateData()
        },
        onNameChange = { viewModel.onNameChange(it) },
        onDescriptionChange = { viewModel.onDescriptionChange(it) },
        onConfirmDialog = { viewModel.onConfirmAlertDialog(it) },
        onChangeIcon = { position, icon -> viewModel.changeIconSelected(position, icon) },
        onChangeColor = { color -> viewModel.changeColorSelected(color) }
    )
}

@Composable
fun CreateFilesPropertiesScreen(
    state: PreviewState,
    mode: FolderAction,
    onClickApply: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirmDialog: (Boolean) -> Unit,
    onChangeIcon: (Int, IconType) -> Unit,
    onChangeColor: (Int) -> Unit
) {
    if (state.showDialog) {
        AlertDialog(
            onDismissRequest = { onConfirmDialog(false) },
            confirmButton = {
                TextButton(onClick = { onConfirmDialog(true) }) { Text("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = { onConfirmDialog(false) }) { Text("Cancelar") }
            },
            title = { Text("Archivo existente") },
            text = { Text("Ya existe un archivo con ese nombre. ¿Deseas continuar?") }
        )
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
        //topBar = { TopAppBar(title = { Text("Carpetas") }) },
        /*floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateFolderClick() },
                containerColor = ColorBotones
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Boton crear carpeta",
                    tint = Color.White
                )
            }
        }*/
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                //.padding(padding)
                .padding(8.dp)
        ) {
            when (mode) {
                FolderAction.CreatingFile, is FolderAction.RenamingFile -> {
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

                FolderAction.CreatingFolder, FolderAction.RenamingFolder -> {
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
                        state.color,
                        state.icon,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    Text("No se puede procesar esa informacion")
                }
            }
        }
    }
}