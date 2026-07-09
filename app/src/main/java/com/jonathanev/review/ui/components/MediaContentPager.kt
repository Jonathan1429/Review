package com.jonathanev.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.theme.BorderPasos
import com.jonathanev.review.ui.theme.TextColorSecondary
import com.jonathanev.review.ui.theme.cardStepBackground

@Preview(showBackground = true)
@Composable
fun PreviewMediaContentPager() {
    val listPreview = listOf(
        QuestionContentUi.Text("Hola", emptyList()),
        QuestionContentUi.Text("Adios", emptyList())
    )
    val listEmpty = listOf<QuestionContentUi>()
    val pagerState = rememberPagerState(pageCount = { 2 })
    MediaContentPager(
        pagerState = pagerState,
        assets = listPreview,
        mediaForSelected = ContentType.TEXT,
        resourceSelected = R.string.lblText,
        onModifyAssetClick = {}
    )
}

@Composable
fun MediaContentPager(
    pagerState: PagerState,
    assets: List<QuestionContentUi>,
    mediaForSelected: ContentType,
    resourceSelected: Int,
    onModifyAssetClick: (QuestionContentUi) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(1.5.dp, BorderPasos, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(cardStepBackground)
    ) {
        if (assets.isEmpty()) {
            when (mediaForSelected) {
                ContentType.IMAGE -> EmptyStateView(
                    icon = painterResource(R.drawable.ic_image),
                    title = "No hay imagen que mostrar",
                    subtitle = "Presiona el botón '+' de abajo para seleccionar un archivo multimedia (Imagen)."
                )

                ContentType.TEXT -> EmptyStateView(
                    icon = painterResource(R.drawable.ic_empty_notes),
                    title = "Sin contenido de texto",
                    subtitle = "Presiona el botón '+' de abajo para escribir tu primer contenido tipo Texto."
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (assets.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        assets.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = null,
                            tint = TextColorSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(onClick = {
                                    when (val asset = assets[pagerState.currentPage]) {
                                        is QuestionContentUi.Image -> {
                                            onModifyAssetClick(QuestionContentUi.Image(asset.uri, asset.nameFile))
                                        }
                                        QuestionContentUi.None -> TODO()
                                        is QuestionContentUi.Text -> TODO()
                                    }
                                })
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${stringResource(resourceSelected)} ${pagerState.currentPage + 1} ${
                                stringResource(R.string.lblOf)
                            } ${assets.size}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (val currentAsset = assets[page]) {
                        is QuestionContentUi.Image -> {
                            CustomBoxCreateImage(currentAsset.nameFile)
                        }

                        is QuestionContentUi.Text -> {
                            CustomBoxCreateText(
                                textValue = currentAsset.text,
                                hint = false,
                                onTextValueChange = {}
                            )
                        }

                        QuestionContentUi.None -> {
                            EmptyStateView(
                                icon = painterResource(R.drawable.ic_empty_notes),
                                title = "Sin contenido",
                                subtitle = "No se pudo cargar el contenido para mostrar"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono representativo con una opacidad sutil para que no sature
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Título principal
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        // Subtítulo opcional para dar más contexto
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}