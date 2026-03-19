package com.jonathanev.review.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.baseColor
import com.jonathanev.review.ui.theme.cardStepBackground
import com.jonathanev.review.ui.theme.iconBackground
import com.skydoves.compose.stability.runtime.TraceRecomposition

@Preview(showBackground = true)
@Composable
fun PreviewPropertiesFiles() {
    val iconsFolders = listOf(
        IconType.ANCHOR_SOLID_FULL,
        IconType.ANGELLIST_BRANDS_SOLID_FULL,
        IconType.BACTERIA_SOLID_FULL
    )
    PropertiesFilesContent(iconsFolders, FolderAction.CreatingFolder)
}

@Composable
fun PropertiesFiles(
    viewModel: CreateFilesViewModel = viewModel(),
    mode: FolderAction
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    viewModel.loadIconsFor(mode)

    PropertiesFilesContent(state.icons, mode)
}

@Composable
fun PropertiesFilesContent(
    icons: List<IconType>,
    mode: FolderAction,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Button(
                onClick = { /* Tu lógica de guardado */ },
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
        var name by rememberSaveable { mutableStateOf("") }
        var description by rememberSaveable { mutableStateOf("") }
        var selectedIcon by rememberSaveable { mutableIntStateOf(0) }
        var color by remember { mutableStateOf(Color.Black) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                //.padding(padding)
                .padding(8.dp)
        ) {
            PersonalizationTF(name, "Nombra tu carpeta") { name = it }
            Spacer(Modifier.size(4.dp))
            if (mode is FolderAction.RenamingFile || mode == FolderAction.CreatingFile){
                PersonalizationTF(
                    description,
                    "Descripción (Opcional)"
                ) { description = it }
            }
            Spacer(Modifier.size(8.dp))
            IconsFiles(icons, selectedIcon) { selectedIcon = it }
            Spacer(Modifier.size(8.dp))
            if (mode is FolderAction.CreatingFolder){
                Text("Selecciona el color de la carpeta")
                PreviewColorFolder { color = it }
            }
        }
    }
}

@Composable
fun IconsFiles(icons: List<IconType>, selectedIcon:Int, onChangeIcon:(Int) -> Unit) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(1),
        modifier = Modifier.height(50.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(icons) { index, icon ->
            LayeredSelectedIcon(
                isSelected = selectedIcon == index,
                icon = icon
            ) { onChangeIcon(index) }
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
    IconsFiles(icons, 0){ }
}

@Preview(showBackground = true)
@Composable
fun PreviewIconFolder() {
    val icons = listOf(
        IconType.ANCHOR_SOLID_FULL,
        IconType.ANGELLIST_BRANDS_SOLID_FULL,
        IconType.BACTERIA_SOLID_FULL
    )
    IconsFiles(icons, 1){ }
}

@Composable
fun PreviewColorFolder(onChangeColor:(Color) -> Unit) {
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
            onChangeColor(colorEnvelope.color)
        },
    )
}