package com.jonathanev.review.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.mapper.stableKey
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.DataMediaContentPagerProvider
import com.jonathanev.review.ui.preview.providers.MediaContentPagerProvider
import com.jonathanev.review.ui.theme.BorderGray
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.Rojo100
import com.jonathanev.review.ui.theme.cardStepBackground

@ComponentsPreviews
@Composable
fun PreviewMediaContentPager(
    @PreviewParameter(MediaContentPagerProvider::class) data: DataMediaContentPagerProvider
) {
    val pagerState = rememberPagerState(pageCount = { data.sizeList })
    ReviewTheme {
        Column() {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PREVIEW: $data - ${data::class.simpleName}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            MediaContentPager(
                pagerState = pagerState,
                assets = data.listType,
                mediaForSelected = data.mediaForSelected,
                guideContext = data.guideContext,
                onOpenAssetClick = { _, _ -> },
                onDeleteAssetClick = { _, _ -> },
            )
        }
    }
}

@Composable
fun MediaContentPager(
    pagerState: PagerState,
    assets: List<QuestionContentUi>,
    mediaForSelected: ContentType,
    guideContext: GuideContext,
    onOpenAssetClick: (typeContent: QuestionContentUi, posItem: Int) -> Unit,
    onDeleteAssetClick: (typeContent: QuestionContentUi, posItem: Int) -> Unit,
) {
    val resourceSelected =
        if (mediaForSelected == ContentType.TEXT) R.string.lblText else R.string.lblImage
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isLandscape) {
                    Modifier.fillMaxHeight(0.8f)
                } else {
                    Modifier.aspectRatio(1f)
                }
            )
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, BorderGray, RoundedCornerShape(24.dp))
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
            val currentAsset = assets.getOrNull(pagerState.currentPage)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { page ->
                    // 🟢 Usar la clave única del item en esa posición.
                    // Evita que la página 0 recicle el contenido de la página 1 tras un swap.
                    assets.getOrNull(page)?.stableKey ?: page
                }
            ) { page ->
                val assetInPage = assets.getOrNull(page)
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Contenido (Imagen o Texto)
                    when (assetInPage) {
                        is QuestionContentUi.Image -> {
                            CustomBoxCreateImage(
                                uriImage = assetInPage.uri
                            )
                        }

                        is QuestionContentUi.Text -> {
                            val textFieldValueWrapper = TextFieldValue(text = assetInPage.text)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        top = 58.dp,
                                        end = 20.dp,
                                        bottom = 20.dp
                                    )
                            ) {
                                CustomBoxCreateText(
                                    readOnly = true,
                                    textValue = textFieldValueWrapper,
                                    hint = false,
                                    onTextValueChange = {},
                                    selectedColor = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        null, QuestionContentUi.None -> {
                            EmptyStateView(
                                icon = painterResource(R.drawable.ic_empty_notes),
                                title = "Sin contenido",
                                subtitle = "No se pudo cargar el contenido para mostrar"
                            )
                        }
                    }

                    // Capa invisible para detectar el clic en cualquier modo (Review, Editing, Creating)
                    // Esto permite abrir el editor o visor a pantalla completa al tocar el contenido
                    if (assetInPage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .singleClick {
                                    onOpenAssetClick(assetInPage, page)
                                }
                        )
                    }
                }
            }

            // Capa Superior de Controles (Indicators + Action Bar)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
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
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }

                val painter =
                    if (guideContext !is GuideContext.Browsing) R.drawable.ic_edit else R.drawable.ic_eye

                // Barra de Acciones (Editar/Ver + Eliminar en TopStart | Contadores en TopEnd)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Grupo de Botones de Acción (Izquierda)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Editar / Ver
                        Box(
                            modifier = Modifier
                                .singleClick(onClick = {
                                    currentAsset?.let { asset ->
                                        onOpenAssetClick(asset, pagerState.currentPage)
                                    }
                                })
                                .padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(painter),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Botón Eliminar (Bote de basura)
                        if (guideContext !is GuideContext.Browsing) {
                            Box(
                                modifier = Modifier
                                    .singleClick(onClick = {
                                        currentAsset?.let { asset ->
                                            onDeleteAssetClick(asset, pagerState.currentPage)
                                        }
                                    })
                                    .padding(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color.Black.copy(alpha = 0.6f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_trash),
                                        contentDescription = "Eliminar",
                                        tint = Rojo100,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Contador / Badge (Derecha)
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

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

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
