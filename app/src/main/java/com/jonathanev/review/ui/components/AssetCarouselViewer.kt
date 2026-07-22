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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.GuideMode
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
                guideMode = data.guideMode,
                currentPosContent = 0,
                onAddAssetClick = { },
                onOpenAssetClick = {},
                onDeleteItemClick = { _, _ -> },
                onCurrentPosContent = {},
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetCarouselViewer(
    assets: List<QuestionContentUi>,
    mediaForSelected: ContentType,
    guideMode: GuideMode,
    currentPosContent: Int,
    onAddAssetClick: () -> Unit,
    onOpenAssetClick: (QuestionContentUi) -> Unit,
    onDeleteItemClick: (typeContent: QuestionContentUi, positionItem: Int) -> Unit,
    onCurrentPosContent: (Int) -> Unit,
) {
    val pagerState =
        rememberPagerState(initialPage = currentPosContent, pageCount = { assets.size })
    val scope = rememberCoroutineScope()
    val lazyRowState = rememberLazyListState()

    LaunchedEffect(currentPosContent) {
        if (assets.isNotEmpty() && pagerState.currentPage != currentPosContent) {
            pagerState.scrollToPage(currentPosContent)
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
            guideMode = guideMode,
            onOpenAssetClick = { typeContent -> onOpenAssetClick(typeContent) },
            onCurrentPosContent = { position -> onCurrentPosContent(position) }
        )
        val currentAsset = assets.getOrNull(pagerState.currentPage)
            .takeUnless { guideMode is GuideMode.Review }

        if (currentAsset != null) {
            DeleteAsset(mediaForSelected = mediaForSelected, onDeleteItemClick = {
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
            guideMode = guideMode,
            onAddAssetClick = onAddAssetClick
        )
    }
}

@Composable
private fun DeleteAsset(mediaForSelected: ContentType, onDeleteItemClick: () -> Unit) {
    val resourceSelected =
        if (mediaForSelected == ContentType.TEXT) R.string.lblText else R.string.lblImage

    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .clickable(onClick = onDeleteItemClick),
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