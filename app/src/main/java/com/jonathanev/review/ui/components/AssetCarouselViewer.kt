package com.jonathanev.review.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.model.ContentType

@Preview(showBackground = true)
@Composable
fun PreviewAssetCarouselViewer() {
    AssetCarouselViewer(
        assets = listOf(
            QuestionContentUi.Text("", listOf()),
            QuestionContentUi.Text("a", listOf())
        ),
        mediaForSelected = ContentType.TEXT,
        guideMode = GuideMode.Review("", 0),
        onAddAssetClick = { },
        onAssetClick = {},
        onDeleteItemClick = { _, _ -> },
        onActionGuideNone = { }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetCarouselViewer(
    assets: List<QuestionContentUi>,
    mediaForSelected: ContentType,
    guideMode: GuideMode,
    onAddAssetClick: () -> Unit,
    onAssetClick: (QuestionContentUi) -> Unit,
    onDeleteItemClick: (typeContent: QuestionContentUi, positionItem: Int ) -> Unit,
    onActionGuideNone: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { assets.size })
    val scope = rememberCoroutineScope()
    val resourceSelected =
        if (mediaForSelected == ContentType.TEXT) R.string.lblText else R.string.lblImage
    val lazyRowState = rememberLazyListState()

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
            resourceSelected = resourceSelected,
            guideMode = guideMode,
            onAssetClick = { typeContent -> onAssetClick(typeContent) },
            onActionGuideNone = { onActionGuideNone() })
        if (guideMode !is GuideMode.Review) {
            DeleteAsset(resourceSelected, onDeleteItemClick = {
                val asset = assets[pagerState.currentPage]
                onDeleteItemClick(
                    asset,
                    pagerState.currentPage
                )
            })
        }
        Spacer(modifier = Modifier.height(24.dp))
        StepNavigationCarousel(
            lazyRowState = lazyRowState,
            assets = assets,
            pagerState = pagerState,
            scope = scope,
            guideMode = guideMode
        )
    }
}

@Composable
private fun DeleteAsset(resourceSelected: Int, onDeleteItemClick: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .clickable(onClick = {}),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_trash),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${stringResource(R.string.lblDelete)} ${stringResource(resourceSelected)}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}