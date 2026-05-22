package com.jonathanev.review.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.jonathanev.review.R
import com.jonathanev.review.presentation.event.CreateFilesEvent
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.model.ScreenDataUi
import com.jonathanev.review.presentation.state.CreatingFileUiState
import com.jonathanev.review.presentation.state.PreviewState
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.ui.mapper.toInt
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.baseColor
import com.jonathanev.review.ui.theme.iconBackground
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlinx.coroutines.flow.collectLatest

@Preview(showBackground = true)
@Composable
fun PreviewPropertiesFiles() {
    val iconsFolders = listOf(
        IconType.ANCHOR_SOLID_FULL,
        IconType.ANGELLIST_BRANDS_SOLID_FULL,
        IconType.BACTERIA_SOLID_FULL
    )

    PropertiesFilesContent(
        PreviewState(icons = iconsFolders, selectedIndex = 1),
        FolderAction.CreatingFolder,
        {},
        {},
        {},
        {},
        { _, _ -> },
        {}
    )
}

@Composable
fun PropertiesFiles(
    viewModel: CreateFilesViewModel = viewModel(),
    mode: FolderAction
) {
    //val state by viewModel.uiState.collectAsStateWithLifecycle()
    val state = viewModel.uiStateComposable
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(mode) {
        viewModel.initWithMode(mode)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when(event){
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
        /*viewModel.messages.collectLatest { event ->
            when (event) {
                is CreatingFileUiState.ContinuedProcess ->{
                    val data = ScreenDataUi(state.name, state.description, state.icon, state.color)
                    viewModel.saveMetadata(data)
                }

                is CreatingFileUiState.Message ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }*/
    }

    PropertiesFilesContent(
        state, mode,
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
fun PropertiesFilesContent(
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
            PersonalizationTF(state.name, "Nombra tu carpeta") { onNameChange(it) }
            if (mode is FolderAction.RenamingFile || mode == FolderAction.CreatingFile) {
                Spacer(Modifier.size(12.dp))
                PersonalizationTF(
                    state.description,
                    "Descripción (Opcional)"
                ) { onDescriptionChange(it) }
            }
            Spacer(Modifier.size(12.dp))
            IconsFiles(state.icons, state.selectedIndex) { position, icon ->
                onChangeIcon(position, icon)
            }
            if (mode is FolderAction.CreatingFolder) {
                Spacer(Modifier.size(12.dp))
                Text("Selecciona el color de la carpeta")
                PreviewColorFolder { onChangeColor(it) }
                Spacer(Modifier.size(12.dp))
                Text("Previsualizacion:")
                PreviewFolder(
                    state.color.toInt(),
                    state.icon.toInt(),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun IconsFiles(
    icons: List<IconType>,
    positionIcon: Int,
    onChangeIcon: (Int, IconType) -> Unit,
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(1),
        modifier = Modifier.height(50.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(icons) { index, icon ->
            LayeredSelectedIcon(
                isSelected = positionIcon == index,
                icon = icon,
            ) { onChangeIcon(index, icon) }
        }
    }
}

@Composable
fun LayeredSelectedIcon(
    icon: IconType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Definimos los colores (puedes usar tus colores de Theme.kt)
    val itemIconColor = Color(0xFF6370E7) // Equivalente a @color/item_icon
    val backgroundColor = Color(0xFFF5F5F5) // Equivalente a @color/bg_edittext
    val iconDrawable = when (icon) {
        IconType.LIGHTBULB -> R.drawable.ic_lightbulb_solid_full
        IconType.ANCHOR_SOLID_FULL -> R.drawable.ic_anchor_solid_full
        IconType.ANGELLIST_BRANDS_SOLID_FULL -> R.drawable.ic_angellist_brands_solid_full
        IconType.BACTERIA_SOLID_FULL -> R.drawable.ic_bacteria_solid_full
    }

    Box(
        modifier = modifier
            .size(56.dp) // Tamaño total del contenedor
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier
                        .background(itemIconColor) // Capa exterior (Capa 1)
                        .padding(2.dp)
                        .background(
                            backgroundColor,
                            RoundedCornerShape(7.dp)
                        ) // Capa media (Capa 2)
                        .padding(2.dp)
                        .background(
                            itemIconColor,
                            RoundedCornerShape(6.dp)
                        ) // Capa interior (Capa 3)
                } else {
                    Modifier.background(iconBackground) // Estado normal
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconDrawable),
            contentDescription = null,
            tint = if (isSelected) Color.White else itemIconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@TraceRecomposition(tag = "Prueba")
@Composable
fun PersonalizationTF(name: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = name,
        onValueChange = { onValueChange(it) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        maxLines = 1,
        label = { Text(label) },
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewIconFile() {
    val icons = listOf(IconType.LIGHTBULB)
    IconsFiles(icons, 0) { _, _ -> }
}

@Preview(showBackground = true)
@Composable
fun PreviewIconFolder() {
    val icons = listOf(
        IconType.ANCHOR_SOLID_FULL,
        IconType.ANGELLIST_BRANDS_SOLID_FULL,
        IconType.BACTERIA_SOLID_FULL
    )
    IconsFiles(icons, 1) { _, _ -> }
}

@Composable
fun PreviewColorFolder(onChangeColor: (Int) -> Unit) {
    val controller = rememberColorPickerController()
    val color = baseColor

    LaunchedEffect(Unit) {
        controller.selectByColor(color = color, fromUser = false)
    }

    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(10.dp),
        controller = controller,
        onColorChanged = { colorEnvelope: ColorEnvelope ->
            onChangeColor(colorEnvelope.color.toArgb())
        },
    )
}

@Composable
fun PreviewFolder(
    folderColor: Int, // Este es el color sólido que viene del picker
    iconRes: Int,       // El ID del recurso del icono
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(folderColor).copy(alpha = 50f / 255f)

    Box(
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor), // Fondo con transparencia
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "Preview Folder Icon",
            modifier = Modifier.size(75.dp),
            // 2. El icono lleva el color sólido seleccionado
            tint = Color(folderColor)
        )
    }
}