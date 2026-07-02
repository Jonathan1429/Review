package com.jonathanev.review.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.FolderAttributesUi
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.ui.components.GuiaItem
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.ListFoldersDataProvider
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.ReviewTheme
import com.skydoves.compose.stability.runtime.TraceRecomposition

@DevicePreviews
@Composable
fun PreviewListFolder(
    @PreviewParameter(ListFoldersDataProvider::class) data: List<FolderUiModel>
) {
    ReviewTheme {
        ListFoldersScreen(data, onCreateFolderClick = {}, onFolderClick = {})
    }
}

@DevicePreviews
@Composable
fun PreviewGuiaItem(
    @PreviewParameter(ListFoldersDataProvider::class) data: List<FolderUiModel>
){
    ReviewTheme {
        GuiaItem(data[0]) { }
    }
}

@TraceRecomposition(tag = "Prueba")
@Composable
fun ListFoldersScreen(
    guias: List<FolderUiModel>,
    onCreateFolderClick: (FolderAction) -> Unit,
    onFolderClick: (Int) -> Unit
) {
    Scaffold(
        //topBar = { TopAppBar(title = { Text("Carpetas") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateFolderClick(FolderAction.CreatingFolder) },
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
            itemsIndexed(guias) { index, guia ->
                GuiaItem(
                    guia,
                    onClick = { onFolderClick(index) }
                )
            }
        }
    }
}