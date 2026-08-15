package com.jonathanev.review.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.StepNavigationCarouselProv
import com.jonathanev.review.ui.preview.providers.StepNavigationCarouselProviders
import com.jonathanev.review.ui.theme.BorderGray
import com.jonathanev.review.ui.theme.HardColorButton
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.cardStepBackground
import com.jonathanev.review.ui.theme.lighten
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@ComponentsPreviews
@Composable
fun PreviewCarousel(
    @PreviewParameter(StepNavigationCarouselProviders::class) data: StepNavigationCarouselProv
) {
    ReviewTheme {
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

            val scope = rememberCoroutineScope()
            val pagerState = rememberPagerState(pageCount = { data.listQuestionContent.size })
            val lazyRowState = rememberLazyListState()

            StepNavigationCarousel(
                assets = data.listQuestionContent,
                pagerState = pagerState,
                scope = scope,
                guideContext = data.guideContext,
                onAddAssetClick = { _ -> }
            )
        }
    }
}

@Composable
fun StepNavigationCarousel(
    assets: List<QuestionContentUi>,
    pagerState: PagerState,
    scope: CoroutineScope,
    guideContext: GuideContext,
    onAddAssetClick: (posItem: Int) -> Unit,
    onMoveItem: (Int, Int) -> Unit = { _, _ -> }
) {
    var initialDragIndex by remember { mutableStateOf<Int?>(null) }
    var currentDragIndex by remember { mutableStateOf<Int?>(null) }

    val lazyListState = rememberLazyListState()
    var items by remember(assets) {
        mutableStateOf(assets)
    }
    val reorderableLazyRowState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThreshold = 120.dp,
        onMove = { from, to ->
            if (initialDragIndex == null) {
                initialDragIndex = from.index
            }

            items = items.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            currentDragIndex = to.index
        }
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (guideContext !is GuideContext.Browsing) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .singleClick(onClick = { onAddAssetClick(pagerState.currentPage) }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar item",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        val colorDegradient = MaterialTheme.colorScheme.background
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .drawWithContent {
                    drawContent()

                    if (lazyListState.canScrollForward) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, colorDegradient),
                                startX = size.width - 60f,
                                endX = size.width
                            )
                        )
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> System.identityHashCode(item) }
            ) { index, item ->
                ReorderableItem(
                    state = reorderableLazyRowState,
                    key = System.identityHashCode(item)
                ) { isDragging ->
                    val elevation by animateDpAsState(
                        targetValue = if (isDragging) 12.dp else 0.dp,
                        label = "elevation_animation"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (isDragging) 1.15f else 1.0f,
                        label = "scale_animation"
                    )
                    val isSelected = index == pagerState.currentPage

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .shadow(
                                elevation = elevation,
                                shape = RoundedCornerShape(12.dp),
                                clip = false,
                                ambientColor = Color.Black.copy(alpha = 0.5f),
                                spotColor = Color.Black.copy(alpha = 0.5f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) HardColorButton.lighten(0.2f) else BorderGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardStepBackground)
                            .longPressDraggableHandle(
                                onDragStopped = {
                                    val start = initialDragIndex
                                    val end = currentDragIndex
                                    if (start != null && end != null && start != end) {
                                        onMoveItem(start, end)
                                    }
                                    initialDragIndex = null
                                    currentDragIndex = null
                                }
                            )
                            .singleClick(onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        index
                                    )
                                }
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}