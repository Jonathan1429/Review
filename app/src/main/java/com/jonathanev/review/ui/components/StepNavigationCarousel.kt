package com.jonathanev.review.ui.components

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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.presentation.model.GuideMode
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
                lazyRowState = lazyRowState,
                assets = data.listQuestionContent,
                pagerState = pagerState,
                scope = scope,
                guideMode = data.mode,
                onAddAssetClick = { _ -> }
            )
        }
    }
}

@Composable
fun StepNavigationCarousel(
    lazyRowState: LazyListState,
    assets: List<QuestionContentUi>,
    pagerState: PagerState,
    scope: CoroutineScope,
    guideMode: GuideMode,
    onAddAssetClick: (posItem: Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (guideMode !is GuideMode.Review) {
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
            state = lazyRowState,
            modifier = Modifier
                .weight(1f)
                .drawWithContent {
                    drawContent()

                    if (lazyRowState.canScrollForward) {
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
            itemsIndexed(assets) { index, _ ->
                val isSelected = index == pagerState.currentPage

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) HardColorButton.lighten(0.2f) else BorderGray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardStepBackground)
                        .singleClick(onClick = { scope.launch { pagerState.animateScrollToPage(index) } }),
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