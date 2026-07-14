package com.jonathanev.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.theme.BorderPasos
import com.jonathanev.review.ui.theme.BorderSelected
import com.jonathanev.review.ui.theme.cardStepBackground
import com.jonathanev.review.ui.theme.degradientColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun StepNavigationCarousel(
    lazyRowState: LazyListState,
    assets: List<QuestionContentUi>,
    pagerState: PagerState,
    scope: CoroutineScope,
    guideMode: GuideMode
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
                    .clickable { /* Guardar todo */ },
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

        val colorDegradient = degradientColor
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
                            color = if (isSelected) BorderSelected else BorderPasos,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardStepBackground)
                        .clickable {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}