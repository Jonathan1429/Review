package com.jonathanev.review.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.QuestionItemProv
import com.jonathanev.review.ui.preview.providers.QuestionItemProvider
import com.jonathanev.review.ui.theme.HardColorButton
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.getCardContainerColor

@ComponentsPreviews
@Composable
fun PreviewQuestionCard(
    @PreviewParameter(QuestionItemProvider::class) data: QuestionItemProv
) {
    ReviewTheme {
        QuestionCard(data.question, data.noTexts, data.noImages, {}, {})
    }
}

@Composable
fun QuestionCard(
    question: String,
    noTexts: String,
    noImages: String,
    onEditingGuideClick: () -> Unit,
    onPlayGuideClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = getCardContainerColor()),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF263350), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = question,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CounterIconItem(R.drawable.ic_file, count = noTexts)
                    CounterIconItem(R.drawable.ic_image, count = noImages)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = singleClick { onEditingGuideClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.5.dp, HardColorButton.copy(alpha = 0.6f), CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = singleClick { onPlayGuideClick() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = HardColorButton.copy(
                                alpha = 0.6f
                            )
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}