package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun PreviewNavigationPagerBar() {
    ReviewTheme {
        Row() {
            NavigationPagerBar(
                actualQuestion = 9,
                totalQuestions = 15,
                onNextQuestionClick = {}
            )
        }
    }
}

@Composable
fun RowScope.NavigationPagerBar(
    actualQuestion: Int,
    totalQuestions: Int,
    onNextQuestionClick: () -> Unit
) {
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            enabled = actualQuestion > 1,
            onClick = { /* Anterior */}
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Prev",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "${stringResource(R.string.etPregunta)} $actualQuestion ${stringResource(R.string.lblOf)} $totalQuestions",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        IconButton(
            enabled =  totalQuestions > 1,
            onClick = onNextQuestionClick
        ) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}