package com.jonathanev.review.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.QuestionItemProv
import com.jonathanev.review.ui.preview.providers.QuestionItemProvider
import com.jonathanev.review.ui.theme.CircleContentSVG
import com.jonathanev.review.ui.theme.IconsCustom
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.cardStepBackground

@DevicePreviews
@Composable
fun PreviewQuestionCard(
    @PreviewParameter(QuestionItemProvider::class) data: QuestionItemProv
) {
    ReviewTheme {
        QuestionCard(data.question, data.noTexts, data.noImages, {})
    }
}

@Composable
fun QuestionCard(
    question: String,
    noTexts: String,
    noImages: String,
    onEditingGuideClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardStepBackground),
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

            HorizontalDivider(color = Color(0xFF263350), thickness = 1.dp)

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
                    /*Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.5.dp, CircleContentSVG, CircleShape)
                            .background(Color.Transparent)
                            .clickable(onClick = { /* Acción editar */ }),
                        contentAlignment = Alignment.Center // Centramos el icono
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = CircleContentSVG,
                            modifier = Modifier.size(18.dp)
                        )
                    }*/

                    IconButton(
                        onClick = { onEditingGuideClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.5.dp, CircleContentSVG, CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = CircleContentSVG
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = CircleContentSVG,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { /* Acción reproducir */ },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = CircleContentSVG),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = IconsCustom,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}