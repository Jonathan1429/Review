package com.jonathanev.review.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.ui.components.CounterIconItem
import com.jonathanev.review.ui.components.QuestionCard
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.CounterIconItemProvider
import com.jonathanev.review.ui.preview.providers.CounterItem
import com.jonathanev.review.ui.preview.providers.PreviewQuestionsProvider
import com.jonathanev.review.ui.preview.providers.QuestionItem
import com.jonathanev.review.ui.preview.providers.QuestionItemProvider
import com.jonathanev.review.ui.theme.BackgroundColor
import com.jonathanev.review.ui.theme.CardBackgroundColor
import com.jonathanev.review.ui.theme.CircleContentSVG
import com.jonathanev.review.ui.theme.CyanAccent
import com.jonathanev.review.ui.theme.IconsCustom
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.TextColorPrimary
import com.jonathanev.review.ui.theme.TextColorSecondary
import com.jonathanev.review.ui.theme.cardStepBackground

@DevicePreviews
@Composable
fun PreviewPreviewQuestionsScreen(
    @PreviewParameter(PreviewQuestionsProvider ::class) data: List<QuestionItem>
) {
    ReviewTheme {
        PreviewQuestionsScreen(data)
    }
}

@Composable
fun PreviewQuestionsScreen(questions: List<QuestionItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cardStepBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.lblListQuestions),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(questions) { question ->
                QuestionCard(question = question)
            }
        }
    }
}
