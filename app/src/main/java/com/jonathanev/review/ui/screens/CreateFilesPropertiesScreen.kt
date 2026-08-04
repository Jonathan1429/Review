package com.jonathanev.review.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.state.CreatingUIState
import com.jonathanev.review.presentation.state.PropertiesFilesState
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.ui.components.CardBoxPrevItem
import com.jonathanev.review.ui.components.CustomTextField
import com.jonathanev.review.ui.components.IconsForSelect
import com.jonathanev.review.ui.components.SelectedPickerColor
import com.jonathanev.review.ui.model.PropertiesGuide
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.CreateFilesScreenDataProvider
import com.jonathanev.review.ui.preview.providers.PropertiesCreateFilesScreen
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.getButtonBackgroundBrush
import com.jonathanev.review.ui.theme.getCardContainerColor
import com.jonathanev.review.ui.theme.getColorSubtitle
import com.jonathanev.review.ui.theme.getColorTitleCard

@DevicePreviews
@Composable
fun PreviewCreatingFile(
    @PreviewParameter(CreateFilesScreenDataProvider::class) data: PropertiesCreateFilesScreen
) {
    ReviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
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
}

@Composable
fun CreateFilesPropertiesRoute(
    viewModel: CreateFilesViewModel,
    fileFormMode: FileFormMode,
    onRenameFile: () -> Unit,
    onCreateFolder: () -> Unit,
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
                viewModel.fillFields(oldName, fileFormMode.guideUiModel.description)
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
                        viewModel.renameFile(
                            oldName = state.oldName,
                            newFileName = state.name,
                            newDescription = state.description
                        )
                        onRenameFile()
                    }

                    is CreatingUIState.CreateFolder -> {
                        onCreateFolder()
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
            viewModel.processSaveRequest(isDarkTheme)
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
                }) {
                    Text(
                        text = "Continuar",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onConfirmDialog(false)
                    onDismissDialogs()
                }) {
                    Text(
                        text = "Cancelar",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            title = { Text("Archivo existente") },
            text = {
                Text(
                    text = "Ya existe un archivo con ese nombre. ¿Deseas continuar?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }

    if (state.showOverwriteDialogFolder) {
        LaunchedEffect(Unit) {
            onShowToast("Ya existe un folder con ese nombre")
        }
    }

    var isIdentityExpanded by remember { mutableStateOf(true) }
    var isAppearanceExpanded by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Button(
                    onClick = onClickApply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = getButtonBackgroundBrush(),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "APLICAR CAMBIOS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Preview
            CardBoxPrevItem(state.name, state.icon, state.color, fileFormMode)

            // Items Collapsados
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CollapseCardIdentidad(
                    isIdentityExpanded = isIdentityExpanded,
                    fileFormMode = fileFormMode,
                    state = state,
                    onNameChange = onNameChange,
                    onDescriptionChange = onDescriptionChange,
                    onToggleExpanded = { isIdentityExpanded = !isIdentityExpanded }
                )

                CollapseCardApariencia(
                    isAppearanceExpanded = isAppearanceExpanded,
                    state = state,
                    onChangeIcon = onChangeIcon,
                    fileFormMode = fileFormMode,
                    onChangeColor = onChangeColor,
                    onToggleExpanded = { isAppearanceExpanded = !isAppearanceExpanded }
                )
            }
        }
    }
}

@Composable
private fun CollapseCardApariencia(
    isAppearanceExpanded: Boolean,
    state: PropertiesFilesState,
    onChangeIcon: (Int, IconType) -> Unit,
    fileFormMode: FileFormMode,
    onChangeColor: (Int) -> Unit,
    onToggleExpanded: () -> Unit
) {
    CollapsibleCard(
        title = "APARIENCIA",
        isExpanded = isAppearanceExpanded,
        onToggle = onToggleExpanded
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Seleccionar Ícono",
                style = MaterialTheme.typography.bodyMedium,
                color = getColorSubtitle()
            )
            IconsForSelect(state.icons, state.selectedIndex) { position, icon ->
                onChangeIcon(position, icon)
            }

            if (fileFormMode is FileFormMode.CreatingFolder) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )

                Text(
                    text = "Color de Tema",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getColorSubtitle()
                )

                SelectedPickerColor(
                    onChangeColor = { colorInt ->
                        onChangeColor(colorInt)
                    }
                )
            }
        }
    }
}

@Composable
private fun CollapseCardIdentidad(
    isIdentityExpanded: Boolean,
    fileFormMode: FileFormMode,
    state: PropertiesFilesState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleExpanded: () -> Unit
) {
    CollapsibleCard(
        title = "IDENTIDAD",
        isExpanded = isIdentityExpanded,
        onToggle = onToggleExpanded
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (fileFormMode) {
                FileFormMode.CreatingFile, is FileFormMode.RenameFile -> {
                    CustomTextField(
                        state.name,
                        "Nombra tu archivo"
                    ) { onNameChange(it) }
                    CustomTextField(
                        state.description,
                        "Descripción (Opcional)"
                    ) { onDescriptionChange(it) }
                }

                FileFormMode.CreatingFolder -> {
                    CustomTextField(
                        state.name,
                        "Nombra tu carpeta"
                    ) { onNameChange(it) }
                }
            }
        }
    }
}

@Composable
fun CollapsibleCard(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = getCardContainerColor()),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = getColorTitleCard(),
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = getColorTitleCard()
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}