package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.TextColorSecondary

@ComponentsPreviews
@Composable
fun PreviewNavigationPagerBar() {
    ReviewTheme {
        Row() {
            NavigationPagerBar(
                actualQuestion = 9,
                totalQuestions = 15,
                onNextQuestionClick = {},
                onBackQuestionClick = {}
            )
        }
    }
}

@Composable
fun RowScope.NavigationPagerBar(
    actualQuestion: Int,
    totalQuestions: Int,
    onBackQuestionClick: () -> Unit,
    onNextQuestionClick: () -> Unit
) {
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.size(50.dp),
            enabled = actualQuestion > 1,
            onClick = onBackQuestionClick //singleClick { onBackQuestionClick() }
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(R.drawable.ic_circle_left_right),
                contentDescription = "Prev",
                tint = if (actualQuestion > 1) Color.Unspecified else TextColorSecondary
            )
        }
        Text(
            text = "${stringResource(R.string.etPregunta)} $actualQuestion ${stringResource(R.string.lblOf)} $totalQuestions",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        IconButton(
            modifier = Modifier.size(50.dp),
            enabled = totalQuestions > 1,
            onClick = onNextQuestionClick //singleClick { onNextQuestionClick() }
        ) {
            Icon(
                modifier = Modifier
                    .size(30.dp)
                    .rotate(180f),
                painter = painterResource(R.drawable.ic_circle_left_right),
                contentDescription = "Next",
                tint = Color.Unspecified
            )
        }
    }
}