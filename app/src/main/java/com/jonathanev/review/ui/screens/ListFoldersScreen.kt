package com.jonathanev.review.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FolderAttributesUi
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.ui.mapper.toColorRes
import com.jonathanev.review.ui.mapper.toDrawableRes
import com.jonathanev.review.ui.theme.ColorBotones
import com.jonathanev.review.ui.theme.Inter
import com.jonathanev.review.ui.theme.cardStepBackground
import com.jonathanev.review.ui.theme.baseColor
import com.skydoves.compose.stability.runtime.TraceRecomposition


@Preview
@Composable
fun PreviewListFolder() {
    val listGuides = listOf(
        FolderUiModel(
            folder = FolderAttributesUi(
                name = "Abap",
                imgFolder = IconType.BACTERIA_SOLID_FULL,
                color = ColorType.White
            ),
            numGuides = 5
        ),
        FolderUiModel(
            folder = FolderAttributesUi(
                name = "Kotlin",
                imgFolder = IconType.ANCHOR_SOLID_FULL,
                color = ColorType.White
            ),
            numGuides = 6
        ),
        FolderUiModel(
            folder = FolderAttributesUi(
                name = "SQL",
                imgFolder = IconType.BACTERIA_SOLID_FULL,
                color = ColorType.White
            ),
            numGuides = 5
        ),
        FolderUiModel(
            folder = FolderAttributesUi(
                name = "Ingles",
                imgFolder = IconType.ANCHOR_SOLID_FULL,
                color = ColorType.White
            ),
            numGuides = 6
        )
    )

    ListFoldersScreen(listGuides, onCreateFolderClick = {}, onFolderClick = {})
}

@TraceRecomposition(tag = "Prueba")
@Composable
fun ListFoldersScreen(
    guias: List<FolderUiModel>,
    onCreateFolderClick: () -> Unit,
    onFolderClick: (Int) -> Unit
) {
    Scaffold(
        //topBar = { TopAppBar(title = { Text("Carpetas") }) },
        floatingActionButton = {
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

@Composable
fun GuiaItem(
    guia: FolderUiModel,
    onClick: () -> Unit
) {
    //val color50 = ColorUtils.setAlphaComponent(guia.folder.color.toColorRes(), 50)
    val color50 = guia.folder.color.toColorRes().copy(alpha = 0.2f)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RectangleShape)
                .fillMaxWidth()
                .background(cardStepBackground)
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color50)
                    .padding(12.dp),
                painter = painterResource(guia.folder.imgFolder.toDrawableRes()),
                colorFilter = ColorFilter.tint(guia.folder.color.toColorRes()),
                contentDescription = "añadir carpeta"
            )
            HorizontalDivider(Modifier.size(8.dp), color = Color.Transparent)
            Text(
                text = guia.folder.name,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter,
                color = baseColor
            )
            Text(
                text = "${guia.numGuides} Guias",
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                color = baseColor
            )
        }
    }
}