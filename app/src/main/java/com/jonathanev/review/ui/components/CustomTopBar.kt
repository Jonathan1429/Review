package com.jonathanev.review.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.CustomTopBarProv
import com.jonathanev.review.ui.preview.providers.CustomTopBarProvider
import com.jonathanev.review.ui.theme.ReviewTheme

@ComponentsPreviews
@Composable
fun PreviewCustomTopBar(
    @PreviewParameter(CustomTopBarProvider::class) data: CustomTopBarProv
) {
    ReviewTheme {
        CustomTopBar(
            actualQuestion = data.actualQuestion,
            totalQuestions = data.totalQuestion,
            guideContext = data.guideContext,
            onDeleteQuestionClick = {},
            onBackQuestionClick = {},
            onNextQuestionClick = {}
        )
    }
}

@Composable
fun CustomTopBar(
    actualQuestion: Int,
    totalQuestions: Int,
    guideContext: GuideContext,
    onDeleteQuestionClick: () -> Unit,
    onBackQuestionClick: () -> Unit,
    onNextQuestionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        NavigationPagerBar(
            actualQuestion = actualQuestion,
            totalQuestions = totalQuestions,
            onBackQuestionClick = onBackQuestionClick,
            onNextQuestionClick = onNextQuestionClick
        )
        Spacer(modifier = Modifier.width(5.dp))
        if (guideContext !is GuideContext.Browsing) {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .clickable(onClick = singleClick { onDeleteQuestionClick() }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}