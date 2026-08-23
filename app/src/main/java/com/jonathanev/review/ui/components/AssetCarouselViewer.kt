package com.jonathanev.review.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.DataMediaContentPagerProvider
import com.jonathanev.review.ui.preview.providers.MediaContentPagerProvider
import com.jonathanev.review.ui.theme.ReviewTheme

@ComponentsPreviews
@Composable
fun PreviewAssetCarouselViewer(
    @PreviewParameter(MediaContentPagerProvider::class) data: DataMediaContentPagerProvider
) {
    ReviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
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

                AssetCarouselViewer(
                    assets = data.listType,
                    mediaForSelected = data.mediaForSelected,
                    guideContext = data.guideContext,
                    currentPosContent = 0,
                    onAddAssetClick = { },
                    onOpenAssetClick = { _, _ -> },
                    onDeleteItemClick = { _, _ -> },
                    onCurrentPosContent = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetCarouselViewer(
    assets: List<QuestionContentUi>,
    mediaForSelected: ContentType,
    guideContext: GuideContext,
    currentPosContent: Int,
    onAddAssetClick: (posItem: Int) -> Unit,
    onOpenAssetClick: (QuestionContentUi, posItem: Int) -> Unit,
    onDeleteItemClick: (typeContent: QuestionContentUi, positionItem: Int) -> Unit,
    onCurrentPosContent: (Int) -> Unit,
    onMoveItem: (Int, Int) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()
    val maxIndex = (assets.size - 1).coerceAtLeast(0)
    val safeInitialPage = currentPosContent.coerceIn(0, maxIndex)

    val pagerState = rememberPagerState(
        initialPage = safeInitialPage,
        pageCount = { assets.size }
    )

    // Forzar scroll en cuanto el ViewModel entregue la lista actualizada y la nueva posición objetivo
    LaunchedEffect(currentPosContent, assets.size) {
        if (assets.isNotEmpty() && currentPosContent in assets.indices) {
            pagerState.scrollToPage(currentPosContent)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { settledPage ->
                // 🛡️ Ignoramos si el Pager está asentado en 0 pero la posición real esperada es otra
                // y la página está en proceso de scroll o inicialización.
                if (settledPage in assets.indices && !pagerState.isScrollInProgress) {
                    if (settledPage != currentPosContent) {
                        onCurrentPosContent(settledPage)
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MediaContentPager(
            pagerState = pagerState,
            assets = assets,
            mediaForSelected = mediaForSelected,
            guideContext = guideContext,
            onOpenAssetClick = { typeContent, posItem -> onOpenAssetClick(typeContent, posItem) },
            onDeleteAssetClick = { typeContent, positionItem ->
                onDeleteItemClick(typeContent, positionItem)
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        StepNavigationCarousel(
            assets = assets,
            pagerState = pagerState,
            scope = scope,
            guideContext = guideContext,
            onAddAssetClick = { posItem -> onAddAssetClick(posItem) },
            onMoveItem = { from, to -> onMoveItem(from, to) }
        )
    }
}